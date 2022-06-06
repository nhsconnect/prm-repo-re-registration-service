package uk.nhs.prm.repo.re_registration.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;


@TestConfiguration
public class LocalStackAwsConfig {

    @Value("${aws.reRegistrationsQueueName}")
    private String reRegistrationsQueueName;

    @Bean
    public static SqsClient sqsClient(@Value("${localstack.url}") String localstackUrl) {
        return SqsClient.builder().endpointOverride(URI.create(localstackUrl)).build();
    }
}
