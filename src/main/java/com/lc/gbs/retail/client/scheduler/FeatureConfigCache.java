package com.lc.gbs.retail.client.scheduler;

import com.lc.gbs.retail.client.service.RetailConfigService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class FeatureConfigCache {

    private static final Logger log = LogManager.getLogger(FeatureConfigCache.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RetailConfigService retailConfigService;

    /**
     * refreshing the feature config data for every 5 min's this value been configured on parent services
     */
    @Scheduled(cron = "${renew.featureconfig.shoplist.cache.expression}")
    public void refreshFeatureData() {
        log.info("Entering to refresh feature config shop list");
        evictCachedFeatureData(Arrays.asList("configCache"));
        retailConfigService.getFeatureConfigResponse();
    }



    /**
     * evict all the caches
     *
     *  @param cacheToDelete specific cache to delete
     */
    public void evictCachedFeatureData(List<String> cacheToDelete) {
        final Collection<String> totalCaches = cacheManager.getCacheNames();
        if (totalCaches.size() > 0) {
            log.info("Entering to evictAllCaches with cache size {}", totalCaches.size());
            totalCaches.forEach(specificCache -> {
                final Cache cache = cacheManager.getCache(specificCache);
                final String cacheName = cache != null ? cache.getName() : "";
                log.info("Entering to evictAllCaches for {} ", cacheName);
                if (cache != null && cacheToDelete.contains(cacheName)) {
                    cache.clear();
                }
            });
        } else {
            log.info("There is no cache to delete for {} ", cacheToDelete);
        }
    }
}