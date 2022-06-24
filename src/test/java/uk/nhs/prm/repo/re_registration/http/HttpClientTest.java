package uk.nhs.prm.repo.re_registration.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HttpClientTest {
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    HttpClient client;

    @Test
    void shouldInvokeRestTemplateExchange() {
     client.makeAGetCall("");
     verify(restTemplate, times(1)).exchange(ArgumentMatchers.eq(""),any(),any(), ArgumentMatchers.eq(String.class));
    }
}