package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.FIFOAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.ClientRequest;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServer;
import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class EdgeServerTest {

    @Test
    void shouldFetchFromOriginAndThenServeFromEdgeCache() {
        OriginServer originServer = new OriginServer();

        ReplicaServer replicaServer = new ReplicaServer(
                "Replica1",
                new LRUAlgorithm(2),
                originServer,
                "eu-west-1"
        );

        EdgeServer edgeServer = new EdgeServer(
                "Edge1",
                new FIFOAlgorithm(2),
                replicaServer
        );

        ClientRequest request = new ClientRequest();
        request.setResourceId("img1");

        Resource firstResponse =
                edgeServer.handleRequests(request);

        assertNotNull(firstResponse);
        assertEquals(
                "img1",
                firstResponse.getResourceId()
        );

        Resource cachedResource =
                edgeServer.getCacheStrategy()
                        .getResource("img1");

        assertNotNull(cachedResource);

        Resource secondResponse =
                edgeServer.handleRequests(request);

        assertSame(
                cachedResource,
                secondResponse
        );

        assertEquals(
                2,
                edgeServer.getRequestCount()
        );

        assertEquals(
                1,
                replicaServer.getCountRequests()
        );
    }
}