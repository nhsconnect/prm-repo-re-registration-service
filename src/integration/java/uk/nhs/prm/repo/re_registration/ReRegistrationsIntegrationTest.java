package uk.nhs.prm.repo.re_registration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import uk.nhs.prm.repo.re_registration.infra.LocalStackAwsConfig;
import uk.nhs.prm.repo.re_registration.logging.TestLogAppender;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
public class ReRegistrationsIntegrationTest {

    @Autowired
    private SqsClient sqsClient;

    @Value("${aws.reRegistrationsQueueName}")
    private String reRegistrationsQueueName;


    private String getReRegistrationsEvent() {
        return "re-registrations-event-message";
    }

    private void createQueue(String queueName) {
        sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build());
    }

    @Test
    void shouldReceiveAndLogAndAcknowledgeReRegistrationsEvent() {
        createQueue(reRegistrationsQueueName);

        var logAppender = TestLogAppender.addTestLogAppender();
        var eventMessage = getReRegistrationsEvent();

        sendMessage(reRegistrationsQueueName, eventMessage);

        await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> {
            var receiveLog = logAppender.findLoggedEvent("RECEIVED");
            assertThat(receiveLog).isNotNull();
            assertThat(receiveLog.getMessage()).contains("length: " + eventMessage.length());
        });


        var messages = receiveMessages(reRegistrationsQueueName);

        assertThat(messages).isEmpty();
    }


    private void sendMessage(String queueName, String messageBody) {
        var sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(getQueueUrl(queueName))
                .messageBody(messageBody)
                .build();
        sqsClient.sendMessage(sendMsgRequest);
    }

    private String getQueueUrl(String queueName) {
        return sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()).queueUrl();
    }

    @Test
    private List<Message> receiveMessages(String queueName) {
        var receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(getQueueUrl(queueName))
                .build();

        return sqsClient.receiveMessage(receiveRequest).messages();
    }

}

