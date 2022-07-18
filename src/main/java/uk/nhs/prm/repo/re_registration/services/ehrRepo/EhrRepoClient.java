package uk.nhs.prm.repo.re_registration.services.ehrRepo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.re_registration.config.Tracer;

import java.net.MalformedURLException;
import java.net.URL;

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
}
