package uk.nhs.prm.repo.re_registration.services.ehrRepo;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.nhs.prm.repo.re_registration.config.Tracer;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
public class EhrRepoClientTest {
    @RegisterExtension
    WireMockExtension wireMock = new WireMockExtension();
    static Tracer tracer = mock(Tracer.class);
    static UUID traceId = UUID.randomUUID();

    @BeforeAll
    static void setUp() {
        when(tracer.getTraceId()).thenReturn(String.valueOf(traceId));
    }

}