package uk.nhs.prm.repo.gpregistrationsmiforwarder.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@EnableScheduling
public class HealthCheckStatusPublisher {

    private static final int SECONDS = 1000;
    private static final int MINUTE_INTERVAL = 60 * SECONDS;
    public static final String HEALTH_METRIC_NAME = "Health";

    private final MetricPublisher metricPublisher;

    @Autowired
    public HealthCheckStatusPublisher(MetricPublisher metricPublisher) {
        this.metricPublisher = metricPublisher;
    }

    @Scheduled(fixedRate = MINUTE_INTERVAL)
    public void publishHealthStatus() {
        if (allProbesHealthy()) {
            metricPublisher.publishMetric(HEALTH_METRIC_NAME, 1.0);
        } else {
            metricPublisher.publishMetric(HEALTH_METRIC_NAME, 0.0);
        }
    }

    private boolean allProbesHealthy() {
        //until adding  some other component we can initially return true.
        return true;
    }

}
