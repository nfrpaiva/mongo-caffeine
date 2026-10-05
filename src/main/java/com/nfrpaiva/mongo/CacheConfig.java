package com.nfrpaiva.mongo;

import java.time.Duration;
import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public CacheManager caffeineCacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(List.of(
				new CaffeineCache("pessoa", buildCache(), false),
				new CaffeineCache("item", buildCache(), false)));
		return manager;
	}

	private static Cache<Object, Object> buildCache() {
		return Caffeine
			.newBuilder()
			.expireAfterAccess(Duration.ofMinutes(2))
			.maximumSize(10_000)
			//.recordStats()
			.build();
	}

}
