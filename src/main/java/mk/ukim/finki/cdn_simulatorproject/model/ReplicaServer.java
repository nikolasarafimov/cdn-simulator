package mk.ukim.finki.cdn_simulatorproject.model;

import jakarta.persistence.*;
import lombok.Data;
import mk.ukim.finki.cdn_simulatorproject.cache.CacheStrategy;
import mk.ukim.finki.cdn_simulatorproject.cache.CachingAlgorithmType;
import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.FIFOAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LFUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
public class ReplicaServer {

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

    public ReplicaServer(String replicaServerId, CacheStrategy cacheStrategy, OriginServer originServer, String location) {
        this.replicaServerId = replicaServerId;
        this.cacheStrategy = cacheStrategy;
        this.originServer = originServer;
        this.countRequests = 0;
        this.location = location;
    }

    public ReplicaServer() {}

    public Resource handleRequest(ClientRequest cr, List<HopDTO> trace){
        countRequests++;
        Resource r = cacheStrategy.getResource(cr.getResourceId());
        boolean hit = r != null;

        trace.add(new HopDTO(replicaServerId,"REPLICA",hit,
                cacheStrategy.snapshot().stream()
                        .map(Resource::getResourceId)
                        .toList()));
        if(!hit){
            r = originServer.getResourceFromOriginServer(cr.getResourceId());
            cacheStrategy.putInCache(r);
        }
        return r;
    }

    public Resource handleRequest(ClientRequest cr){
        return handleRequest(cr, new ArrayList<>());
    }

    @PostLoad
    public void init() {
        switch (this.cachingAlgorithmType) {
            case LRU:
                this.cacheStrategy = new LRUAlgorithm(2);
                break;
            case LFU:
                this.cacheStrategy = new LFUAlgorithm(2);
                break;
            case FIFO:
                this.cacheStrategy = new FIFOAlgorithm(2);
                break;
        }
    }

    public String getReplicaServerId() {
        return replicaServerId;
    }

    public CacheStrategy getCacheStrategy() {
        return cacheStrategy;
    }

    public OriginServer getOriginServer() {
        return originServer;
    }

    public int getCountRequests() {
        return countRequests;
    }

    public void setCountRequests(int countRequests) {
        this.countRequests = countRequests;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setReplicaServerId(String replicaServerId) {
        this.replicaServerId = replicaServerId;
    }

    public void setCachingAlgorithmType(CachingAlgorithmType cachingAlgorithmType) {
        this.cachingAlgorithmType = cachingAlgorithmType;
    }

    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    public void setOriginServer(OriginServer originServer) {
        this.originServer = originServer;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}