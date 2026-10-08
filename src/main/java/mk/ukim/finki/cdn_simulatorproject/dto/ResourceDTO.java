package mk.ukim.finki.cdn_simulatorproject.dto;

import mk.ukim.finki.cdn_simulatorproject.model.Resource;

import java.util.Objects;

public record ResourceDTO(
        String id,
        String name,
        String type,
        String path
) {

    public static ResourceDTO from(Resource resource) {
        Objects.requireNonNull(
                resource,
                "Resource is required."
        );

        return new ResourceDTO(
                resource.getResourceId(),
                resolveFriendlyName(resource.getResourceId()),
                resource.getResourceType(),
                resource.getResourcePath()
        );
    }

    private static String resolveFriendlyName(
            String resourceId
    ) {
        return switch (resourceId) {
            case "img1" -> "Bali Beach";
            case "img2" -> "Mountain Bike Photo";
            case "img3" -> "Dolphins Picture";
            case "img4" -> "Bird Picture";
            case "img5" -> "Tigers Photo";
            case "img6" -> "Sydney, Australia";
            default -> resourceId;
        };
    }
}