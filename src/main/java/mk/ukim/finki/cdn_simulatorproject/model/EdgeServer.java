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
import java.util.List;

@Data
@Entity
public class EdgeServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String edgeServerId;

    @Enumerated(EnumType.STRING)
    private CachingAlgorithmType cachingAlgorithmType;

    @Transient
    private CacheStrategy cacheStrategy;
    @ManyToOne
    private ReplicaServer replicaServer;
    private int requestCount;

    public EdgeServer() {}

    public EdgeServer(String edgeServerId, CacheStrategy cacheStrategy, ReplicaServer replicaServer){
        this.edgeServerId = edgeServerId;
        this.cacheStrategy = cacheStrategy;
        this.replicaServer = replicaServer;
    }

    public Resource handleRequest(ClientRequest cr, List<HopDTO> trace){
        requestCount++;
        Resource r = cacheStrategy.getResource(cr.getResourceId());
        boolean hit = r != null;

        trace.add(new HopDTO(edgeServerId,"EDGE",hit,
                cacheStrategy.snapshot().stream()
                        .map(Resource::getResourceId)
                        .toList()));

        if(!hit){
            r = replicaServer.handleRequest(cr, trace);
            cacheStrategy.putInCache(r);
        }
        return r;
    }

    public Resource handleRequests(ClientRequest cr){
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

    public String getEdgeServerId() {
        return edgeServerId;
    }

    public CacheStrategy getCacheStrategy() {
        return cacheStrategy;
    }

    public ReplicaServer getReplicaServer() {
        return replicaServer;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
    public void setEdgeServerId(String edgeServerId) {
        this.edgeServerId = edgeServerId;
    }

    public void setCachingAlgorithmType(CachingAlgorithmType cachingAlgorithmType) {
        this.cachingAlgorithmType = cachingAlgorithmType;
    }

    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    public void setReplicaServer(ReplicaServer replicaServer) {
        this.replicaServer = replicaServer;
    }
}