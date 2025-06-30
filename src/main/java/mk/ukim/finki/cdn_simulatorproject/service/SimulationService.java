package mk.ukim.finki.cdn_simulatorproject.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import mk.ukim.finki.cdn_simulatorproject.cache.CachingAlgorithmType;
import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;
import mk.ukim.finki.cdn_simulatorproject.model.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SimulationService {

    private final CDNService cdnService;
    private final EdgeServerManager edgeServerManager;
    private final ReplicaServerManager replicaServerManager;
    private final OriginServer originServer;

    private final List<ClientRequest> requestLog = new ArrayList<>();

    public SimulationService(CDNService cdnService, EdgeServerManager edgeServerManager, ReplicaServerManager replicaServerManager, OriginServer originServer) {
        this.cdnService = cdnService;
        this.edgeServerManager = edgeServerManager;
        this.replicaServerManager = replicaServerManager;
        this.originServer = originServer;
    }

    @PostConstruct
    public void init(){
        initializeTopology();
    }

    @Transactional
    public void initializeTopology() {
        ReplicaServer rep1 = new ReplicaServer();
        rep1.setReplicaServerId("replica-us-east");
        rep1.setCachingAlgorithmType(CachingAlgorithmType.LRU);
        rep1.setLocation("us-east-1");
        rep1.setOriginServer(originServer);
        rep1.init();
        cdnService.addReplicaServer(rep1);

        ReplicaServer rep2 = new ReplicaServer();
        rep2.setReplicaServerId("replica-eu-west");
        rep2.setCachingAlgorithmType(CachingAlgorithmType.LFU);
        rep2.setLocation("eu-west-1");
        rep2.setOriginServer(originServer);
        rep2.init();
        cdnService.addReplicaServer(rep2);

        EdgeServer edgeA = new EdgeServer();
        edgeA.setEdgeServerId("edge-a");
        edgeA.setCachingAlgorithmType(CachingAlgorithmType.FIFO);
        edgeA.setReplicaServer(rep1);
        edgeA.init();
        edgeServerManager.addEdgeServer(edgeA);

        EdgeServer edgeB = new EdgeServer();
        edgeB.setEdgeServerId("edge-b");
        edgeB.setCachingAlgorithmType(CachingAlgorithmType.LRU);
        edgeB.setReplicaServer(rep2);
        edgeB.init();
        edgeServerManager.addEdgeServer(edgeB);
    }

    private List<HopDTO> lastTrace = new ArrayList<>();
    public List<HopDTO> getLastTrace(){ return lastTrace; }

    public ClientRequest handleClientRequest(String clientId,String resId,String url){
        List<HopDTO> trace = new ArrayList<>();

        Resource res = cdnService.fetchResource(resId, trace);

        ClientRequest req = new ClientRequest();
        req.setClientID(clientId);
        req.setResourceId(resId);
        req.setUrl(url);
        req.setTimestamp(Instant.now().toEpochMilli());
        req.setCached(res.isResourceIsCached());
        req.setResourcePath(res.getResourcePath());

        requestLog.add(req);
        this.lastTrace = trace;

        return req;
    }

    public List<ClientRequest> getRequestLog() {
        return Collections.unmodifiableList(requestLog);
    }
}