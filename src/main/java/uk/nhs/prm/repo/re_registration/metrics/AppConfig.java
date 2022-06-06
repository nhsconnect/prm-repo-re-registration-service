package uk.nhs.prm.repo.re_registration.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class AppConfig {

    private final String environment;
    private final String reRegistrationsQueueName;

    public AppConfig(@Value("${environment}") String environment, @Value("${aws.reRegistrationsQueueName}") String reRegistrationsQueueName) {
        this.environment = environment;
        this.reRegistrationsQueueName = reRegistrationsQueueName;
    }

    public String environment() {
        return environment;
    }

    public String reRegistrationsQueueName() {
        return reRegistrationsQueueName;
    }

    @Bean
    @SuppressWarnings("unused")
    public CloudWatchClient cloudWatchClient() {
        return CloudWatchClient.create();
    }
}
