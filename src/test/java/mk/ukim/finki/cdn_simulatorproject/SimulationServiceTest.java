package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;
import mk.ukim.finki.cdn_simulatorproject.dto.RequestTraceDTO;
import mk.ukim.finki.cdn_simulatorproject.model.ClientRequest;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServerManager;
import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import mk.ukim.finki.cdn_simulatorproject.service.CDNService;
import mk.ukim.finki.cdn_simulatorproject.service.SimulationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimulationServiceTest {

    @Test
    void shouldReturnRequestTraceAndStoreRequestInLog() {
        CDNService cdnService = mock(CDNService.class);

        SimulationService simulationService = new SimulationService(
                cdnService,
                new EdgeServerManager(),
                new OriginServer()
        );

        Resource resource = new Resource(
                "img1",
                "image",
                1024,
                "/img/bali.jpg"
        );

        doAnswer(invocation -> {
            List<HopDTO> trace = invocation.getArgument(1);

            trace.add(new HopDTO(
                    "edge-a",
                    "EDGE",
                    false,
                    List.of()
            ));

            trace.add(new HopDTO(
                    "replica-us-east",
                    "REPLICA",
                    true,
                    List.of("img1")
            ));

            return resource;
        }).when(cdnService)
                .fetchResource(eq("img1"), anyList());

        RequestTraceDTO result =
                simulationService.handleClientRequest(
                        "client-1",
                        "img1",
                        "/img/bali.jpg"
                );

        assertEquals("img1", result.resourceId());
        assertEquals("/img/bali.jpg", result.url());
        assertEquals("/img/bali.jpg", result.resourcePath());

        assertFalse(result.hitOnEdge());

        assertEquals(2, result.trace().size());
        assertEquals("edge-a", result.trace().getFirst().serverId());
        assertFalse(result.trace().getFirst().hit());

        assertEquals(
                "replica-us-east",
                result.trace().get(1).serverId()
        );

        assertTrue(result.trace().get(1).hit());

        List<ClientRequest> requestLog =
                simulationService.getRequestLog();

        assertEquals(1, requestLog.size());

        ClientRequest loggedRequest =
                requestLog.getFirst();

        assertEquals(
                "client-1",
                loggedRequest.getClientId()
        );

        assertEquals(
                "img1",
                loggedRequest.getResourceId()
        );

        assertEquals(
                "/img/bali.jpg",
                loggedRequest.getUrl()
        );

        assertEquals(
                "/img/bali.jpg",
                loggedRequest.getResourcePath()
        );

        assertTrue(loggedRequest.isCached());
        assertTrue(loggedRequest.getTimestamp() > 0);
    }

    @Test
    void shouldMarkRequestAsEdgeHit() {
        CDNService cdnService = mock(CDNService.class);

        SimulationService simulationService = new SimulationService(
                cdnService,
                new EdgeServerManager(),
                new OriginServer()
        );

        Resource resource = new Resource(
                "img1",
                "image",
                1024,
                "/img/bali.jpg"
        );

        doAnswer(invocation -> {
            List<HopDTO> trace = invocation.getArgument(1);

            trace.add(new HopDTO(
                    "edge-a",
                    "EDGE",
                    true,
                    List.of("img1")
            ));

            return resource;
        }).when(cdnService)
                .fetchResource(eq("img1"), anyList());

        RequestTraceDTO result =
                simulationService.handleClientRequest(
                        "client-1",
                        "img1",
                        "/img/bali.jpg"
                );

        assertTrue(result.hitOnEdge());
        assertEquals(1, result.trace().size());
        assertTrue(result.trace().getFirst().hit());
    }

    @Test
    void shouldKeepRequestTracesIndependent() {
        CDNService cdnService = mock(CDNService.class);

        SimulationService simulationService = new SimulationService(
                cdnService,
                new EdgeServerManager(),
                new OriginServer()
        );

        Resource resource1 = new Resource(
                "img1",
                "image",
                1024,
                "/img/bali.jpg"
        );

        Resource resource2 = new Resource(
                "img2",
                "image",
                1024,
                "/img/bike.jpg"
        );

        doAnswer(invocation -> {
            List<HopDTO> trace = invocation.getArgument(1);

            trace.add(new HopDTO(
                    "edge-a",
                    "EDGE",
                    true,
                    List.of("img1")
            ));

            return resource1;
        }).when(cdnService)
                .fetchResource(eq("img1"), anyList());

        doAnswer(invocation -> {
            List<HopDTO> trace = invocation.getArgument(1);

            trace.add(new HopDTO(
                    "edge-b",
                    "EDGE",
                    false,
                    List.of()
            ));

            trace.add(new HopDTO(
                    "replica-eu-west",
                    "REPLICA",
                    false,
                    List.of()
            ));

            return resource2;
        }).when(cdnService)
                .fetchResource(eq("img2"), anyList());

        RequestTraceDTO firstResult =
                simulationService.handleClientRequest(
                        "client-1",
                        "img1",
                        "/img/bali.jpg"
                );

        RequestTraceDTO secondResult =
                simulationService.handleClientRequest(
                        "client-2",
                        "img2",
                        "/img/bike.jpg"
                );

        assertEquals(1, firstResult.trace().size());
        assertEquals(
                "edge-a",
                firstResult.trace().getFirst().serverId()
        );

        assertEquals(2, secondResult.trace().size());
        assertEquals(
                "edge-b",
                secondResult.trace().getFirst().serverId()
        );

        assertEquals(
                "replica-eu-west",
                secondResult.trace().get(1).serverId()
        );

        assertEquals(
                2,
                simulationService.getRequestLog().size()
        );
    }

    @Test
    void shouldThrowWhenResourceDoesNotExist() {
        CDNService cdnService = mock(CDNService.class);

        SimulationService simulationService = new SimulationService(
                cdnService,
                new EdgeServerManager(),
                new OriginServer()
        );

        when(
                cdnService.fetchResource(
                        eq("unknown"),
                        anyList()
                )
        ).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> simulationService.handleClientRequest(
                        "client-1",
                        "unknown",
                        "/img/unknown.jpg"
                )
        );
    }

    @Test
    void shouldRejectInvalidRequestData() {
        CDNService cdnService = mock(CDNService.class);

        SimulationService simulationService = new SimulationService(
                cdnService,
                new EdgeServerManager(),
                new OriginServer()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> simulationService.handleClientRequest(
                        "",
                        "img1",
                        "/img/bali.jpg"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> simulationService.handleClientRequest(
                        "client-1",
                        "",
                        "/img/bali.jpg"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> simulationService.handleClientRequest(
                        "client-1",
                        "img1",
                        ""
                )
        );
    }
}