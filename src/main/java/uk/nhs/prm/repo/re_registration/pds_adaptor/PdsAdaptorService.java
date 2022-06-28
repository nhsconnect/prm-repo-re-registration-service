package uk.nhs.prm.repo.re_registration.pds_adaptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.repo.re_registration.http.HttpClient;
import uk.nhs.prm.repo.re_registration.pds_adaptor.model.PdsAdaptorSuspensionStatusResponse;

@Slf4j
public class PdsAdaptorService {
    @Autowired
    HttpClient httpClient;

    String pdsAdaptorServiceUrl;
    String authPassword;
    String authUserName;


    public PdsAdaptorService(HttpClient httpClient,
                             @Value("${pdsAdaptor.serviceUrl}") String pdsAdaptorServiceUrl,
                             @Value("${pdsAdaptor.authPassword}") String authPassword,
                             @Value("${pdsAdaptor.authUserName}") String authUserName) {

        this.httpClient = httpClient;
        this.pdsAdaptorServiceUrl = pdsAdaptorServiceUrl;
        this.authPassword = authPassword;
        this.authUserName = authUserName;
    }

    public PdsAdaptorSuspensionStatusResponse getPatientPdsStatus() {
        var response = httpClient.get(pdsAdaptorServiceUrl, authUserName, authPassword);

        if (isSuccessful(response)) {
            var pdsAdaptorResponse = parseToPdsAdaptorResponse(response.getBody());
           return pdsAdaptorResponse;
        }

        return null;
    }

    private PdsAdaptorSuspensionStatusResponse parseToPdsAdaptorResponse(String responseBody) {
        try {

            return new ObjectMapper().readValue(responseBody, PdsAdaptorSuspensionStatusResponse.class);

        } catch (Exception e) {

            log.error("Encountered Exception while trying to parse pds-adaptor response");
            throw new RuntimeException(e);
        }
    }

    private boolean isSuccessful(org.springframework.http.ResponseEntity<String> response) {
        return response.getStatusCode().is2xxSuccessful();
    }
}
