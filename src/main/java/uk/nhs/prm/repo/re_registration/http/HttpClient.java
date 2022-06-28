package uk.nhs.prm.repo.re_registration.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpClient {

    private final RestTemplate restTemplate;

    public HttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> get(String uri, String userName, String password) {
        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(getHeaders()), String.class);
    }

    private HttpHeaders getHeaders() {
        return new HttpHeaders();
    }
}
