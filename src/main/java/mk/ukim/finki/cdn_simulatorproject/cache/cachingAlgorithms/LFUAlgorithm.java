package mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms;

import mk.ukim.finki.cdn_simulatorproject.cache.CacheStrategy;
import mk.ukim.finki.cdn_simulatorproject.cache.CachingAlgorithmType;
import mk.ukim.finki.cdn_simulatorproject.model.ClientRequest;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LFUAlgorithm implements CacheStrategy {
    private final int capacity;
    private final Map<String, Resource> resourceMap;
    private final Map<String, Integer> frequencies;
    private CachingAlgorithmType cachingAlgorithmType;

    public LFUAlgorithm(int capacity) {
        this.capacity = capacity;
        this.cachingAlgorithmType = CachingAlgorithmType.LFU;
        this.resourceMap = new HashMap<>();
        this.frequencies = new HashMap<>();
    }

    @Override
    public void removeFromCache(ClientRequest clientRequest) {
        String resourceId = clientRequest.getResourceId();
        resourceMap.remove(resourceId);
        frequencies.remove(resourceId);
    }

    @Override
    public void clearCache() {
        resourceMap.clear();
        frequencies.clear();
    }

    @Override
    public boolean isCacheFull() {
        return resourceMap.size() >= capacity;
    }

    @Override
    public void putInCache(Resource resource) {
        String resourceId = resource.getResourceId();

        if (resourceMap.containsKey(resourceId)) {
            frequencies.put(resourceId, frequencies.get(resourceId) + 1);
            return;
        }

        if (isCacheFull()) {
            lfuRemoval();
        }

        resourceMap.put(resourceId, resource);
        frequencies.put(resourceId, 1);
    }

    @Override
    public Collection<Resource> snapshot() {
        return List.copyOf(resourceMap.values());
    }

    @Override
    public Resource getResource(String resourceId) {
        Resource resource = resourceMap.get(resourceId);
        if (resource != null) {
            frequencies.put(resourceId, frequencies.get(resourceId) + 1);
        }
        return resource;
    }

    private void lfuRemoval() {
        frequencies.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .ifPresent(entry -> {
                    String key = entry.getKey();
                    resourceMap.remove(key);
                    frequencies.remove(key);
                });
    }
}