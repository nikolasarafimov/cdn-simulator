package mk.ukim.finki.cdn_simulatorproject.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Entity
public class ClientRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientID;
    private String resourceId;
    private String url;
    private boolean isCached;
    private long timestamp;
    private String resourcePath;

    public String getResourcePath() {
        return resourcePath;
    }

    public ClientRequest(){}
    public String getClientID() {return clientID;}

    public String getResourceId() {
        return resourceId;
    }

    public String getUrl() {
        return url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setCached(boolean cached) {
        isCached = cached;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public boolean isCached() {
        return isCached;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }
}