package uk.nhs.prm.repo.re_registration.infra;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;


@TestConfiguration
public class LocalStackAwsConfig {

    /*@Value("${aws.reRegistrationsQueueName}")
    private String reRegistrationsQueueName;*/

    @Bean
    @Primary
    public static SqsClient sqsClient(@Value("${localstack.url}") String localstackUrl) {
        return SqsClient.builder().endpointOverride(URI.create(localstackUrl)).build();
    }

    @Bean
    public static AmazonSQSAsync amazonSQSAsync(@Value("${localstack.url}") String localstackUrl) {
        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("FAKE", "FAKE")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localstackUrl, "eu-west-2"))
                .build();
    }
}
