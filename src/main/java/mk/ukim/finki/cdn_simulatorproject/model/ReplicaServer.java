package mk.ukim.finki.cdn_simulatorproject.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;
import mk.ukim.finki.cdn_simulatorproject.cache.CacheStrategy;
import mk.ukim.finki.cdn_simulatorproject.cache.CachingAlgorithmType;
import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.FIFOAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LFUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class ReplicaServer {

    private static final int CACHE_CAPACITY = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String replicaServerId;

    @Enumerated(EnumType.STRING)
    private CachingAlgorithmType cachingAlgorithmType;

    @Transient
    private CacheStrategy cacheStrategy;

    @Transient
    private OriginServer originServer;

    private String location;
    private int countRequests;

    public ReplicaServer(
            String replicaServerId,
            CacheStrategy cacheStrategy,
            OriginServer originServer,
            String location
    ) {
        if (replicaServerId == null || replicaServerId.isBlank()) {
            throw new IllegalArgumentException("Replica server ID is required.");
        }

        if (cacheStrategy == null) {
            throw new IllegalArgumentException("Cache strategy is required.");
        }

        if (originServer == null) {
            throw new IllegalArgumentException("Origin server is required.");
        }

        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Replica server location is required.");
        }

        this.replicaServerId = replicaServerId;
        this.cacheStrategy = cacheStrategy;
        this.cachingAlgorithmType = resolveAlgorithmType(cacheStrategy);
        this.originServer = originServer;
        this.location = location;
        this.countRequests = 0;
    }

    public Resource handleRequest(
            ClientRequest clientRequest,
            List<HopDTO> trace
    ) {
        if (clientRequest == null) {
            throw new IllegalArgumentException("Client request is required.");
        }

        if (trace == null) {
            throw new IllegalArgumentException("Request trace is required.");
        }

        ensureCacheStrategyInitialized();

        if (originServer == null) {
            throw new IllegalStateException(
                    "Replica server is not connected to an origin server."
            );
        }

        countRequests++;

        Resource resource =
                cacheStrategy.getResource(clientRequest.getResourceId());

        boolean hit = resource != null;

        trace.add(
                new HopDTO(
                        replicaServerId,
                        "REPLICA",
                        hit,
                        cacheStrategy.snapshot()
                                .stream()
                                .map(Resource::getResourceId)
                                .toList()
                )
        );

        if (!hit) {
            resource =
                    originServer.getResourceFromOriginServer(
                            clientRequest.getResourceId()
                    );

            if (resource != null) {
                cacheStrategy.putInCache(resource);
            }
        }

        return resource;
    }

    public Resource handleRequest(ClientRequest clientRequest) {
        return handleRequest(clientRequest, new ArrayList<>());
    }

    @PostLoad
    public void initCacheStrategy() {
        ensureCacheStrategyInitialized();
    }

    private void ensureCacheStrategyInitialized() {
        if (cacheStrategy != null) {
            return;
        }

        if (cachingAlgorithmType == null) {
            throw new IllegalStateException(
                    "Caching algorithm type is not configured for replica server "
                            + replicaServerId + "."
            );
        }

        cacheStrategy = createCacheStrategy(cachingAlgorithmType);
    }

    private static CacheStrategy createCacheStrategy(
            CachingAlgorithmType algorithmType
    ) {
        return switch (algorithmType) {
            case FIFO -> new FIFOAlgorithm(CACHE_CAPACITY);
            case LFU -> new LFUAlgorithm(CACHE_CAPACITY);
            case LRU -> new LRUAlgorithm(CACHE_CAPACITY);
        };
    }

    private static CachingAlgorithmType resolveAlgorithmType(
            CacheStrategy cacheStrategy
    ) {
        if (cacheStrategy instanceof FIFOAlgorithm) {
            return CachingAlgorithmType.FIFO;
        }

        if (cacheStrategy instanceof LFUAlgorithm) {
            return CachingAlgorithmType.LFU;
        }

        if (cacheStrategy instanceof LRUAlgorithm) {
            return CachingAlgorithmType.LRU;
        }

        throw new IllegalArgumentException(
                "Unsupported cache strategy: "
                        + cacheStrategy.getClass().getName()
        );
    }
}