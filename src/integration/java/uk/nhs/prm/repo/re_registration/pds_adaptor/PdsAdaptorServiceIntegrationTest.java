package uk.nhs.prm.repo.re_registration.pds_adaptor;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.re_registration.infra.LocalStackAwsConfig;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration( classes = LocalStackAwsConfig.class)
public class PdsAdaptorServiceIntegrationTest {

    public static final String NHS_NUMBER = "1234567890";
    public static final String STATUS_MESSAGE_FOR_WHEN_PATIENT_IS_STILL_SUSPENDED = "NO_ACTION:RE_REGISTRATION_FAILED_STILL_SUSPENDED";
    public static final String STATUS_MESSAGE_FOR_WHEN_PDS_RETURNS_4XX_ERROR = "NO_ACTION:RE_REGISTRATION_FAILED_PDS_ERROR";
    @Autowired
    private AmazonSQSAsync sqs;

    @Value("${aws.reRegistrationsQueueName}")
    private String reRegistrationsQueueName;

    private String splunkAuditUploader = "splunk-audit-uploader";

    WireMockServer stubPdsAdaptor;
    private String reRegistrationsQueueUrl;
    private String splunkAuditUploaderUrl;
    private String nemsMessageId = "nemsMessageId";;


    @BeforeEach
    public void setUp() {
        stubPdsAdaptor = initializeWebServer();
        reRegistrationsQueueUrl = sqs.getQueueUrl(reRegistrationsQueueName).getQueueUrl();
        splunkAuditUploaderUrl = sqs.getQueueUrl(splunkAuditUploader).getQueueUrl();
    }

    @AfterEach
    public void tearDown() {
        stubPdsAdaptor.resetAll();
        stubPdsAdaptor.stop();
    }

    private WireMockServer initializeWebServer() {
        final WireMockServer wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        return wireMockServer;
    }

    @Test
    void shouldNotPutAnythingOnReRegistrationAuditTopicWhenPdsReturnsResponseWithStatusCode500() {
        setPdsErrorStateWithStatusCode(NHS_NUMBER, 500);
        sqs.sendMessage(reRegistrationsQueueUrl,getReRegistrationEvent().toJsonString());
        await().atMost(20, TimeUnit.SECONDS).untilAsserted(()->assertThat(isQueueEmpty(splunkAuditUploaderUrl)));
    }

    @Test
    void shouldPutTheAuditStatusMessageOnAuditTopicWhenPdsReturnsResponseWithStatusCode200() {
        setPds200SuccessState(NHS_NUMBER);

        sqs.sendMessage(reRegistrationsQueueUrl,getReRegistrationEvent().toJsonString());

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(()-> {
            String messageBody = checkMessageInRelatedQueue(splunkAuditUploaderUrl).get(0).getBody();
            assertThat(messageBody).contains(STATUS_MESSAGE_FOR_WHEN_PATIENT_IS_STILL_SUSPENDED);
            assertThat(messageBody).contains(nemsMessageId);
        }
        );
    }

    @Test
    void shouldPutTheAuditStatusMessageOnAuditTopicWhenPdsReturnsResponseWithStatusCode400() {
        setPdsErrorStateWithStatusCode(NHS_NUMBER, 400);

        sqs.sendMessage(reRegistrationsQueueUrl,getReRegistrationEvent().toJsonString());

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(()-> {
                    String messageBody = checkMessageInRelatedQueue(splunkAuditUploaderUrl).get(0).getBody();
                    assertThat(messageBody).contains(STATUS_MESSAGE_FOR_WHEN_PDS_RETURNS_4XX_ERROR);
                    assertThat(messageBody).contains(nemsMessageId);
                }
        );
    }

//    @Test
//    void shouldPutTheAuditStatusMessageOnAuditTopicWhenPdsReturnsResponseWithStatusCode200() {
//        setPds200SuccessState(NHS_NUMBER);
//
//        sqs.sendMessage(reRegistrationsQueueUrl,getReRegistrationEvent().toJsonString());
//        await().atMost(20, TimeUnit.SECONDS).untilAsserted(()->assertThat(checkMessageInRelatedQueue(splunkAuditUploaderUrl)));
//    }


    private void setPdsErrorStateWithStatusCode(String nhsNumber, int statusCode) {
        stubFor(get(urlMatching("/suspended-patient-status/" + nhsNumber))
                .withHeader("Authorization", matching("Basic cmUtcmVnaXN0cmF0aW9uLXNlcnZpY2U6ZGVmYXVsdA=="))
                .willReturn(aResponse()
                        .withStatus(statusCode)// request unsuccessful with status code 500
                        .withHeader("Content-Type", "text/xml")
                        .withBody("<response>Some content</response>")));
    }

    private void setPds200SuccessState( String nhsNumber) {
        stubFor(get(urlMatching("/suspended-patient-status/" + nhsNumber))
                .withHeader("Authorization", matching("Basic cmUtcmVnaXN0cmF0aW9uLXNlcnZpY2U6ZGVmYXVsdA=="))
                .willReturn(aResponse()
                        .withStatus(200)// request unsuccessful with status code 500
                        .withHeader("Content-Type", "text/xml")
                        .withBody(getPdsResponseString().getBody())));
    }

    private ReRegistrationEvent getReRegistrationEvent() {
        return new ReRegistrationEvent(NHS_NUMBER, "ABC123", nemsMessageId, "2017-11-01T15:00:33+00:00");
    }

    private boolean isQueueEmpty(String queueUrl) {
        List<String> attributeList = new ArrayList<>();
        attributeList.add("ApproximateNumberOfMessagesNotVisible");
        attributeList.add("ApproximateNumberOfMessages");
        GetQueueAttributesResult getQueueAttributesResult = sqs.getQueueAttributes(queueUrl, attributeList);

        var numberOfMessageNotVisible = Integer.valueOf(getQueueAttributesResult.getAttributes().get("ApproximateNumberOfMessagesNotVisible"));
        var numberOfMessageVisible = Integer.valueOf(getQueueAttributesResult.getAttributes().get("ApproximateNumberOfMessages"));

        return (numberOfMessageVisible == 0 && numberOfMessageNotVisible == 0);
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

    private ResponseEntity<String> getPdsResponseString() {
        var pdsResponseString = "{\"nhsNumber\":\"" + NHS_NUMBER + "\",\"isSuspended\":true,\"currentOdsCode\":\"currentOdsCode\",\"managingOrganisation\":\"managingOrganisation\",\"recordETag\":\"etag\",\"isDeceased\":false}";
        return new ResponseEntity<>(pdsResponseString, HttpStatus.OK);
    }
}
