package mk.ukim.finki.cdn_simulatorproject.service.impl;

import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServerManager;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import mk.ukim.finki.cdn_simulatorproject.service.CDNService;
import mk.ukim.finki.cdn_simulatorproject.service.CacheService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CDNServiceImpl implements CDNService {

    private final EdgeServerManager edgeServerManager;
    private final CacheService cacheService;

    public CDNServiceImpl(
            EdgeServerManager edgeServerManager,
            CacheService cacheService
    ) {
        this.edgeServerManager = edgeServerManager;
        this.cacheService = cacheService;
    }

    @Override
    public Resource fetchResource(
            String resourceId,
            List<HopDTO> trace
    ) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("Resource ID is required.");
        }

        if (trace == null) {
            throw new IllegalArgumentException("Request trace is required.");
        }

        return edgeServerManager.routeRequest(resourceId, trace);
    }

    @Override
    public Resource fetchResource(String resourceId) {
        return fetchResource(resourceId, new ArrayList<>());
    }

    @Override
    public void addReplicaServer(ReplicaServer replicaServer) {
        cacheService.addReplicaServer(replicaServer);
    }

    @Override
    public void removeReplicaServer(ReplicaServer replicaServer) {
        cacheService.removeReplicaServer(replicaServer);
    }

    @Override
    public void clearCache() {
        cacheService.clearCache();
    }
}