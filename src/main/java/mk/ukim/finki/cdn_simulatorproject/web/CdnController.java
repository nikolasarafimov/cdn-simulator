package mk.ukim.finki.cdn_simulatorproject.web;

import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;
import mk.ukim.finki.cdn_simulatorproject.model.ClientRequest;
import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import mk.ukim.finki.cdn_simulatorproject.service.CDNService;
import mk.ukim.finki.cdn_simulatorproject.service.SimulationService;
import org.springframework.web.bind.annotation.*;
import mk.ukim.finki.cdn_simulatorproject.dto.ResourceDTO;
import java.util.List;

@RestController
@RequestMapping("/api/cdn")
public class CdnController {

    private final CDNService cdnService;
    private final OriginServer originServer;
    private final SimulationService simulationService;

    public CdnController(CDNService cdnService, OriginServer originServer, SimulationService simulationService) {
        this.cdnService = cdnService;
        this.originServer = originServer;
        this.simulationService = simulationService;
    }
    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/fetchResource/{resourceId}")
    public Resource fetchResource(@PathVariable String resourceId) {
        return cdnService.fetchResource(resourceId);
    }

    @PostMapping("/clientRequest")
    public RequestTraceDTO handle(@RequestBody ClientRequestDTO dto){
        ClientRequest cr = simulationService.handleClientRequest(
                dto.clientId(), dto.resourceId(), dto.url());
        return new RequestTraceDTO(
                cr.getResourceId(),
                cr.getUrl(),
                cr.isCached(),
                simulationService.getLastTrace(),
                cr.getResourcePath());
    }

    @GetMapping("/resources")
    public List<ResourceDTO> list(){
        return originServer.listAll()
                .stream()
                .map(ResourceDTO::from)
                .toList();
    }

    public record ClientRequestDTO(String clientId, String resourceId, String url) {}

    public record RequestTraceDTO(
            String resourceId,
            String url,
            boolean hitOnEdge,
            List<HopDTO> trace,
            String resourcePath
    ) {}
}