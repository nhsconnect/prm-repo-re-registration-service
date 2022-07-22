package uk.nhs.prm.repo.re_registration.ehr_repo;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.re_registration.infra.LocalStackAwsConfig;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration( classes = LocalStackAwsConfig.class)
@DirtiesContext
public class EhrRepoServiceIntegrationTest {
    @Autowired
    private AmazonSQSAsync sqs;

    @Value("${aws.reRegistrationsQueueName}")
    private String reRegistrationsQueueName;

    @Value("${aws.reRegistrationsAuditQueueName}")
    private String reRegistrationsAuditQueueName;

    @Value("${ehrRepoAuthKey}")
    private String authKey;

    private String reRegistrationsQueueUrl;
    private String reRegistrationsAuditUrl;
    private WireMockServer stubEhrRepo;

    public static final String NHS_NUMBER = "1234567890";
    private static final String NEMS_MESSAGE_ID = "nemsMessageId";
    public static final String CONVERSATION_ID1 = "2431d4ff-f760-4ab9-8cd8-a3fc47846762";
    public static final String CONVERSATION_ID2 = "c184cc19-86e9-4a95-b5b5-2f156900bb3c";
    public static final String EHR_SUCCESSFULLY_DELETED_STATUS = "ACTION:RE_REGISTRATION_EHR_DELETED";


    @BeforeEach
    public void setUp() {
        stubEhrRepo = initializeWebServer();
        reRegistrationsQueueUrl = sqs.getQueueUrl(reRegistrationsQueueName).getQueueUrl();
        reRegistrationsAuditUrl = sqs.getQueueUrl(reRegistrationsAuditQueueName).getQueueUrl();
    }

    @AfterEach
    public void tearDown() {
        stubEhrRepo.resetAll();
        stubEhrRepo.stop();
    }

    private WireMockServer initializeWebServer() {
        final WireMockServer wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        return wireMockServer;
    }

    @Test
    void shouldSendMessageWithActionOnAuditTopicWhenEhrRepoReturns200() {
        pdsResponse();
        ehrRepo200Response();
        sqs.sendMessage(reRegistrationsQueueUrl,getReRegistrationEvent().toJsonString());

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(()-> {
            String messageBody = checkMessageInRelatedQueue(reRegistrationsAuditUrl).get(0).getBody();
            assertThat(messageBody).contains(EHR_SUCCESSFULLY_DELETED_STATUS);
            assertThat(messageBody).contains(NEMS_MESSAGE_ID);
            assertThat(messageBody).contains(CONVERSATION_ID1);
            assertThat(messageBody).contains(CONVERSATION_ID2);
        });
    }

    private void ehrRepo200Response() {
        stubFor(delete(urlMatching("/patients/" + NHS_NUMBER))
                .withHeader("Authorization", matching("Basic "+ authKey))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"type\":\"patients\", \"id\":"+ NHS_NUMBER +", \"conversationIds\":[\"2431d4ff-f760-4ab9-8cd8-a3fc47846762\"," + "\"c184cc19-86e9-4a95-b5b5-2f156900bb3c\"]}")
                        .withHeader("Content-Type", "application/json")));
    }

    private void pdsResponse() {
        stubFor(get(urlMatching("/suspended-patient-status/" + NHS_NUMBER))
                .withHeader("Authorization", matching("Basic cmUtcmVnaXN0cmF0aW9uLXNlcnZpY2U6ZGVmYXVsdA=="))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(getPdsResponseString().getBody())));
    }

    private List<Message> checkMessageInRelatedQueue(String queueUrl) {
        System.out.println("checking sqs queue: " + queueUrl);

        var requestForMessagesWithAttributes
                = new ReceiveMessageRequest().withQueueUrl(queueUrl)
                .withMessageAttributeNames("traceId");
        List<Message> messages = sqs.receiveMessage(requestForMessagesWithAttributes).getMessages();
        System.out.printf("Found %s messages on queue: %s%n", messages.size(), queueUrl);
        assertThat(messages).hasSize(1);
        return messages;
    }

    private ReRegistrationEvent getReRegistrationEvent() {
        return new ReRegistrationEvent(NHS_NUMBER, "ABC123", NEMS_MESSAGE_ID, "2017-11-01T15:00:33+00:00");
    }

    private ResponseEntity<String> getPdsResponseString() {
        var pdsResponseString = "{\"nhsNumber\":\"" + NHS_NUMBER + "\",\"isSuspended\":false,\"currentOdsCode\":\"currentOdsCode\",\"managingOrganisation\":\"managingOrganisation\",\"recordETag\":\"etag\",\"isDeceased\":false}";
        return new ResponseEntity<>(pdsResponseString, HttpStatus.OK);
    }
}
