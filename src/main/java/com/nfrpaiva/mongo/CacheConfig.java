package com.nfrpaiva.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
public class CacheConfig{

	@Bean
	public CacheManager caffeineCacheManager() {
		List<CaffeineCache> caches = new ArrayList<>();
		SimpleCacheManager manager = new SimpleCacheManager();
		
		Cache<Object, Object> pessoaCache = Caffeine
			.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.maximumSize(10_000)
			//.recordStats()
			.build();
		caches.add(new CaffeineCache("pessoa", pessoaCache, false));
		
		
		
		Cache<Object, Object> item = Caffeine
			.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.maximumSize(10_000)
			//.recordStats()
			.build();
		caches.add(new CaffeineCache("item", item, false));
		
		manager.setCaches(caches);
		return manager;
	}

}
