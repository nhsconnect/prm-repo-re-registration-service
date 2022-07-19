package uk.nhs.prm.repo.re_registration.services.ehrRepo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.re_registration.config.Tracer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EhrRepoClient {
    private final URL ehrRepoUrl;
    private final String ehrRepoAuthKey;
    private final Tracer tracer;

    public EhrRepoClient(@Value("${ehrRepoUrl}") String ehrRepoUrl, @Value("${ehrRepoAuthKey}") String ehrRepoAuthKey, Tracer tracer) throws MalformedURLException {
        this.ehrRepoUrl = new URL(ehrRepoUrl);
        this.ehrRepoAuthKey = ehrRepoAuthKey;
        this.tracer = tracer;
    }

    public EhrDeleteResponse deletePatientEhr(String nhsNumber) throws IOException, URISyntaxException, InterruptedException, HttpException {
        String endpoint = "/patients/" + nhsNumber;
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URL(ehrRepoUrl, endpoint).toURI())
                .header("Authorization", ehrRepoAuthKey)
                .header("Content-Type", "application/json")
                .header("traceId", tracer.getTraceId())
                .DELETE().build();

        System.out.println("*** Sending httpRequest to EhrRepo as : " + httpRequest);

        var response = HttpClient.newBuilder()
                .build()
                .send(httpRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("ehrDeleteResponse coming from ehrRepo is : " + response.body());

        if (response.statusCode() != 200) {
            throw new HttpException(String.format("Unexpected response from EHR while checking if a message was stored: %d", response.statusCode()));
        }

        return new ObjectMapper().readValue(response.body(), EhrDeleteResponse.class);

    }

}
