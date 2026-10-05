package com.nfrpaiva.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PessoaRepositoryTest {

	@Autowired
	private PessoaRepository repository;

	@Autowired
	private CacheManager cacheManager;

	@Test
	void testInsert() {
		Pessoa p = new Pessoa();
		p.setNome("Um nome");
		Pessoa result = repository.save(p);
		assertThat(result.getNome()).isEqualTo("Um nome");
		assertThat(result.getId()).isNotNull();
	}

	@Test
	void saveColocaEntidadeNoCache() {
		Pessoa p = new Pessoa();
		p.setNome("Em cache");
		Pessoa result = repository.save(p);
		assertThat(cacheManager.getCache("pessoa").get(result.getId(), Pessoa.class))
			.isNotNull()
			.extracting(Pessoa::getNome)
			.isEqualTo("Em cache");
	}

}
