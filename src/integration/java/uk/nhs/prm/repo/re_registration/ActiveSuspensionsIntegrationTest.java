package uk.nhs.prm.repo.re_registration;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.re_registration.data.ActiveSuspensionsDetailsDb;
import uk.nhs.prm.repo.re_registration.infra.LocalStackAwsConfig;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
public class ActiveSuspensionsIntegrationTest {
    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @Autowired
    ActiveSuspensionsDetailsDb activeSuspensionsDetailsDb;

    @Value("${aws.activeSuspensionsQueueName}")
    private String activeSuspensionsQueueName;

    private final String nhsNumber = "0987654321";
    private final String previousOdsCode = "TEST00";
    private final String nemsLastUpdatedDate = "2022-09-01T15:00:33+00:00";

    @Test
    void shouldSaveMessageFromActiveSuspensionsQueueInDb() {
        sendMessage(activeSuspensionsQueueName, getActiveSuspensionsMessage());

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var activeSuspensionsData = activeSuspensionsDetailsDb.getByNhsNumber(nhsNumber);

            assertThat(activeSuspensionsData.getNhsNumber()).isEqualTo(nhsNumber);
            assertThat(activeSuspensionsData.getPreviousOdsCode()).isEqualTo(previousOdsCode);
            assertThat(activeSuspensionsData.getNemsLastUpdatedDate()).isEqualTo(nemsLastUpdatedDate);
        });

    }

    private void sendMessage(String queueName, String messageBody) {
        var queueUrl = amazonSQSAsync.getQueueUrl(queueName).getQueueUrl();
        amazonSQSAsync.sendMessage(queueUrl, messageBody);
    }

    private String getActiveSuspensionsMessage() {
        return new ActiveSuspensionsMessage(nhsNumber, previousOdsCode, nemsLastUpdatedDate).toJsonString();
    }

}
