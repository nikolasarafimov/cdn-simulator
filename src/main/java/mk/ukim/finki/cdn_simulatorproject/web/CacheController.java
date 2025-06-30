package mk.ukim.finki.cdn_simulatorproject.web;

import mk.ukim.finki.cdn_simulatorproject.service.CacheService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @DeleteMapping("/clear")
    public void clearCache() {
        cacheService.clearCache();
    }
}