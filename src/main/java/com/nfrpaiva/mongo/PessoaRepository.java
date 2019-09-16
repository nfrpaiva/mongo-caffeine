package com.nfrpaiva.mongo;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

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
	@Cacheable(key = "#id", unless="#result == null")
	Optional<Pessoa> findById(String id) ;
	
	
}