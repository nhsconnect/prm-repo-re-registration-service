package uk.nhs.prm.repo.re_registration.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.metrics.HealthCheckStatusPublisher;
import uk.nhs.prm.repo.re_registration.metrics.MetricPublisher;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckStatusPublisherTest {
    private MetricPublisher metricPublisher;

    @BeforeEach
    void setUp(){
        metricPublisher = Mockito.mock(MetricPublisher.class);
    }

    @Test
    public void shouldSetHealthMetricToHealthy() {

        HealthCheckStatusPublisher healthPublisher = new HealthCheckStatusPublisher(metricPublisher);
        healthPublisher.publishHealthStatus();

        verify(metricPublisher,times(1)).publishMetric("Health", 1.0);
    }

}
