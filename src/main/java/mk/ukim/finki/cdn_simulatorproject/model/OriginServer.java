package mk.ukim.finki.cdn_simulatorproject.model;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OriginServer {

    private final Map<String, Resource> resourceMap;

    public OriginServer() {
        this.resourceMap = new LinkedHashMap<>();

        addResource("img1", "image", 1024, "/img/bali.jpg");
        addResource("img2", "image", 1024, "/img/bike.jpg");
        addResource("img3", "image", 1024, "/img/dolphins.jpg");
        addResource("img4", "image", 1024, "/img/bird.jpg");
        addResource("img5", "image", 1024, "/img/tigers.jpg");
        addResource("img6", "image", 1024, "/img/city.jpeg");
    }

    public Resource getResourceFromOriginServer(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) {
            return null;
        }

        return resourceMap.get(resourceId);
    }

    public void addResource(
            String resourceId,
            String resourceType,
            long resourceSize,
            String resourcePath
    ) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("Resource ID is required.");
        }

        if (resourceType == null || resourceType.isBlank()) {
            throw new IllegalArgumentException("Resource type is required.");
        }

        if (resourceSize < 0) {
            throw new IllegalArgumentException("Resource size cannot be negative.");
        }

        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("Resource path is required.");
        }

        resourceMap.put(
                resourceId,
                new Resource(
                        resourceId,
                        resourceType,
                        resourceSize,
                        resourcePath
                )
        );
    }

    public Map<String, Resource> getResourceMap() {
        return Collections.unmodifiableMap(resourceMap);
    }

    public List<Resource> listAll() {
        return List.copyOf(resourceMap.values());
    }
}