package uk.nhs.prm.repo.re_registration.pds_adaptor;

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
import uk.nhs.prm.repo.re_registration.http.HttpClient;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.pds_adaptor.model.PdsAdaptorSuspensionStatusResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdsAdaptorServiceTest {
    @Mock
    ReRegistrationAuditPublisher reRegistrationAuditPublisher;

    @Mock
    HttpClient httpClient;

    @Captor
    ArgumentCaptor<String> url;

    @Captor
    ArgumentCaptor<String> username;

    @Captor
    ArgumentCaptor<String> password;

    PdsAdaptorService pdsAdaptorService;

    private String pdsAdaptorServiceUrl = "pds-service-url";
    private String authUserName = "username";
    private String authPassword = "password";

    @BeforeEach
    void init() {
        pdsAdaptorService = new PdsAdaptorService(httpClient, reRegistrationAuditPublisher, pdsAdaptorServiceUrl, authUserName, authPassword);
    }

    @Test
    void shouldCallHttpClientWithCorrectUriAndUserNAmeAndPassword() {
        when(httpClient.get(any(), any(), any())).thenReturn(getPdsResponseStringWithSuspendedStatus(false));
        pdsAdaptorService.getPatientPdsStatus(getReRegistrationEvent());
        verify(httpClient).get(url.capture(), username.capture(), password.capture());
        assertThat("pds-service-url/suspended-patient-status/1234567890").isEqualTo(url.getValue());
        assertThat("username").isEqualTo(username.getValue());
        assertThat("password").isEqualTo(password.getValue());
    }

    @Test
    void shouldReturnParsedPdsAdaptorResponseIfSuccessful(){
        when(httpClient.get(any(), any(), any())).thenReturn(getPdsResponseStringWithSuspendedStatus(true));
        var actualResponse = pdsAdaptorService.getPatientPdsStatus(getReRegistrationEvent());
        var expectedResponse = new PdsAdaptorSuspensionStatusResponse("0000000000",true ,"currentOdsCode","managingOrganisation","etag",false);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void shouldPublishStatusMessageOnAuditTopicWhenPDSAdaptorReturns4xxError() {
        when(httpClient.get(any(), any(), any())).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        pdsAdaptorService.getPatientPdsStatus(getReRegistrationEvent());
        verify(reRegistrationAuditPublisher).sendMessage(new NonSensitiveDataMessage("nemsMessageId", "NO_ACTION:RE_REGISTRATION_FAILED_PDS_ERROR"));
    }

    @Test
    void shouldThrowAnIntermittentErrorPdsExceptionWhenPDSAdaptorReturns5xxError() {
        when(httpClient.get(any(), any(), any())).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThrows(IntermittentErrorPdsException.class, () -> pdsAdaptorService.getPatientPdsStatus(getReRegistrationEvent()));
    }

    private ResponseEntity<String> getPdsResponseStringWithSuspendedStatus(boolean isSuspended) {
        var pdsResponseString = "{\"nhsNumber\":\"0000000000\",\"isSuspended\":" + isSuspended + ",\"currentOdsCode\":\"currentOdsCode\",\"managingOrganisation\":\"managingOrganisation\",\"recordETag\":\"etag\",\"isDeceased\":false}";
        return new ResponseEntity<>(pdsResponseString, HttpStatus.OK);
    }

    private ReRegistrationEvent getReRegistrationEvent() {
        return new ReRegistrationEvent("1234567890", "ABC123", "nemsMessageId", "2017-11-01T15:00:33+00:00");
    }
}