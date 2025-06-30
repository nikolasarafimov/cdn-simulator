package mk.ukim.finki.cdn_simulatorproject.cache;

import mk.ukim.finki.cdn_simulatorproject.model.ClientRequest;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;

import java.util.Collection;
import java.util.List;

public interface CacheStrategy{
    void removeFromCache(ClientRequest clientRequest);
    void clearCache();
    void putInCache(Resource resource);
    boolean isCacheFull();
    Resource getResource(String resourceId);
    default Collection<Resource> snapshot() {
        return List.of();
    }
}
