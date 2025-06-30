package mk.ukim.finki.cdn_simulatorproject.dto;

public record ClientRequestDTO(
        String clientId,
        String resourceId,
        String url
) {}