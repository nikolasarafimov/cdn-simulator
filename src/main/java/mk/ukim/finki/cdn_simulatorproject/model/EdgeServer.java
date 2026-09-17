package mk.ukim.finki.cdn_simulatorproject.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
public class EdgeServer {

    private static final int CACHE_CAPACITY = 2;

    @Id
    private String edgeServerId;

    @Enumerated(EnumType.STRING)
    private CachingAlgorithmType cachingAlgorithmType;

    @Transient
    private CacheStrategy cacheStrategy;

    @ManyToOne
    private ReplicaServer replicaServer;

    private int requestCount;

    public EdgeServer(
            String edgeServerId,
            CacheStrategy cacheStrategy,
            ReplicaServer replicaServer
    ) {
        if (edgeServerId == null || edgeServerId.isBlank()) {
            throw new IllegalArgumentException("Edge server ID is required.");
        }

        if (cacheStrategy == null) {
            throw new IllegalArgumentException("Cache strategy is required.");
        }

        if (replicaServer == null) {
            throw new IllegalArgumentException("Replica server is required.");
        }

        this.edgeServerId = edgeServerId;
        this.cacheStrategy = cacheStrategy;
        this.cachingAlgorithmType = resolveAlgorithmType(cacheStrategy);
        this.replicaServer = replicaServer;
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

        if (replicaServer == null) {
            throw new IllegalStateException(
                    "Edge server is not connected to a replica server."
            );
        }

        requestCount++;

        Resource resource =
                cacheStrategy.getResource(clientRequest.getResourceId());

        boolean hit = resource != null;

        trace.add(
                new HopDTO(
                        edgeServerId,
                        "EDGE",
                        hit,
                        cacheStrategy.snapshot()
                                .stream()
                                .map(Resource::getResourceId)
                                .toList()
                )
        );

        if (!hit) {
            resource = replicaServer.handleRequest(clientRequest, trace);

            if (resource != null) {
                cacheStrategy.putInCache(resource);
            }
        }

        return resource;
    }

    public Resource handleRequests(ClientRequest clientRequest) {
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
                    "Caching algorithm type is not configured for edge server "
                            + edgeServerId + "."
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