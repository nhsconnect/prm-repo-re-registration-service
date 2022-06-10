package uk.nhs.prm.repo.re_registration.infra;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.sqs.SqsClient;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;


@TestConfiguration
public class LocalStackAwsConfig {

    @Value("${aws.reRegistrationsQueueName}")
    private String reRegistrationsQueueName;

    @Value("${aws.region}")
    private String awsRegion;

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @Bean
    public static SqsClient sqsClient(@Value("${localstack.url}") String localstackUrl) throws URISyntaxException {
        return SqsClient.builder()
                .credentialsProvider((()-> AwsBasicCredentials.create("FAKE", "FAKE")))
                .endpointOverride(new URI(localstackUrl))
                .build();
    }

    @Bean
    public static AmazonSQSAsync amazonSQSAsync(@Value("${localstack.url}") String localstackUrl) {
        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("FAKE", "FAKE")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localstackUrl, "eu-west-2"))
                .build();
    }

    @PostConstruct
    public void setupTestQueuesAndTopics() {
        recreateReRegistrationsQueue();
    }

    private void recreateReRegistrationsQueue() {
        ensureQueueDeleted(reRegistrationsQueueName);
        createQueue(reRegistrationsQueueName);
    }

    private void createQueue(String queueName) {
        CreateQueueRequest createQueueRequest = new CreateQueueRequest();
        createQueueRequest.setQueueName(queueName);
        HashMap<String, String> attributes = new HashMap<>();
        createQueueRequest.withAttributes(attributes);
        amazonSQSAsync.createQueue(queueName);
    }

    private void ensureQueueDeleted(String queueName) {
        try {
            deleteQueue(queueName);
        }
        catch (QueueDoesNotExistException e) {
            // no biggie
        }
    }

    private void deleteQueue(String queueName) {
        amazonSQSAsync.deleteQueue(amazonSQSAsync.getQueueUrl(queueName).getQueueUrl());
    }

}
