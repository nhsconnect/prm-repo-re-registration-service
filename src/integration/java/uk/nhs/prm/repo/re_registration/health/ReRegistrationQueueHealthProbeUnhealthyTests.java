package uk.nhs.prm.repo.re_registration.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.nhs.prm.repo.re_registration.metrics.healthprobes.ReRegistrationsQueueHealthProbe;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("non-existent-queue")
class ReRegistrationQueueHealthProbeUnhealthyTests {

	@Autowired
	private ReRegistrationsQueueHealthProbe probe;

	@Test
	void shouldReturnNotHealthyWhenTheProbeCannotAccessTheQueue() {
		assertThat(probe.isHealthy()).isEqualTo(false);
	}

}
