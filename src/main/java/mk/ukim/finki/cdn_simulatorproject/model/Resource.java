package mk.ukim.finki.cdn_simulatorproject.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Resource {

    @Id
    private String resourceId;
    private String resourceType;
    private long resourceSize;
    private String resourcePath;

    private boolean resourceIsCached;

    public Resource(String resourceId, String resourceType, long resourceSize , String resourcePath) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.resourceSize = resourceSize;
        this.resourcePath = resourcePath;
    }

    public Resource() {}

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public long getResourceSize() {
        return resourceSize;
    }

    public boolean isResourceIsCached(){
        return resourceIsCached;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}