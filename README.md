# Projeto - mongo-caffeine
## Objetivo
Configurar um cache com expiração e configuração indepentende em memória

## Maven
```xml
<dependency>
	<groupId>com.github.ben-manes.caffeine</groupId>
	<artifactId>caffeine</artifactId>
</dependency>
```
> Por se tratar de spring boot a versão do artefato esta disponível *out of the box*. Caso não esteja usando spring boot você pode encontrar a versão mais recente [aqui](https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine).

## Exemplo configuração de bean de cache
```java
@Bean
	public CacheManager caffeineCacheManager() {
		List<CaffeineCache> caches = new ArrayList<>();
		SimpleCacheManager manager = new SimpleCacheManager();
		
		Cache<Object, Object> pessoaCache = Caffeine
			.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.maximumSize(10_000)
			.recordStats()
			.build();
		caches.add(new CaffeineCache("pessoa", pessoaCache, false));
		
		Cache<Object, Object> item = Caffeine
			.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.maximumSize(10_000)
			.recordStats()
			.build();
		caches.add(new CaffeineCache("item", item, false));
		
		manager.setCaches(caches);
		return manager;
	}
```

## Exemplo de configuração de cache utilizando MongoRepository

```java
@Repository
@CacheConfig(cacheNames = "pessoa")
public interface PessoaRepository extends MongoRepository<Pessoa, String> {

	@Override
	@Cacheable
	List<Pessoa> findAll();

	@Override
	@CachePut(key = "#entity.id", unless = "#result == null")
	<S extends Pessoa> S save(S entity);

	@Override
	@Cacheable(key = "#id")
	Optional<Pessoa> findById(String id) ;
	
}
```
>Observe que os métodos foram sobre escritos para que fosse possível anotar as políticas de cache.

## Configuração de Log
Caso queira observar as operações de cache para debug
```properties
logging.level.org.springframework.cache=trace
```
## Analisando estatísticas de cache
### Exemplo com Schedule do spring
>Não vou entrar nos detalhes de configuração do Schedule do spring apenas um exemplo de log de estatística utilizando essa ferramenta.
```java
@Scheduled(fixedDelay = 5000)
public void checkStats() {
	cacheManager.getCacheNames().forEach(name -> {
		CaffeineCache cache = (CaffeineCache) cacheManager.getCache(name);
		Cache<Object, Object> nativeCache = cache.getNativeCache();
		logger.info("Cache {} - stats: {} - size {}", name nativeCache.stats(), nativeCache.estimatedSize());
	});
}
```
