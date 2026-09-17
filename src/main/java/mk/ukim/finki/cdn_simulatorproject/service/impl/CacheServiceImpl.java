package mk.ukim.finki.cdn_simulatorproject.service.impl;

import mk.ukim.finki.cdn_simulatorproject.model.EdgeServerManager;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServerManager;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import mk.ukim.finki.cdn_simulatorproject.service.CacheService;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceImpl implements CacheService {

    private final EdgeServerManager edgeServerManager;
    private final ReplicaServerManager replicaServerManager;

    public CacheServiceImpl(
            EdgeServerManager edgeServerManager,
            ReplicaServerManager replicaServerManager
    ) {
        this.edgeServerManager = edgeServerManager;
        this.replicaServerManager = replicaServerManager;
    }

    @Override
    public Resource fetchResource(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("Resource ID is required.");
        }

        return edgeServerManager.routeRequest(resourceId);
    }

    @Override
    public void clearCache() {
        edgeServerManager.getEdgeServerList()
                .forEach(edgeServer ->
                        edgeServer.getCacheStrategy().clearCache());

        replicaServerManager.getReplicaServerList()
                .forEach(replicaServer ->
                        replicaServer.getCacheStrategy().clearCache());
    }

    @Override
    public void addReplicaServer(ReplicaServer replicaServer) {
        replicaServerManager.addReplicaServer(replicaServer);
    }

    @Override
    public void removeReplicaServer(ReplicaServer replicaServer) {
        replicaServerManager.removeReplicaServer(replicaServer);
    }
}