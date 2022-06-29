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
import uk.nhs.prm.repo.re_registration.http.HttpClient;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(httpClient.get(any(), any(), any())).thenReturn(getResponseEntityForSuspendedPatient());
        pdsAdaptorService.getPatientPdsStatus(getReRegistrationEvent());
        verify(httpClient).get(url.capture(), username.capture(), password.capture());
        assertThat("pds-service-url/1234567890").isEqualTo(url.getValue());
        assertThat("username").isEqualTo(username.getValue());
        assertThat("password").isEqualTo(password.getValue());
    }

    @Test
    void shouldPublishToQueueWhenPatientIsSuspended() {
        when(httpClient.get(any(), any(), any())).thenReturn(getResponseEntityForSuspendedPatient());
        pdsAdaptorService.getPatientPdsStatus(new ReRegistrationEvent(null,null,"nemsMessageId",null));
        verify(reRegistrationAuditPublisher).sendMessage(new NonSensitiveDataMessage("nemsMessageId","NO_ACTION:RE_REGISTRATION_FAILED_STILL_SUSPENDED"));
    }

    private ResponseEntity<String> getResponseEntityForNonSuspendedPatient() {
        return new ResponseEntity<>(pdsResponseStringForNotSuspendedPatient(), HttpStatus.OK);
    }
    private ResponseEntity<String> getResponseEntityForSuspendedPatient() {
        return new ResponseEntity<>(pdsResponseStringForSuspendedPatient(), HttpStatus.OK);
    }

    private String pdsResponseStringForNotSuspendedPatient() {
        return "{\"nhsNumber\":\"0000000000\",\"isSuspended\":false,\"currentOdsCode\":\"currentOdsCode\",\"managingOrganisation\":\"managingOrganisation\",\"recordETag\":\"etag\",\"isDeceased\":false}";
    }
    private String pdsResponseStringForSuspendedPatient() {
        return "{\"nhsNumber\":\"0000000000\",\"isSuspended\":true,\"currentOdsCode\":\"currentOdsCode\",\"managingOrganisation\":\"managingOrganisation\",\"recordETag\":\"etag\",\"isDeceased\":false}";
    }
    private ReRegistrationEvent getReRegistrationEvent(){
        return new ReRegistrationEvent("1234567890", "ABC123", UUID.randomUUID().toString(), "2017-11-01T15:00:33+00:00");
    }
}