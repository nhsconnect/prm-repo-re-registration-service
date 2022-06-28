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
    HttpClient httpClient;
    @Autowired
    ReRegistrationAuditPublisher reRegistrationAuditPublisher;

    String pdsAdaptorServiceUrl;
    String authPassword;
    String authUserName;


    public PdsAdaptorService(HttpClient httpClient,
                             @Value("${pdsAdaptor.serviceUrl}") String pdsAdaptorServiceUrl,
                             @Value("${pdsAdaptor.authPassword}") String authPassword,
                             @Value("${pdsAdaptor.authUserName}") String authUserName,
                             ReRegistrationAuditPublisher reRegistrationAuditPublisher) {

        this.httpClient = httpClient;
        this.pdsAdaptorServiceUrl = pdsAdaptorServiceUrl;
        this.authPassword = authPassword;
        this.authUserName = authUserName;
        this.reRegistrationAuditPublisher = reRegistrationAuditPublisher;
    }

    public PdsAdaptorSuspensionStatusResponse getPatientPdsStatus(ReRegistrationEvent reRegistrationEvent) {
        var response = httpClient.get(pdsAdaptorServiceUrl, authUserName, authPassword);

        if (isSuccessful(response)) {
            var pdsAdaptorResponse = parseToPdsAdaptorResponse(response.getBody());
            handleEvent(pdsAdaptorResponse, reRegistrationEvent);
            return pdsAdaptorResponse;
        }

        return null;
    }

    private void handleEvent(PdsAdaptorSuspensionStatusResponse pdsAdaptorResponse, ReRegistrationEvent reRegistrationEvent) {
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
}
