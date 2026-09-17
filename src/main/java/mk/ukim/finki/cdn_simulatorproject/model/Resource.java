package mk.ukim.finki.cdn_simulatorproject.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Resource {

    @Id
    private String resourceId;

    private String resourceType;
    private long resourceSize;
    private String resourcePath;
    private boolean resourceIsCached;

    public Resource(
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

        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.resourceSize = resourceSize;
        this.resourcePath = resourcePath;
    }
}