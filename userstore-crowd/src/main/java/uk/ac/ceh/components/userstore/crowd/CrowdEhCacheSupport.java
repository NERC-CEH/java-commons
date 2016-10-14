package uk.ac.ceh.components.userstore.crowd;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;

/**
 * The following support class enables EhCaching for the UserStore Crowd api.
 * When working with the crowd.
 * @author cjohn
 */
public class CrowdEhCacheSupport {
    
    /**
     * Generate a cache manager which which is fully populated with UserStore 
     * Crowd cache configurations.
     * @return 
     */
    public static CacheManager createCacheManager() {
        return CacheManager.newInstance(createCrowdCacheConfiguration());
    }
    
    /**
     * Produces a fresh ehcache configuration which can be supplied to a cacheManager
     * @return an ehcache configuration for use with the crowd userstore
     */
    public static Configuration createCrowdCacheConfiguration() {
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        addCrowdCacheConfigurations(config);
        return config;
    }
    
    /**
     * Populates the supplied ehcache configuration with the required 
     * configuration to enable caching of crowd rest api responses for frequently
     * used requests.
     * @param config 
     */
    public static void addCrowdCacheConfigurations(Configuration config) {
        // Crowd User caches
        config.addCache(userSpecificCache("crowd-users", 10000));        
        config.addCache(userSpecificCache("crowd-authentication", 10000));
        
        // Crowd Group caches
        config.addCache(userSpecificCache("crowd-usersGroups", 1000)); 
        config.addCache(userSpecificCache("crowd-usersDirectGroups", 1000));
        config.addCache(globalCache("crowd-groups"));
    }
    
    private static CacheConfiguration globalCache(String name) {
        CacheConfiguration cache = new CacheConfiguration();
        cache.setName(name);
        cache.setTimeToIdleSeconds(300);
        cache.setTimeToLiveSeconds(600);
        cache.setMemoryStoreEvictionPolicy("LFU");
        cache.setMaxEntriesLocalHeap(10000);
        return cache;
    }
    
    private static CacheConfiguration userSpecificCache(String name, int maxEntries) {
        CacheConfiguration cache = new CacheConfiguration();
        cache.setName(name);
        cache.setTimeToIdleSeconds(60);
        cache.setTimeToLiveSeconds(120);
        cache.setMemoryStoreEvictionPolicy("LRU");
        cache.setMaxEntriesLocalHeap(maxEntries);
        return cache;
    }    
}
