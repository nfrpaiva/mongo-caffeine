# Configuração de cache em memória com Caffeine

## Objetivo
Utilizar [caffeine](https://github.com/ben-manes/caffeine) como cache.
> MongoDB foi utilizado como banco de dados de referência

## Requisitos
- Java 25
- Spring Boot 4.1
- Caffeine 3.3
- Docker (os testes sobem um MongoDB via [Testcontainers](https://testcontainers.com/))

## Executando
```bash
# MongoDB local para a aplicação
docker run -d --rm -p 27017:27017 mongo:8

./mvnw spring-boot:run   # aplicação em http://localhost:8080/pessoa
./mvnw verify            # testes (usam Testcontainers)
```

## Maven
```xml
<dependency>
	<groupId>com.github.ben-manes.caffeine</groupId>
	<artifactId>caffeine</artifactId>
</dependency>
```
> Por se tratar de spring-boot a versão do artefato está disponível *"out of the box"*. Caso não esteja usando spring boot você pode encontrar a versão mais recente [aqui](https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine).

## Exemplo configuração de bean de cache
```java
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
		.recordStats()
		.build();
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
	@Cacheable(key = "#id", unless = "#result == null")
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
		if (cacheManager.getCache(name) instanceof CaffeineCache cache) {
			Cache<Object, Object> nativeCache = cache.getNativeCache();
			logger.info("Cache {} - stats: {} - size {}", name, nativeCache.stats(), nativeCache.estimatedSize());
		}
	});
}
```
