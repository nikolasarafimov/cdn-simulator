package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;
import mk.ukim.finki.cdn_simulatorproject.model.ClientRequest;
import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplicaServerTest {

    @Test
    void shouldFetchFromOriginAndCacheResourceOnFirstRequest() {
        OriginServer originServer = new OriginServer();

        ReplicaServer replicaServer = new ReplicaServer(
                "Replica1",
                new LRUAlgorithm(2),
                originServer,
                "eu-west-1"
        );

        ClientRequest request = new ClientRequest();
        request.setResourceId("img1");

        List<HopDTO> trace = new ArrayList<>();

        Resource response = replicaServer.handleRequest(
                request,
                trace
        );

        assertNotNull(response);
        assertEquals("img1", response.getResourceId());

        Resource cachedResource =
                replicaServer.getCacheStrategy()
                        .getResource("img1");

        assertNotNull(cachedResource);
        assertSame(response, cachedResource);

        assertEquals(1, replicaServer.getCountRequests());

        assertEquals(1, trace.size());

        HopDTO hop = trace.getFirst();

        assertEquals("Replica1", hop.serverId());
        assertEquals("REPLICA", hop.level());
        assertFalse(hop.hit());
    }

    @Test
    void shouldServeSecondRequestFromReplicaCache() {
        OriginServer originServer = new OriginServer();

        ReplicaServer replicaServer = new ReplicaServer(
                "Replica1",
                new LRUAlgorithm(2),
                originServer,
                "eu-west-1"
        );

        ClientRequest request = new ClientRequest();
        request.setResourceId("img1");

        Resource firstResponse =
                replicaServer.handleRequest(request);

        List<HopDTO> secondTrace = new ArrayList<>();

        Resource secondResponse =
                replicaServer.handleRequest(
                        request,
                        secondTrace
                );

        assertSame(firstResponse, secondResponse);

        assertEquals(2, replicaServer.getCountRequests());

        assertEquals(1, secondTrace.size());

        HopDTO hop = secondTrace.getFirst();

        assertEquals("Replica1", hop.serverId());
        assertEquals("REPLICA", hop.level());
        assertTrue(hop.hit());
    }

    @Test
    void shouldReturnNullForUnknownResource() {
        OriginServer originServer = new OriginServer();

        ReplicaServer replicaServer = new ReplicaServer(
                "Replica1",
                new LRUAlgorithm(2),
                originServer,
                "eu-west-1"
        );

        ClientRequest request = new ClientRequest();
        request.setResourceId("unknown");

        List<HopDTO> trace = new ArrayList<>();

        Resource response = replicaServer.handleRequest(
                request,
                trace
        );

        assertNull(response);

        assertNull(
                replicaServer.getCacheStrategy()
                        .getResource("unknown")
        );

        assertEquals(1, replicaServer.getCountRequests());
        assertEquals(1, trace.size());
        assertFalse(trace.getFirst().hit());
    }
}