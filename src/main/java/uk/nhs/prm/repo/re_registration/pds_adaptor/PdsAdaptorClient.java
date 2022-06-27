package uk.nhs.prm.repo.re_registration.pds_adaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.repo.re_registration.http.HttpClient;

public class PdsAdaptorClient {
    @Autowired
    HttpClient httpClient;

    String pdsAdaptorServiceUrl;
    String authPassword;
    String authUserName;


    public PdsAdaptorClient(HttpClient httpClient,
                            @Value("${pdsAdaptor.serviceUrl}") String pdsAdaptorServiceUrl,
                            @Value("${pdsAdaptor.authPassword}") String authPassword,
                            @Value("${pdsAdaptor.authUserName}") String authUserName) {

        this.httpClient = httpClient;
        this.pdsAdaptorServiceUrl = pdsAdaptorServiceUrl;
        this.authPassword = authPassword;
        this.authUserName = authUserName;
    }

    public void getPatientPdsStatus() {
        httpClient.get(pdsAdaptorServiceUrl, authUserName, authPassword);
    }
}
