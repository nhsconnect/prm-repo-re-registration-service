package uk.nhs.prm.repo.re_registration.services.ehrRepo;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.http.HttpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.nhs.prm.repo.re_registration.config.Tracer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
public class EhrRepoClientTest {
    @RegisterExtension
    WireMockExtension wireMock = new WireMockExtension();
    static UUID traceId = UUID.randomUUID();

    Tracer tracer = mock(Tracer.class);

    EhrRepoClient ehrRepoClient;

    @BeforeEach
     void setUp() throws MalformedURLException {
        when(tracer.getTraceId()).thenReturn(String.valueOf(traceId));
        ehrRepoClient = new EhrRepoClient(wireMock.baseUrl(), "secret", tracer);
    }

    @Test
    public void shouldCallDeleteEhrEndpointInEhrRepoService() throws IOException, URISyntaxException, InterruptedException, HttpException {
        String nhsNumber = "1234567890";

        wireMock.stubFor(delete(urlEqualTo("/patients/" + nhsNumber)).withHeader("Authorization", equalTo("secret"))
                .withHeader("traceId", equalTo(String.valueOf(traceId)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"type\":\"patients\", \"id\":\"1234567890\", \"conversationIds\":[\"2431d4ff-f760-4ab9-8cd8-a3fc47846762\"," + "\"c184cc19-86e9-4a95-b5b5-2f156900bb3c\"]}")
                        .withHeader("Content-Type", "application/json")));

        var actualEhrDeleteResponse = ehrRepoClient.deletePatientEhr(nhsNumber);
        var expectedEhrDeleteResponse = new EhrDeleteResponse("patients", nhsNumber, Arrays.asList("2431d4ff-f760-4ab9-8cd8-a3fc47846762", "c184cc19-86e9-4a95-b5b5-2f156900bb3c"));
        assertEquals(expectedEhrDeleteResponse, actualEhrDeleteResponse);
    }
}