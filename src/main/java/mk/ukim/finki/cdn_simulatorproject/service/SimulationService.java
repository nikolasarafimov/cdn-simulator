package mk.ukim.finki.cdn_simulatorproject.service;

import jakarta.annotation.PostConstruct;
import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.FIFOAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LFUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;
import mk.ukim.finki.cdn_simulatorproject.dto.RequestTraceDTO;
import mk.ukim.finki.cdn_simulatorproject.model.ClientRequest;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServer;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServerManager;
import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class SimulationService {

    private static final int CACHE_CAPACITY = 2;

    private final CDNService cdnService;
    private final EdgeServerManager edgeServerManager;
    private final OriginServer originServer;

    private final Queue<ClientRequest> requestLog;

    public SimulationService(
            CDNService cdnService,
            EdgeServerManager edgeServerManager,
            OriginServer originServer
    ) {
        this.cdnService = cdnService;
        this.edgeServerManager = edgeServerManager;
        this.originServer = originServer;
        this.requestLog = new ConcurrentLinkedQueue<>();
    }

    @PostConstruct
    public void init() {
        initializeTopology();
    }

    public void initializeTopology() {
        ReplicaServer replicaUsEast = new ReplicaServer(
                "replica-us-east",
                new LRUAlgorithm(CACHE_CAPACITY),
                originServer,
                "us-east-1"
        );

        ReplicaServer replicaEuWest = new ReplicaServer(
                "replica-eu-west",
                new LFUAlgorithm(CACHE_CAPACITY),
                originServer,
                "eu-west-1"
        );

        cdnService.addReplicaServer(replicaUsEast);
        cdnService.addReplicaServer(replicaEuWest);

        EdgeServer edgeA = new EdgeServer(
                "edge-a",
                new FIFOAlgorithm(CACHE_CAPACITY),
                replicaUsEast
        );

        EdgeServer edgeB = new EdgeServer(
                "edge-b",
                new LRUAlgorithm(CACHE_CAPACITY),
                replicaEuWest
        );

        edgeServerManager.addEdgeServer(edgeA);
        edgeServerManager.addEdgeServer(edgeB);
    }

    public RequestTraceDTO handleClientRequest(
            String clientId,
            String resourceId,
            String url
    ) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException(
                    "Client ID is required."
            );
        }

        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException(
                    "Resource ID is required."
            );
        }

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                    "URL is required."
            );
        }

        List<HopDTO> trace = new ArrayList<>();

        Resource resource =
                cdnService.fetchResource(
                        resourceId,
                        trace
                );

        if (resource == null) {
            throw new IllegalArgumentException(
                    "Resource not found: " + resourceId
            );
        }

        List<HopDTO> immutableTrace =
                List.copyOf(trace);

        boolean servedFromCache =
                immutableTrace.stream()
                        .anyMatch(HopDTO::hit);

        boolean hitOnEdge =
                !immutableTrace.isEmpty()
                        && "EDGE".equals(
                        immutableTrace
                                .getFirst()
                                .level()
                )
                        && immutableTrace
                        .getFirst()
                        .hit();

        ClientRequest request =
                new ClientRequest();

        request.setClientId(clientId);
        request.setResourceId(resourceId);
        request.setUrl(url);
        request.setTimestamp(
                Instant.now().toEpochMilli()
        );
        request.setCached(servedFromCache);
        request.setResourcePath(
                resource.getResourcePath()
        );

        requestLog.add(request);

        return new RequestTraceDTO(
                resourceId,
                url,
                hitOnEdge,
                immutableTrace,
                resource.getResourcePath()
        );
    }

    public List<ClientRequest> getRequestLog() {
        return List.copyOf(requestLog);
    }
}