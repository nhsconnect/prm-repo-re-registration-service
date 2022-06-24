package uk.nhs.prm.repo.re_registration.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class HttpClient {

    private final RestTemplate restTemplate;

    public HttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void makeAGetCall(String uri){
        var exchange = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(getHeaders()), String.class);
    }

    private HttpHeaders getHeaders() {
        var httpHeaders = new HttpHeaders();
        return httpHeaders;
    }
}
