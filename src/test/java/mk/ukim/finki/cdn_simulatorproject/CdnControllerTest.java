package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;
import mk.ukim.finki.cdn_simulatorproject.dto.RequestTraceDTO;
import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import mk.ukim.finki.cdn_simulatorproject.service.CDNService;
import mk.ukim.finki.cdn_simulatorproject.service.SimulationService;
import mk.ukim.finki.cdn_simulatorproject.web.CdnController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CdnControllerTest {

    private CDNService cdnService;
    private OriginServer originServer;
    private SimulationService simulationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cdnService = mock(CDNService.class);
        originServer = mock(OriginServer.class);
        simulationService = mock(SimulationService.class);

        CdnController controller = new CdnController(
                cdnService,
                originServer,
                simulationService
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void shouldReturnAvailableResources() throws Exception {
        when(originServer.listAll()).thenReturn(
                List.of(
                        new Resource(
                                "img1",
                                "image",
                                1024,
                                "/img/bali.jpg"
                        ),
                        new Resource(
                                "img2",
                                "image",
                                1024,
                                "/img/bike.jpg"
                        )
                )
        );

        mockMvc.perform(
                        get("/api/cdn/resources")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$[0].id").value("img1"))
                .andExpect(jsonPath("$[0].name").value("Bali Beach"))
                .andExpect(jsonPath("$[0].type").value("image"))
                .andExpect(jsonPath("$[0].path").value("/img/bali.jpg"))
                .andExpect(jsonPath("$[1].id").value("img2"))
                .andExpect(jsonPath("$[1].name").value("Mountain Bike Photo"));
    }

    @Test
    void shouldReturnFetchedResource() throws Exception {
        Resource resource = new Resource(
                "img1",
                "image",
                1024,
                "/img/bali.jpg"
        );

        when(cdnService.fetchResource("img1"))
                .thenReturn(resource);

        mockMvc.perform(
                        get("/api/cdn/fetchResource/img1")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceId").value("img1"))
                .andExpect(jsonPath("$.resourceType").value("image"))
                .andExpect(jsonPath("$.resourceSize").value(1024))
                .andExpect(jsonPath("$.resourcePath").value("/img/bali.jpg"));

        verify(cdnService)
                .fetchResource("img1");
    }

    @Test
    void shouldReturnNotFoundForUnknownResource() throws Exception {
        when(cdnService.fetchResource("unknown"))
                .thenReturn(null);

        mockMvc.perform(
                        get("/api/cdn/fetchResource/unknown")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSimulateClientRequestAndReturnTrace() throws Exception {
        RequestTraceDTO trace = new RequestTraceDTO(
                "img1",
                "/img/bali.jpg",
                false,
                List.of(
                        new HopDTO(
                                "edge-a",
                                "EDGE",
                                false,
                                List.of()
                        ),
                        new HopDTO(
                                "replica-us-east",
                                "REPLICA",
                                true,
                                List.of("img1")
                        )
                ),
                "/img/bali.jpg"
        );

        when(
                simulationService.handleClientRequest(
                        "client-1",
                        "img1",
                        "/img/bali.jpg"
                )
        ).thenReturn(trace);

        mockMvc.perform(
                        post("/api/cdn/clientRequest")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "clientId": "client-1",
                                          "resourceId": "img1",
                                          "url": "/img/bali.jpg"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceId").value("img1"))
                .andExpect(jsonPath("$.url").value("/img/bali.jpg"))
                .andExpect(jsonPath("$.hitOnEdge").value(false))
                .andExpect(jsonPath("$.resourcePath").value("/img/bali.jpg"))
                .andExpect(jsonPath("$.trace.length()").value(2))
                .andExpect(jsonPath("$.trace[0].serverId").value("edge-a"))
                .andExpect(jsonPath("$.trace[0].level").value("EDGE"))
                .andExpect(jsonPath("$.trace[0].hit").value(false))
                .andExpect(jsonPath("$.trace[1].serverId").value("replica-us-east"))
                .andExpect(jsonPath("$.trace[1].level").value("REPLICA"))
                .andExpect(jsonPath("$.trace[1].hit").value(true));

        verify(simulationService)
                .handleClientRequest(
                        "client-1",
                        "img1",
                        "/img/bali.jpg"
                );
    }
}