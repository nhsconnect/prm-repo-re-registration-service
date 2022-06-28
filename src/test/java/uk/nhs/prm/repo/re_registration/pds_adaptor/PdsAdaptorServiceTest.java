package uk.nhs.prm.repo.re_registration.pds_adaptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.nhs.prm.repo.re_registration.http.HttpClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdsAdaptorServiceTest {

    @Mock
    HttpClient httpClient;

    @InjectMocks
    PdsAdaptorService pdsAdaptorService;

    @Test
    void shouldCallHttpClientWithCorrectUriAndUserNAmeAndPassword() {
        when(httpClient.get(any(), any(), any())).thenReturn(getResponseEntity());
        pdsAdaptorService.getPatientPdsStatus();
        verify(httpClient, times(1)).get(any(), any(), any());
    }

    @Test
    void shouldReturnPdsAdaptorResponseWhenGetPatientStatusCallReturns200HttpStatusCode() {
        when(httpClient.get(any(), any(), any())).thenReturn(getResponseEntity());
        var patientPdsStatus = pdsAdaptorService.getPatientPdsStatus();

        assertEquals("0000000000",patientPdsStatus.getNhsNumber());
    }

    private ResponseEntity<String> getResponseEntity() {
        return new ResponseEntity<>(pdsResponseString(), HttpStatus.OK);
    }

    private String pdsResponseString() {
        return "{\"nhsNumber\":\"0000000000\",\"isSuspended\":true,\"currentOdsCode\":\"currentOdsCode\",\"managingOrganisation\":\"managingOrganisation\",\"recordETag\":\"etag\",\"isDeceased\":false}";
    }
}