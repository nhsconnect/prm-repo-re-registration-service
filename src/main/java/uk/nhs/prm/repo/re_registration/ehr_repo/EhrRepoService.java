package uk.nhs.prm.repo.re_registration.ehr_repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import uk.nhs.prm.repo.re_registration.config.Tracer;
import uk.nhs.prm.repo.re_registration.http.HttpClient;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

import java.io.IOException;
import java.net.http.HttpResponse;

@Slf4j
@Service
public class EhrRepoService {
    private final String ehrRepoUrl;
    private final String ehrRepoAuthKey;
    private final Tracer tracer;
    private final ReRegistrationAuditPublisher auditPublisher;
    private final HttpClient httpClient;

    public EhrRepoService(@Value("${ehrRepoUrl}") String ehrRepoUrl, @Value("${ehrRepoAuthKey}") String ehrRepoAuthKey, Tracer tracer, ReRegistrationAuditPublisher auditPublisher, HttpClient httpClient) {
        this.ehrRepoUrl = ehrRepoUrl;
        this.ehrRepoAuthKey = ehrRepoAuthKey;
        this.tracer = tracer;
        this.auditPublisher = auditPublisher;
        this.httpClient = httpClient;
    }

    public EhrDeleteResponseContent deletePatientEhr(ReRegistrationEvent reRegistrationEvent) throws IOException, InterruptedException {

        var url = getPatientDeleteEhrUrl(reRegistrationEvent.getNhsNumber());
        try {
            log.info("Making a DELETE EHR Request to ehr-repo");
            var ehrRepoResponse = httpClient.delete(url, ehrRepoAuthKey);

            if (isDeleteRequestSuccessful(ehrRepoResponse)) {
                return getParsedDeleteEhrResponseBody(ehrRepoResponse.body());
            } else {
                throw new RuntimeException();
            }
        } catch (HttpStatusCodeException e) {
            handleErrorResponse(reRegistrationEvent, e);
            throw e;
        } catch (Exception e) {
            log.error("Error during the performing ehr delete request.");
            throw e;
        }
    }

    private void handleErrorResponse(ReRegistrationEvent reRegistrationEvent, HttpStatusCodeException e) {

        if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            log.info("Encountered client error with status code : {}", e.getStatusCode());
            auditPublisher.sendMessage(new NonSensitiveDataMessage(reRegistrationEvent.getNemsMessageId(),
                    "NO_ACTION:RE_REGISTRATION_EHR_NOT_IN_REPO"));
        } else if (e.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
            log.info("Encountered client error with status code : {}", e.getStatusCode());
            auditPublisher.sendMessage(new NonSensitiveDataMessage(reRegistrationEvent.getNemsMessageId(),
                    "NO_ACTION:RE_REGISTRATION_EHR_FAILED_TO_DELETE"));
        } else if (e.getStatusCode().is5xxServerError()) {
            log.info("Encountered server error with status code : {}", e.getStatusCode());
            throw new IntermittentErrorEhrRepoException("Encountered error when calling ehr-repo DELETE patient endpoint", e);
        }

    }

    private EhrDeleteResponseContent getParsedDeleteEhrResponseBody(String responseBody) {
        try {
            log.info("Trying to parse ehr-repo response");
            return new ObjectMapper().readValue(responseBody, EhrDeleteResponse.class).getEhrDeleteResponseContent();
        } catch (Exception e) {
            log.error("Encountered Exception while trying to request patient DELETE ehr");
            throw new RuntimeException(e);
        }
    }

    private boolean isDeleteRequestSuccessful(HttpResponse<String> response) {
        log.info("response body: " + response.body());
        return response.statusCode() == 200;
    }

    private String getPatientDeleteEhrUrl(String nhsNumber) {
        return ehrRepoUrl + "/patients/" + nhsNumber;
    }
}
