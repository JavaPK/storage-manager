package pl.tt.storagemanager.storagemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StoragemanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoragemanagerApplication.class, args);
	}

}
