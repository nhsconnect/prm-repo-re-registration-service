package uk.nhs.prm.repo.re_registration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReRegistrationServiceTests {

	@Test
	void contextLoads() {
		assertThat(1 + 1).isEqualTo(2);
	}

}
