package mk.ukim.finki.cdn_simulatorproject.service.impl;

import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServerManager;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServerManager;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import mk.ukim.finki.cdn_simulatorproject.service.CDNService;

import mk.ukim.finki.cdn_simulatorproject.service.CacheService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CDNServiceImpl implements CDNService {

    private final EdgeServerManager edgeManager;
    private final ReplicaServerManager replicaManager;
    private final CacheService cacheService;

    public CDNServiceImpl(EdgeServerManager edgeManager, ReplicaServerManager replicaManager, CacheService cacheService) {
        this.edgeManager   = edgeManager;
        this.replicaManager = replicaManager;
        this.cacheService = cacheService;
    }

    @Override
    public Resource fetchResource(String id,List<HopDTO> tr){
        return edgeManager.routeRequest(id,tr);
    }
    @Override
    public Resource fetchResource(String id){
        return fetchResource(id,new ArrayList<>());
    }

    @Override
    public void addReplicaServer(ReplicaServer replicaServer) {
        replicaManager.addReplicaServer(replicaServer);
    }

    @Override
    public void removeReplicaServer(ReplicaServer replicaServer) {
        replicaManager.removeReplicaServer(replicaServer);
    }

    @Override
    public void clearCache() {
        cacheService.clearCache();
    }
}