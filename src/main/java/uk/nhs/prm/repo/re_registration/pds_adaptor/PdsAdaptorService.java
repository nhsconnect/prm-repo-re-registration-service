package uk.nhs.prm.repo.re_registration.pds_adaptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import uk.nhs.prm.repo.re_registration.http.HttpClient;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.pds_adaptor.model.PdsAdaptorSuspensionStatusResponse;

@Slf4j
@Service
public class PdsAdaptorService {


    private HttpClient httpClient;
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

    public void getPatientPdsStatus(ReRegistrationEvent reRegistrationEvent) {
        var url = getPatientUrl(reRegistrationEvent.getNhsNumber());
        try {
            var pdsAdaptorResponseEntity = httpClient.get(url, authUserName, authPassword);

            if (isSuccessful(pdsAdaptorResponseEntity)) {
                var parsedPdsAdaptorResponse = getParsedPdsAdaptorReponseBody(pdsAdaptorResponseEntity.getBody());
                handleSuccessfulResponse(parsedPdsAdaptorResponse, reRegistrationEvent);
            }
        } catch (HttpStatusCodeException e) {
            handleErrorResponse(reRegistrationEvent, e);
        }
    }

    private void handleSuccessfulResponse(PdsAdaptorSuspensionStatusResponse pdsAdaptorResponse, ReRegistrationEvent reRegistrationEvent) {
        if (pdsAdaptorResponse.isSuspended()) {
            reRegistrationAuditPublisher.sendMessage(new NonSensitiveDataMessage(reRegistrationEvent.getNemsMessageId(), "NO_ACTION:RE_REGISTRATION_FAILED_STILL_SUSPENDED"));
        } else {
            log.info("Patient is not suspended");
        }
    }

    private void handleErrorResponse(ReRegistrationEvent reRegistrationEvent, HttpStatusCodeException e) {

        if (e.getStatusCode().is4xxClientError()) {
            reRegistrationAuditPublisher.sendMessage(new NonSensitiveDataMessage(reRegistrationEvent.getNemsMessageId(),
                    "NO_ACTION:RE_REGISTRATION_FAILED_PDS_ERROR"));
        } else if (e.getStatusCode().is5xxServerError()) {
            throw new IntermittentErrorPdsException("Encountered error when calling pds get patient endpoint", e);
        }

    }

    private PdsAdaptorSuspensionStatusResponse getParsedPdsAdaptorReponseBody(String responseBody) {
        try {
            return new ObjectMapper().readValue(responseBody, PdsAdaptorSuspensionStatusResponse.class);
        } catch (Exception e) {
            log.error("Encountered Exception while trying to parse pds-adaptor response");
            throw new RuntimeException(e);
        }
    }

    private boolean isSuccessful(ResponseEntity<String> response) {
        return response.getStatusCode().is2xxSuccessful();
    }

    private String getPatientUrl(String nhsNumber) {
        return pdsAdaptorServiceUrl + "/" + nhsNumber;
    }
}
