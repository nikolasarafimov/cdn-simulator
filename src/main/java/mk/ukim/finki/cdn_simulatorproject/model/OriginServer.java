package mk.ukim.finki.cdn_simulatorproject.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.*;

@Data
@Component
public class OriginServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Transient
    private final Map<String, Resource> resourceMap;

    public OriginServer() {
        this.resourceMap = new HashMap<>();

        resourceMap.put("img1", new Resource("img1", "image", 1024, "/img/bali.jpg"));
        resourceMap.put("img2", new Resource("img2", "image", 1024, "/img/bike.jpg"));
        resourceMap.put("img3", new Resource("img3", "image", 1024, "/img/dolphins.jpg"));
        resourceMap.put("img4", new Resource("img4", "image", 1024, "/img/bird.jpg"));
        resourceMap.put("img5", new Resource("img5", "image", 1024, "/img/tigers.jpg"));
        resourceMap.put("img6", new Resource("img6", "image", 1024, "/img/city.jpeg"));
    }

    public Resource getResourceFromOriginServer(String resourceId){

        if(resourceMap.containsKey(resourceId)){
            return resourceMap.get(resourceId);
        }
        return new Resource(resourceId, "type unknown", 1024, "/img/dolphins.jpg");
    }

    public void addResource(String resourceId, String resourceType, long resourceSize, String resourcePath) {
        resourceMap.put(resourceId, new Resource(resourceId, resourceType, resourceSize, resourcePath));
    }

    public Long getId() {
        return id;
    }

    public Map<String, Resource> getResourceMap() {
        return Collections.unmodifiableMap(resourceMap);
    }

    public List<Resource> listAll() {
        return List.copyOf(resourceMap.values());
    }
}