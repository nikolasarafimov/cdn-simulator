package mk.ukim.finki.cdn_simulatorproject.dto;

import mk.ukim.finki.cdn_simulatorproject.model.Resource;

public record ResourceDTO(
        String id,
        String name,
        String type,
        String path
) {
    public static ResourceDTO from(Resource r) {
        String friendlyName = switch (r.getResourceId()) {
            case "img1" -> "Bali Beach";
            case "img2" -> "Mountain Bike Photo";
            case "img3" -> "Dolphins Picture";
            case "img4" -> "Bird Picture";
            case "img5" -> "Tigers Photo";
            case "img6" -> "Sydney, Australia";
            default -> r.getResourceId();
        };
        return new ResourceDTO(
                r.getResourceId(),
                friendlyName,
                r.getResourceType(),
                r.getResourcePath()
        );
    }
}