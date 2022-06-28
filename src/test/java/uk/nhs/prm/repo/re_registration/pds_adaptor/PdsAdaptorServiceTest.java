package uk.nhs.prm.repo.re_registration.pds_adaptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.nhs.prm.repo.re_registration.http.HttpClient;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdsAdaptorServiceTest {
    @Mock
    ReRegistrationAuditPublisher reRegistrationAuditPublisher;

    @Mock
    HttpClient httpClient;

    @InjectMocks
    PdsAdaptorService pdsAdaptorService;

    @Test
    void shouldCallHttpClientWithCorrectUriAndUserNAmeAndPassword() {
        when(httpClient.get(any(), any(), any())).thenReturn(getResponseEntityForNonSuspendedPatient());
        pdsAdaptorService.getPatientPdsStatus(new ReRegistrationEvent());
        verify(httpClient, times(1)).get(any(), any(), any());
    }

    @Test
    void shouldReturnPdsAdaptorResponseWhenGetPatientStatusCallReturns200HttpStatusCode() {
        when(httpClient.get(any(), any(), any())).thenReturn(getResponseEntityForNonSuspendedPatient());
        var patientPdsStatus = pdsAdaptorService.getPatientPdsStatus(new ReRegistrationEvent());

        assertEquals("0000000000",patientPdsStatus.getNhsNumber());
    }

    @Test
    void shouldPublishToQueueWhenPatientIsSuspended() {
        when(httpClient.get(any(), any(), any())).thenReturn(getResponseEntityForSuspendedPatient());
        var patientPdsStatus = pdsAdaptorService.getPatientPdsStatus(new ReRegistrationEvent(null,null,"nemsMessageId",null));
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
}