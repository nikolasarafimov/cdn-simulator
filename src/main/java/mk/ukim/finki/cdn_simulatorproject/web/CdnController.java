package mk.ukim.finki.cdn_simulatorproject.web;

import mk.ukim.finki.cdn_simulatorproject.dto.ClientRequestDTO;
import mk.ukim.finki.cdn_simulatorproject.dto.RequestTraceDTO;
import mk.ukim.finki.cdn_simulatorproject.dto.ResourceDTO;
import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import mk.ukim.finki.cdn_simulatorproject.service.CDNService;
import mk.ukim.finki.cdn_simulatorproject.service.SimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cdn")
public class CdnController {

    private final CDNService cdnService;
    private final OriginServer originServer;
    private final SimulationService simulationService;

    public CdnController(
            CDNService cdnService,
            OriginServer originServer,
            SimulationService simulationService
    ) {
        this.cdnService = cdnService;
        this.originServer = originServer;
        this.simulationService = simulationService;
    }

    @GetMapping("/fetchResource/{resourceId}")
    public ResponseEntity<Resource> fetchResource(
            @PathVariable String resourceId
    ) {
        Resource resource =
                cdnService.fetchResource(resourceId);

        if (resource == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(resource);
    }

    @PostMapping("/clientRequest")
    public RequestTraceDTO handleClientRequest(
            @RequestBody ClientRequestDTO dto
    ) {
        return simulationService.handleClientRequest(
                dto.clientId(),
                dto.resourceId(),
                dto.url()
        );
    }

    @GetMapping("/resources")
    public List<ResourceDTO> listResources() {
        return originServer.listAll()
                .stream()
                .map(ResourceDTO::from)
                .toList();
    }
}