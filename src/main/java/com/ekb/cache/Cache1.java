package com.ekb.cache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class Cache1 {
    public static Logger log = LoggerFactory.getLogger(Cache1.class);

    public static void main(String... args) {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("preConfigured",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(10)))
                .build();
        cacheManager.init();

        log.info("Cache initialized");

        Cache<Long, String> preConfigured =
                cacheManager.getCache("preConfigured", Long.class, String.class);

        log.info("Got preConfigured cache");

        Cache<Long, String> myCache = cacheManager.createCache("myCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(10)));

        log.info("Got the cache");

        myCache.put(1L, "da one!");
        String value = myCache.get(1L);

        log.info("1: {}", value);

        value = myCache.get(2L);

        log.info("2: {}", value);

        for (long i = 0; i < 20; i++) {
            myCache.put(i, "da " + i + "!");
        }

        log.info("Get 1L to see if it got removed: {}", myCache.get(1L));

        log.info("Get 15L to see the value: {}", myCache.get(15L));

        Set<Long> keys = new TreeSet<>();

        keys.add(0L);
        keys.add(1L);
        keys.add(2L);
        keys.add(3L);
        keys.add(4L);
        keys.add(5L);
        keys.add(6L);
        keys.add(7L);
        keys.add(8L);
        keys.add(9L);
        keys.add(10L);
        keys.add(11L);
        keys.add(12L);
        keys.add(13L);
        keys.add(14L);
        keys.add(15L);
        keys.add(16L);
        keys.add(17L);
        keys.add(18L);
        keys.add(19L);
        keys.add(20L);

        Map<Long, String> contents = myCache.getAll(keys);

        log.info("Contents returned: {}", contents);

        log.info("Get 18L to see the value: --{}--", myCache.get(18L));

        cacheManager.removeCache("preConfigured");

        log.info("cache removed");

        cacheManager.close();



        log.info("cache closed.");
    }
}
