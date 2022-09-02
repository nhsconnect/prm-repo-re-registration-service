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

    private static final String NHS_NUMBER = "0987654321";
    private static final String PREVIOUS_ODS_CODE = "TEST00";
    private static final String NEMS_LAST_UPDATED_DATE = "2022-09-01T15:00:33+00:00";

    @Test
    void shouldSaveMessageFromActiveSuspensionsQueueInDb() {
        sendMessage(activeSuspensionsQueueName, getActiveSuspensionsMessage());

        await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> {
            var activeSuspensionsData = activeSuspensionsDetailsDb.getByNhsNumber(NHS_NUMBER);

            assertThat(activeSuspensionsData.getNhsNumber()).isEqualTo(NHS_NUMBER);
            assertThat(activeSuspensionsData.getPreviousOdsCode()).isEqualTo(PREVIOUS_ODS_CODE);
            assertThat(activeSuspensionsData.getNemsLastUpdatedDate()).isEqualTo(NEMS_LAST_UPDATED_DATE);
        });

    }

    private void sendMessage(String queueName, String messageBody) {
        var queueUrl = amazonSQSAsync.getQueueUrl(queueName).getQueueUrl();
        amazonSQSAsync.sendMessage(queueUrl, messageBody);
    }

    private String getActiveSuspensionsMessage() {
        return new ActiveSuspensionsMessage(NHS_NUMBER, PREVIOUS_ODS_CODE, NEMS_LAST_UPDATED_DATE).toJsonString();
    }

}
