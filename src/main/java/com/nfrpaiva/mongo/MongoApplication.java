package com.nfrpaiva.mongo;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StopWatch;

@SpringBootApplication
public class MongoApplication {

	@Autowired
	private PessoaRepository repository;

	public static void main(String[] args) {
		SpringApplication.run(MongoApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner() {
		return args -> {
			StopWatch stopWatch =  new StopWatch();
			stopWatch.start();
			ExecutorService pool = Executors.newFixedThreadPool(10);
			repository.deleteAll();
			IntStream.rangeClosed(1, 100).boxed().forEach(i -> {
				pool.execute(()->{
					repository.save(new Pessoa(null, "Uma Pessoa " + i, new Date(),1.0, i));
				});
			});
			pool.shutdown();
			pool.awaitTermination(10, TimeUnit.HOURS);
			stopWatch.stop();
			System.out.println("Execução em segundos: " +  stopWatch.getTotalTimeSeconds());
		};
	}

	@Bean
	public Jackson2ObjectMapperBuilder jacksonbuilder() {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.indentOutput(true);
		builder.simpleDateFormat("yyyy-MM-dd");

		return builder;
	}

}
