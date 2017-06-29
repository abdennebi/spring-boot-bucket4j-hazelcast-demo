package com.abddennebi.demo.config;

import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICacheManager;
import io.github.bucket4j.grid.GridBucketState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Cache;

@Configuration
public class HazelcastConfiguration {

    @Bean
    Cache<String, GridBucketState> cache() {
        Config config = new Config();
        config.setLiteMember(false);
        CacheSimpleConfig cacheConfig = new CacheSimpleConfig();
        cacheConfig.setName("buckets");
        config.addCacheConfig(cacheConfig);

        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        ICacheManager cacheManager = hazelcastInstance.getCacheManager();
        Cache<String, GridBucketState> cache = cacheManager.getCache("buckets");
        return cache;
    }


}