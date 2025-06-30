package mk.ukim.finki.cdn_simulatorproject.service.impl;

import mk.ukim.finki.cdn_simulatorproject.model.*;
import mk.ukim.finki.cdn_simulatorproject.service.CacheService;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceImpl implements CacheService {

    private final EdgeServerManager edgeServerManager;
    private final ReplicaServerManager replicaServerManager;
    private final OriginServer originServer;

    public CacheServiceImpl(EdgeServerManager edgeServerManager, ReplicaServerManager replicaServerManager, OriginServer originServer) {
        this.edgeServerManager = edgeServerManager;
        this.replicaServerManager = replicaServerManager;
        this.originServer = originServer;
    }

    @Override
    public Resource fetchResource(String resourceId) {
        EdgeServer edgeServer = edgeServerManager.getTheLeastLoadedReplicaServer();
        Resource resource = edgeServer.getCacheStrategy().getResource(resourceId);

        if (resource != null) {
            System.out.println("[EdgeServer " + edgeServer.getEdgeServerId() + "] Cache HIT for: " + resourceId);
            edgeServer.setRequestCount(edgeServer.getRequestCount() + 1);
            return resource;
        }

        ReplicaServer replicaServer = replicaServerManager.getTheLeastLoadedReplicaServer();
        resource = replicaServer.getCacheStrategy().getResource(resourceId);

        if (resource != null) {
            System.out.println("[ReplicaServer " + replicaServer.getReplicaServerId() + "] Cache HIT for: " + resourceId);
            replicaServer.
                    setCountRequests(replicaServer.getCountRequests() + 1);

            edgeServer.getCacheStrategy().putInCache(resource);

            return resource;
        }

        System.out.println("Resource " + resourceId + " not found in cache. Fetching from Origin...");
        resource = originServer.getResourceFromOriginServer(resourceId);
        edgeServer.getCacheStrategy().putInCache(resource);
        replicaServer.getCacheStrategy().putInCache(resource);

        return resource;
    }

    @Override
    public void clearCache() {

        edgeServerManager.getEdgeServerList().stream()
                .forEach(edgeServer -> edgeServer.getCacheStrategy().clearCache());

        replicaServerManager.getReplicaServerList().stream()
                .forEach(replicaServer -> replicaServer.getCacheStrategy().clearCache());
    }

    public void addReplicaServer(ReplicaServer replicaServer) {
        replicaServerManager.addReplicaServer(replicaServer);
    }

    public void removeReplicaServer(ReplicaServer replicaServer) {
        replicaServerManager.removeReplicaServer(replicaServer);
    }
}