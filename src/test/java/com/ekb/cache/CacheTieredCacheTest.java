package com.ekb.cache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.event.*;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import static org.ehcache.config.builders.ResourcePoolsBuilder.newResourcePoolsBuilder;

public class CacheTieredCacheTest {

    public static class ListenerObject implements CacheEventListener {

        /**
         * Invoked on {@link CacheEvent CacheEvent} firing.
         * <p>
         * This method is invoked according to the
         * {@link EventType} requirements provided at listener registration time.
         * <p>
         * Any exception thrown from this listener will be swallowed and logged but will not prevent other listeners to run.
         *
         * @param event the actual {@code CacheEvent}
         */
        @Override
        public void onEvent(CacheEvent event) {
            log.info("Cache event fired: {}  for key: {}", event.getType(), event.getKey());
        }
    }

    private static CacheManager cacheManager;
    public static Logger log = LoggerFactory.getLogger(CacheTieredCacheTest.class);
    private static CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration;

    @BeforeClass
    public static void setup() {

        cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
                .newEventListenerConfiguration(new ListenerObject(), EventType.EVICTED)
                .unordered().asynchronous();

        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(new File("C:/dev/data/cache", "cache-data")))
                .withCache("tripleTier",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                                newResourcePoolsBuilder().heap(500, EntryUnit.ENTRIES)
                                        .offheap(500, MemoryUnit.MB)
                                        .disk(200, MemoryUnit.GB, true)
                        ).add(cacheEventListenerConfiguration)
                )
                .build();
        cacheManager.init();
    }

    @Test
    public void expireCache() throws Exception {
        ListenerObject lo = new ListenerObject();

        CacheManager cm = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("expires",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                                newResourcePoolsBuilder().heap(500, EntryUnit.ENTRIES)).
                                withExpiry(Expirations.timeToLiveExpiration(Duration.of(45, TimeUnit.SECONDS))
                        ))
                .build();
        cm.init();

        Cache<Long, String> cache =
                cm.getCache("expires", Long.class, String.class);

        cache.getRuntimeConfiguration().registerCacheEventListener(lo, EventOrdering.ORDERED,
                EventFiring.ASYNCHRONOUS, EnumSet.of(EventType.CREATED, EventType.REMOVED, EventType.EVICTED,
                        EventType.EXPIRED));

        for (long i = 0; i < 20; i++) {
            cache.put(i, "Item " + i + " entry!");
        }


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

        Map<Long, String> contents = cache.getAll(keys);

        log.info("***** Contents returned: {}", contents);

        log.info("Sleeping 1 minute then going to retrieve");
        TimeUnit.MINUTES.sleep(1L);

        contents = cache.getAll(keys);

        log.info("Contents returned: {}", contents);

        cm.close();

    }

    @Test
    public void readCache() {
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

        Cache<Long, String> cache =
                cacheManager.getCache("tripleTier", Long.class, String.class);

        Assert.assertNotNull("Missing cache?", cache);

        Map<Long, String> contents = cache.getAll(keys);

        log.info("Contents returned: {}", contents);
    }


    @AfterClass
    public static void tearDown() {
        cacheManager.close();
    }
}
