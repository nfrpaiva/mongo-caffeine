package com.nfrpaiva.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;

@Component
public class CacheStatComponent {

	private static final Logger logger = LoggerFactory.getLogger(CacheStatComponent.class);

	private final CacheManager cacheManager;

	public CacheStatComponent(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Scheduled(fixedDelay = 5000)
	public void checkStats() {
		cacheManager.getCacheNames().forEach(name -> {
			if (cacheManager.getCache(name) instanceof CaffeineCache cache) {
				Cache<Object, Object> nativeCache = cache.getNativeCache();
				logger.info("Cache {} - stats: {} - size {}", name, nativeCache.stats(), nativeCache.estimatedSize());
			}
		});
	}

}
