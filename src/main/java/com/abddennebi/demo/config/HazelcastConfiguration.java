package com.abddennebi.demo.config;

import io.github.bucket4j.grid.GridBucketState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICacheManager;
import io.github.bucket4j.grid.GridBucketState;

@Configuration
public class HazelcastConfiguration {

//    @Bean
//    Cache<String, GridBucketState> cache() {
//        // Retrieve the CachingProvider which is automatically backed by
//        // the chosen Hazelcast server or client provider
//        CachingProvider cachingProvider = Caching.getCachingProvider();
//
//        // Create a CacheManager
//        CacheManager cacheManager = cachingProvider.getCacheManager();
//
//        // Create a simple but typesafe configuration for the cache
//        CompleteConfiguration<String, GridBucketState> config =
//                new MutableConfiguration<String, GridBucketState>()
//                        .setTypes(String.class, GridBucketState.class);
//
//
//
//        // Create and get the cache
//        Cache<String, GridBucketState> cache = cacheManager.createCache("buckets", config);
//
//        return cache;
//    }

    @Bean
    Cache<String, GridBucketState> cache() {
        Config config = new Config();
        config.setLiteMember(true);
        CacheSimpleConfig cacheConfig = new CacheSimpleConfig();
        cacheConfig.setName("buckets");
        config.addCacheConfig(cacheConfig);

        HazelcastInstance  hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        ICacheManager cacheManager = hazelcastInstance.getCacheManager();
        Cache<String, GridBucketState> cache = cacheManager.getCache("buckets");
        return cache;
    }



}