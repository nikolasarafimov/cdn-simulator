package mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms;

import mk.ukim.finki.cdn_simulatorproject.cache.CacheStrategy;
import mk.ukim.finki.cdn_simulatorproject.cache.CachingAlgorithmType;
import mk.ukim.finki.cdn_simulatorproject.model.ClientRequest;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LRUAlgorithm implements CacheStrategy {
    private final int capacity;
    private final LinkedHashMap<String, Resource> lruMap;
    private CachingAlgorithmType cachingAlgorithmType;


    public LRUAlgorithm(int capacity) {
        this.capacity = capacity;
        this.cachingAlgorithmType = CachingAlgorithmType.LRU;
        this.lruMap = new LinkedHashMap<String, Resource>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Resource> eldest) {
                return size() > capacity;
            }
        };
    }

    @Override
    public void removeFromCache(ClientRequest clientRequest) {
        lruMap.remove(clientRequest.getResourceId());
    }

    @Override
    public void clearCache() {
        lruMap.clear();
    }

    @Override
    public void putInCache(Resource resource) {
        String resourceId = resource.getResourceId();
        lruMap.put(resourceId, resource);
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
        return lruMap.get(resourceId);
    }
}