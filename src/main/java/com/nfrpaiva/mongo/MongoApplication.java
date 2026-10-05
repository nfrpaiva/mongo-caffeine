package com.nfrpaiva.mongo;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;

@SpringBootApplication
public class MongoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MongoApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(PessoaRepository repository) {
		return args -> {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			repository.deleteAll();
			// close() aguarda a conclusão de todas as tarefas
			try (var pool = Executors.newFixedThreadPool(10)) {
				IntStream.rangeClosed(1, 100).forEach(i ->
					pool.execute(() -> repository.save(new Pessoa(null, "Uma Pessoa " + i, new Date(), 1.0, i))));
			}
			stopWatch.stop();
			System.out.println("Execução em segundos: " + stopWatch.getTotalTimeSeconds());
		};
	}

}
