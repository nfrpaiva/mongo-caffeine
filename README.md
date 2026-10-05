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

A aplicação conecta em `mongodb://localhost:27017` (padrão do Spring Boot). Para usar outro servidor:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--spring.mongodb.uri=mongodb://outro-host:27017/banco
```

> [!WARNING]
> Ao iniciar, a aplicação **apaga toda a coleção `pessoa`** (`repository.deleteAll()`) e insere 100 pessoas de exemplo
> usando 10 threads (veja `MongoApplication`). Não aponte a aplicação para um banco com dados que você queira manter.

## Endpoints
| Método | Caminho        | Descrição                                                        |
|--------|----------------|------------------------------------------------------------------|
| GET    | `/pessoa`      | Lista todas as pessoas (resultado em cache)                      |
| GET    | `/pessoa/{id}` | Busca uma pessoa pelo id (em cache); retorna 404 se não existir  |

A busca por id registra no log o tempo gasto (`Busca realizada em X ms`). Chame o mesmo id duas vezes para ver a diferença
entre a primeira consulta (MongoDB) e a segunda (cache):
```bash
ID=$(curl -s localhost:8080/pessoa | grep -m1 '"id"' | cut -d'"' -f4)
curl -s localhost:8080/pessoa/$ID
curl -s localhost:8080/pessoa/$ID
```

As respostas JSON saem indentadas e as datas no formato `yyyy-MM-dd` (configurado em `application.properties`).

## Maven
```xml
<dependency>
	<groupId>com.github.ben-manes.caffeine</groupId>
	<artifactId>caffeine</artifactId>
</dependency>
```
> Por se tratar de spring-boot a versão do artefato está disponível *"out of the box"*. Neste projeto a propriedade
> `caffeine.version` no `pom.xml` fixa a versão 3.3.0, sobrescrevendo a gerenciada pelo Spring Boot. Caso não esteja usando
> spring boot você pode encontrar a versão mais recente [aqui](https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine).

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
- `expireAfterAccess`: a entrada expira 2 minutos após o último acesso (leitura ou escrita).
- `maximumSize`: no máximo 10.000 entradas por cache; acima disso o Caffeine remove as menos relevantes.
- `recordStats`: habilita a coleta de estatísticas (acertos, falhas, remoções), usada na seção de estatísticas abaixo.
- O terceiro argumento `false` do `CaffeineCache` indica que valores `null` não são armazenados.
- O cache `item` está declarado apenas como exemplo de múltiplos caches; ele não é usado pelo código.

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

### Limitações deste exemplo
Este exemplo é propositalmente simples. Antes de usar o padrão em produção, considere:
- **Lista desatualizada:** `findAll()` é guardado no cache `pessoa` sob uma única chave (`SimpleKey.EMPTY`). O `save`
  atualiza apenas a entrada do id salvo, então a lista em cache não inclui novos registros nem alterações até expirar.
  Uma alternativa é anotar `save` também com `@CacheEvict(key = "T(org.springframework.cache.interceptor.SimpleKey).EMPTY")`
  (via `@Caching`) ou usar um cache separado para a lista.
- **Exclusões não invalidam o cache:** `delete`, `deleteById` e `deleteAll` não têm `@CacheEvict`; registros excluídos
  continuam sendo retornados pelo cache até expirarem.
- **Cache local:** o Caffeine guarda os dados na memória de cada instância. Com várias instâncias da aplicação, cada uma
  tem o seu cache e elas podem divergir entre si.

## Configuração de Log
Caso queira observar as operações de cache para debug, descomente em `application.properties`:
```properties
logging.level.org.springframework.cache=trace
logging.level.com.github.benmanes=trace
```
## Analisando estatísticas de cache
### Exemplo com Schedule do spring
O agendamento é habilitado em `ScheduleConfig` (`@EnableScheduling`, com um pool de 10 threads), e o
`CacheStatComponent` registra no log, a cada 5 segundos, as estatísticas de cada cache Caffeine:
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
> As estatísticas só são coletadas porque o cache foi criado com `.recordStats()`; sem ele os valores aparecem zerados.
> Para desligar o log periódico, remova a anotação `@Scheduled`.
