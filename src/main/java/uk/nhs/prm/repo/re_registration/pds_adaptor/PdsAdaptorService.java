package uk.nhs.prm.repo.re_registration.pds_adaptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.repo.re_registration.http.HttpClient;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.pds_adaptor.model.PdsAdaptorSuspensionStatusResponse;

@Slf4j
public class PdsAdaptorService {
    @Autowired
    private HttpClient httpClient;
    @Autowired
    private ReRegistrationAuditPublisher reRegistrationAuditPublisher;

    private String pdsAdaptorServiceUrl;
    private String authPassword;
    private String authUserName;


    public PdsAdaptorService(HttpClient httpClient,
                             ReRegistrationAuditPublisher reRegistrationAuditPublisher,
                             @Value("${pdsAdaptor.serviceUrl}") String pdsAdaptorServiceUrl,
                             @Value("${pdsAdaptor.authUserName}") String authUserName,
                             @Value("${pdsAdaptor.authPassword}") String authPassword) {

        this.httpClient = httpClient;
        this.reRegistrationAuditPublisher = reRegistrationAuditPublisher;
        this.pdsAdaptorServiceUrl = pdsAdaptorServiceUrl;
        this.authUserName = authUserName;
        this.authPassword = authPassword;
    }

    public void getPatientPdsStatus(ReRegistrationEvent reRegistrationEvent){
        var url = getPatientUrl(reRegistrationEvent.getNhsNumber());
        var pdsAdaptorResponse = httpClient.get(url, authUserName, authPassword);
        if (isSuccessful(pdsAdaptorResponse)) {
            var pdsAdaptorSuspensionStatusResponse = parseToPdsAdaptorResponse(pdsAdaptorResponse.getBody());
            handleSuccessfulResponse(pdsAdaptorSuspensionStatusResponse, reRegistrationEvent);
        }
    }

    private void handleSuccessfulResponse(PdsAdaptorSuspensionStatusResponse pdsAdaptorResponse, ReRegistrationEvent reRegistrationEvent) {
        if(pdsAdaptorResponse.isSuspended()){
            reRegistrationAuditPublisher.sendMessage(new NonSensitiveDataMessage(reRegistrationEvent.getNemsMessageId(),"NO_ACTION:RE_REGISTRATION_FAILED_STILL_SUSPENDED"));
        }
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

    private String getPatientUrl(String nhsNumber) {
        return pdsAdaptorServiceUrl + "/" + nhsNumber;
    }
}
