package mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms;

import mk.ukim.finki.cdn_simulatorproject.cache.CacheStrategy;
import mk.ukim.finki.cdn_simulatorproject.model.ClientRequest;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LRUAlgorithm implements CacheStrategy {

    private final int capacity;
    private final LinkedHashMap<String, Resource> lruMap;

    public LRUAlgorithm(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Cache capacity must be greater than zero.");
        }

        this.capacity = capacity;
        this.lruMap = new LinkedHashMap<String, Resource>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Resource> eldest) {
                return size() > LRUAlgorithm.this.capacity;
            }
        };
    }

    @Override
    public void removeFromCache(ClientRequest clientRequest) {
        if (clientRequest == null) {
            return;
        }

        lruMap.remove(clientRequest.getResourceId());
    }

    @Override
    public void clearCache() {
        lruMap.clear();
    }

    @Override
    public void putInCache(Resource resource) {
        if (resource == null || resource.getResourceId() == null) {
            return;
        }

        lruMap.put(resource.getResourceId(), resource);
    }

    @Override
    public Collection<Resource> snapshot() {
        return List.copyOf(lruMap.values());
    }

    @Override
    public boolean isCacheFull() {
        return lruMap.size() >= capacity;
    }

    @Override
    public Resource getResource(String resourceId) {
        if (resourceId == null) {
            return null;
        }

        return lruMap.get(resourceId);
    }
}