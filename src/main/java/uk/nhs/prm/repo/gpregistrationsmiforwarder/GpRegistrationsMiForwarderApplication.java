package uk.nhs.prm.repo.gpregistrationsmiforwarder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class GpRegistrationsMiForwarderApplication {

	public static void main(String[] args) {
		SpringApplication.run(GpRegistrationsMiForwarderApplication.class, args);
	}

}
