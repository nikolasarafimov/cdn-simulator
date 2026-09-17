package mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms;

import mk.ukim.finki.cdn_simulatorproject.cache.CacheStrategy;
import mk.ukim.finki.cdn_simulatorproject.model.ClientRequest;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LFUAlgorithm implements CacheStrategy {

    private final int capacity;
    private final Map<String, Resource> resourceMap;
    private final Map<String, Integer> frequencies;

    public LFUAlgorithm(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Cache capacity must be greater than zero.");
        }

        this.capacity = capacity;
        this.resourceMap = new LinkedHashMap<>();
        this.frequencies = new LinkedHashMap<>();
    }

    @Override
    public void removeFromCache(ClientRequest clientRequest) {
        if (clientRequest == null) {
            return;
        }

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
        if (resource == null || resource.getResourceId() == null) {
            return;
        }

        String resourceId = resource.getResourceId();

        if (resourceMap.containsKey(resourceId)) {
            frequencies.merge(resourceId, 1, Integer::sum);
            return;
        }

        if (isCacheFull()) {
            removeLeastFrequentlyUsed();
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
        if (resourceId == null) {
            return null;
        }

        Resource resource = resourceMap.get(resourceId);

        if (resource != null) {
            frequencies.merge(resourceId, 1, Integer::sum);
        }

        return resource;
    }

    private void removeLeastFrequentlyUsed() {
        String resourceToRemove = null;
        int lowestFrequency = Integer.MAX_VALUE;

        for (String resourceId : resourceMap.keySet()) {
            int frequency = frequencies.getOrDefault(resourceId, 0);

            if (frequency < lowestFrequency) {
                lowestFrequency = frequency;
                resourceToRemove = resourceId;
            }
        }

        if (resourceToRemove != null) {
            resourceMap.remove(resourceToRemove);
            frequencies.remove(resourceToRemove);
        }
    }
}