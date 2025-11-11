package com.kma.backend;

import com.kma.backend.model.TestItem;
import com.kma.backend.repository.TestItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(TestItemRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				TestItem item1 = new TestItem();
				item1.setName("Test Item 1");
				repository.save(item1);

				TestItem item2 = new TestItem();
				item2.setName("Test Item 2");
				repository.save(item2);

				TestItem item3 = new TestItem();
				item3.setName("Test Item 3");
				repository.save(item3);
			}
		};
	}

}
