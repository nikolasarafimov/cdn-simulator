package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.FIFOAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EdgeServerTest {

    @Test
    void testEdgeServerCacheHitAndMiss() {
        OriginServer origin = new OriginServer();
        LRUAlgorithm lru = new LRUAlgorithm(2);
        ReplicaServer replicaServer = new ReplicaServer("Replica1", lru, origin,"eu-west-1");

        FIFOAlgorithm edgeCache = new FIFOAlgorithm(2);
        EdgeServer edgeServer = new EdgeServer("Edge1", edgeCache, replicaServer);

        ClientRequest request = new ClientRequest();
        request.setResourceId("resourceNum1");

        Resource resource1 = edgeServer.handleRequests(request);
        assertNotNull(resource1, "Should fetch from ReplicaServer/Origin on first request");
        assertEquals("resourceNum1", resource1.getResourceId());

        Resource resource2 = edgeServer.handleRequests(request);
        assertEquals(resource1, resource2, "Should come from EdgeServer cache on the second request");
    }
}