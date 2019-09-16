package com.nfrpaiva.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;

@Component
public class CacheStatComponent {

	Logger logger = LoggerFactory.getLogger(CacheStatComponent.class);

	@Autowired
	private CacheManager cacheManager;

	//@Scheduled(fixedDelay = 5000)
	public void checkStats() {
		cacheManager.getCacheNames().forEach(name -> {
			CaffeineCache cache = (CaffeineCache) cacheManager.getCache(name);
			Cache<Object, Object> nativeCache = cache.getNativeCache();
			logger.info("Cache {} - stats: {} - size {}", name, nativeCache.stats(), nativeCache.estimatedSize());
		});
	}

}
