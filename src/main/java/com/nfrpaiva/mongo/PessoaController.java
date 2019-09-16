package com.nfrpaiva.mongo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("pessoa")
public class PessoaController {

    @Autowired
    private PessoaRepository repository;

    @GetMapping
    public List<Pessoa> getAll(){
        return repository.findAll();
    }
    @GetMapping("{id}")
    public Pessoa findById (@PathVariable String id) {
    	return repository.findById(id).orElseThrow(RuntimeException::new);
    }

}