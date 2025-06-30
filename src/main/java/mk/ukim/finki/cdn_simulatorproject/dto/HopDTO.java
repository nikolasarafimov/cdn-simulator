package mk.ukim.finki.cdn_simulatorproject.dto;

import java.util.List;

public record HopDTO(
        String serverId,
        String level,
        boolean hit,
        List<String> cache
) {}