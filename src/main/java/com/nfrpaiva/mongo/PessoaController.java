package com.nfrpaiva.mongo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("pessoa")
@Slf4j
public class PessoaController {

	@Autowired
	private PessoaRepository repository;

	@GetMapping
	public List<Pessoa> getAll() {
		return repository.findAll();
	}

	@GetMapping("{id}")
    public ResponseEntity<Pessoa> findById (@PathVariable String id) {
    	StopWatch stopWatch = new StopWatch();
    	stopWatch.start();
    	Optional<Pessoa> result = repository.findById(id);
    	stopWatch.stop();
    	log.info("Busca realizada em {} ms", stopWatch.getTotalTimeMillis());
    	 if (result.isPresent()) {
    		 return ResponseEntity.ok(result.get());
    	 }
    	 return ResponseEntity.notFound().build();
    	 
    }

}