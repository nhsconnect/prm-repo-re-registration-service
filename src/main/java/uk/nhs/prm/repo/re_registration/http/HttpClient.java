package uk.nhs.prm.repo.re_registration.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.nhs.prm.repo.re_registration.config.Tracer;

import java.util.Arrays;

@Component
public class HttpClient {

    private final RestTemplate restTemplate;
    private final Tracer tracer;

    public HttpClient(RestTemplate restTemplate, Tracer tracer) {
        this.restTemplate = restTemplate;
        this.tracer = tracer;
    }

    public ResponseEntity<String> get(String uri, String userName, String password) {
        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(getHeaders(userName, password)), String.class);
    }

    private HttpHeaders getHeaders(String userName, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(userName, password);
        headers.add("traceId", tracer.getTraceId());
        return headers;
    }
}