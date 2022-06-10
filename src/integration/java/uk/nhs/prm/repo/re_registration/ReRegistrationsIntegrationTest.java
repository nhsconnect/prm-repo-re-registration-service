package uk.nhs.prm.repo.re_registration;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.re_registration.infra.LocalStackAwsConfig;
import uk.nhs.prm.repo.re_registration.logging.TestLogAppender;

import java.util.HashMap;
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
    private AmazonSQSAsync amazonSQSAsync;

    @Value("${aws.reRegistrationsQueueName}")
    private String reRegistrationsQueueName;


    private String getReRegistrationsEvent() {
        return "re-registrations-event-message";
    }

    private void createQueue(String queueName) {
        CreateQueueRequest createQueueRequest = new CreateQueueRequest();
        createQueueRequest.setQueueName(queueName);
        HashMap<String, String> attributes = new HashMap<>();
        createQueueRequest.withAttributes(attributes);
        amazonSQSAsync.createQueue(reRegistrationsQueueName);

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
        var queueUrl = getQueueUrl(queueName);
        amazonSQSAsync.sendMessage(queueUrl, messageBody);
    }

    private String getQueueUrl(String queueName) {
        return amazonSQSAsync.getQueueUrl(queueName).getQueueUrl();
    }

    @Test
    private List<Message> receiveMessages(String queueName) {
        String queueUrl = amazonSQSAsync.getQueueUrl(queueName).getQueueUrl();

        var receiveMessageRequest = new ReceiveMessageRequest().withQueueUrl(queueUrl);
        var messages = amazonSQSAsync.receiveMessage(receiveMessageRequest).getMessages();
        return messages;
    }

}

