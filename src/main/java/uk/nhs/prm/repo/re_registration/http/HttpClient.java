package uk.nhs.prm.repo.re_registration.http;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.nhs.prm.repo.re_registration.config.Tracer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class HttpClient {

    public static final String AUTHORIZATION = "Authorization";
    private final RestTemplate restTemplate;
    private final Tracer tracer;

    public ResponseEntity<String> get(String uri, String userName, String password) {
        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(getHeaders(userName, password)), String.class);
    }

    public HttpResponse<String> delete(String uri, String authKey) throws IOException, InterruptedException {

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", authKey)
                .header("Content-Type", "application/json")
                .header("traceId", tracer.getTraceId())
                .DELETE().build();

        return java.net.http.HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

//        return restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(getHeadersForEhrRepo(authKey)), String.class);
    }

    private HttpHeaders getHeadersForEhrRepo(String authKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add(AUTHORIZATION, authKey);
        headers.add("traceId", tracer.getTraceId());
        return headers;
    }

    private MultiValueMap<String, String> createHeader(String authKey) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        headers.add(AUTHORIZATION, authKey);
        headers.add("traceId", tracer.getTraceId());

        return headers;
    }

    private HttpHeaders getHeaders(String userName, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(userName, password);
        headers.add("traceId", tracer.getTraceId());
        return headers;
    }
}
