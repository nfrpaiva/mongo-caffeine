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