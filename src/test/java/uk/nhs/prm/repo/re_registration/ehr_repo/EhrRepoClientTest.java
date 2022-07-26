package uk.nhs.prm.repo.re_registration.ehr_repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.nhs.prm.repo.re_registration.config.Tracer;
import uk.nhs.prm.repo.re_registration.http.HttpClient;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EhrRepoClientTest {
    @Mock
    ReRegistrationAuditPublisher reRegistrationAuditPublisher;

    Tracer tracer = mock(Tracer.class);

    static UUID traceId = UUID.randomUUID();

    @Mock
    HttpClient httpClient;

    EhrRepoService ehrRepoService;


    @Captor
    ArgumentCaptor<String> url;

    @Captor
    ArgumentCaptor<String> authKey;


    private String ehrRepoServiceUrl = "ehr-repo-service-url";
    private String ehrRepoAuthKey = "authKey";

    @BeforeEach
    void init() throws MalformedURLException {
        ehrRepoService = new EhrRepoService(ehrRepoServiceUrl, ehrRepoAuthKey, tracer, reRegistrationAuditPublisher, httpClient);
    }

//    @Test
//    void shouldCallHttpClientWithCorrectUriAndUserNAmeAndPassword() throws IOException, InterruptedException {
//        when(httpClient.delete(any(), any())).thenReturn(createDeleteEhrResponseJsonString());
//        ehrRepoService.deletePatientEhr(getReRegistrationEvent());
//        verify(httpClient).delete(url.capture(), authKey.capture());
//        assertThat("ehr-repo-service-url/patients/1234567890").isEqualTo(url.getValue());
//        assertThat("authKey").isEqualTo(authKey.getValue());
//    }
//
//    @Test
//    void shouldReturnParsedEhrRepoResponseIfSuccessfulAndWhenEhrResponseReturns200Ok() {
//        when(httpClient.delete(any(), any())).thenReturn(createDeleteEhrResponseJsonString());
//        var actualResponse = ehrRepoService.deletePatientEhr(getReRegistrationEvent());
//        var expectedResponse = createExpectedSuccessfulEhrDeleteResponse();
//        assertEquals(expectedResponse, actualResponse);
//    }

    @Test
    void shouldPublishStatusMessageOnAuditTopicWhenEhrResponseReturns404Error() throws IOException, InterruptedException {
        when(httpClient.delete(any(), any())).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        assertThrows(HttpClientErrorException.class, () -> {
            ehrRepoService.deletePatientEhr(getReRegistrationEvent());
        });
        verify(reRegistrationAuditPublisher, times(1)).sendMessage(new NonSensitiveDataMessage(getReRegistrationEvent().getNemsMessageId(), "NO_ACTION:RE_REGISTRATION_EHR_NOT_IN_REPO"));
    }

    @Test
    void shouldPublishStatusMessageOnAuditTopicWhenEhrResponseReturns400Error() throws IOException, InterruptedException {
        when(httpClient.delete(any(), any())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        assertThrows(HttpClientErrorException.class, () -> {
            ehrRepoService.deletePatientEhr(getReRegistrationEvent());
        });
        verify(reRegistrationAuditPublisher, times(1)).sendMessage(new NonSensitiveDataMessage(getReRegistrationEvent().getNemsMessageId(), "NO_ACTION:RE_REGISTRATION_EHR_FAILED_TO_DELETE"));
    }

    @Test
    void shouldThrowAnIntermittentErrorExceptionWhenEhrResponseReturns5xxError() throws IOException, InterruptedException {
        when(httpClient.delete(any(), any())).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThrows(IntermittentErrorEhrRepoException.class, () -> ehrRepoService.deletePatientEhr(getReRegistrationEvent()));
    }

    private ReRegistrationEvent getReRegistrationEvent() {
        return new ReRegistrationEvent("1234567890", "ABC123", "nemsMessageId", "2017-11-01T15:00:33+00:00");
    }


    private ResponseEntity<String> createDeleteEhrResponseJsonString() {
        var ehrRepoDeleteResponse = "{\"data\":" +
                "{\"type\":\"patients\", " +
                "\"id\":\"1234567890\", " +
                "\"conversationIds\":[\"2431d4ff-f760-4ab9-8cd8-a3fc47846762\"," + "\"c184cc19-86e9-4a95-b5b5-2f156900bb3c\"]}}";
        return new ResponseEntity<>(ehrRepoDeleteResponse, HttpStatus.OK);
    }

    private EhrDeleteResponseContent createExpectedSuccessfulEhrDeleteResponse() {
        return new EhrDeleteResponseContent("patients", "1234567890", Arrays.asList("2431d4ff-f760-4ab9-8cd8-a3fc47846762", "c184cc19-86e9-4a95-b5b5-2f156900bb3c"));
    }
}