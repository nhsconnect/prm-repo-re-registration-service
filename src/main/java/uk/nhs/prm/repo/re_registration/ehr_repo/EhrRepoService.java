package uk.nhs.prm.repo.re_registration.ehr_repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.re_registration.config.Tracer;
import uk.nhs.prm.repo.re_registration.http.HttpClient;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

@Slf4j
@Service
public class EhrRepoService {
    private final String ehrRepoUrl;
    private final String ehrRepoAuthKey;
    private final Tracer tracer;
    private final ReRegistrationAuditPublisher auditPublisher;
    private HttpClient httpClient;

    public EhrRepoService(@Value("${ehrRepoUrl}") String ehrRepoUrl, @Value("${ehrRepoAuthKey}") String ehrRepoAuthKey, Tracer tracer, ReRegistrationAuditPublisher auditPublisher, HttpClient httpClient) {
        this.ehrRepoUrl = ehrRepoUrl;
        this.ehrRepoAuthKey = ehrRepoAuthKey;
        this.tracer = tracer;
        this.auditPublisher = auditPublisher;
        this.httpClient = httpClient;
    }

    public EhrDeleteResponseContent deletePatientEhr(ReRegistrationEvent reRegistrationEvent) {

        var url = getPatientDeleteEhrUrl(reRegistrationEvent.getNhsNumber());
        try {
            log.info("Making a DELETE EHR Request to ehr-repo");
            var ehrRepoResponse = httpClient.delete(url, ehrRepoAuthKey);
            return getParsedDeleteEhrResponseBody(ehrRepoResponse.getBody());
        } catch (Exception e) {
            log.error("Error during the performing ehr delete request." + e.getMessage());
            throw e;
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

    private String getPatientDeleteEhrUrl(String nhsNumber) {
        return ehrRepoUrl + "/patients/" + nhsNumber;
    }
}
