package com.nfrpaiva.mongo;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PessoaRepositoryTest {

    @Autowired
    private PessoaRepository repository;

    @Test
    public void testInsert() throws Exception {

        Pessoa p = new Pessoa();
        p.setNome("Um nome");
        Pessoa result = repository.save(p);
        Assertions.assertThat(result.getNome()).isEqualTo("Um nome");
        Assertions.assertThat(result.getId()).isNotNull();

    }

}
