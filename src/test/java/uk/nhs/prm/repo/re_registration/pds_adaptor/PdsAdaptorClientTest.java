package uk.nhs.prm.repo.re_registration.pds_adaptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.http.HttpClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PdsAdaptorClientTest {

    @Mock
    HttpClient httpClient;

    @InjectMocks
    PdsAdaptorClient pdsAdaptorClient;

    @Test
    void shouldCallHttpClientWithCorrectUriAndUserNAmeAndPassword() {
        pdsAdaptorClient.getPatientPdsStatus();
        verify(httpClient,times(1)).get(any(),any(),any());
    }
}