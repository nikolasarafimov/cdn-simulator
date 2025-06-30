package mk.ukim.finki.cdn_simulatorproject.dto;

import java.util.List;

public record RequestTraceDTO(
        String resourceId,
        String url,
        boolean hitOnEdge,
        List<HopDTO> trace,
        String resourcePath
) {}