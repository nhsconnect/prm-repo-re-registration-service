package uk.nhs.prm.repo.re_registration.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import uk.nhs.prm.repo.re_registration.config.ToggleConfig;
import uk.nhs.prm.repo.re_registration.ehr_repo.EhrDeleteResponseContent;
import uk.nhs.prm.repo.re_registration.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.re_registration.ehr_repo.IntermittentErrorEhrRepoException;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.parser.ReRegistrationParser;
import uk.nhs.prm.repo.re_registration.pds.IntermittentErrorPdsException;
import uk.nhs.prm.repo.re_registration.pds.PdsAdaptorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReRegistrationsHandler {

    private final ReRegistrationParser parser;
    private final PdsAdaptorService pdsAdaptorService;
    private final ToggleConfig toggleConfig;
    private final ReRegistrationAuditPublisher auditPublisher;
    private final EhrRepoService ehrRepoService;

    public void process(String payload) {

        log.info("RECEIVED: Re-registrations Event Message, payload length: " + payload.length());
        var reRegistrationEvent = parser.parse(payload);

        if (toggleConfig.canSendDeleteEhrRequest()) {
            try {
                checkSuspendedStatus(reRegistrationEvent);
            } catch (Exception e) {
                throw e;
            }
        } else {
            log.info("Toggle canSendDeleteEhrRequest is false: not processing event, sending update to audit");
            sendAuditMessage(reRegistrationEvent, "NO_ACTION:RE_REGISTRATION_EVENT_RECEIVED");
        }
    }

    private void handlePdsResponse(ReRegistrationEvent reRegistrationEvent, boolean isSuspended) {
        if (isSuspended) {
            log.info("Patient is suspended");
            sendAuditMessage(reRegistrationEvent, "NO_ACTION:RE_REGISTRATION_FAILED_STILL_SUSPENDED");
        } else {
            log.info("Patient is not suspended");
            deleteEhr(reRegistrationEvent);
        }
    }

    private void checkSuspendedStatus(ReRegistrationEvent reRegistrationEvent) {
        try {
            var parsedPdsAdaptorResponse = pdsAdaptorService.getPatientPdsStatus(reRegistrationEvent);
            handlePdsResponse(reRegistrationEvent, parsedPdsAdaptorResponse.isSuspended());
        } catch (HttpStatusCodeException e) {
            handlePdsErrorResponse(reRegistrationEvent, e);
        }
    }

    private void handlePdsErrorResponse(ReRegistrationEvent reRegistrationEvent, HttpStatusCodeException e) {

        if (e.getStatusCode().is4xxClientError()) {
            log.info("Encountered client error with status code : {}", e.getStatusCode());
            sendAuditMessage(reRegistrationEvent, "NO_ACTION:RE_REGISTRATION_FAILED_PDS_ERROR");
        } else if (e.getStatusCode().is5xxServerError()) {
            log.info("Caught retryable exception: " + e.getMessage());
            log.info("Encountered server error with status code : {}", e.getStatusCode());
            throw new IntermittentErrorPdsException("Encountered error when calling pds get patient endpoint", e);
        }

    }

    private void deleteEhr(ReRegistrationEvent reRegistrationEvent) {
        log.info("Toggle canSendDeleteEhrRequest is true: processing event to delete ehr");
        EhrDeleteResponseContent ehrDeleteResponse;
        try{
            ehrDeleteResponse = ehrRepoService.deletePatientEhr(reRegistrationEvent);
            sendAuditMessage(reRegistrationEvent, "ACTION:RE_REGISTRATION_EHR_DELETED with conversationIds: " + ehrDeleteResponse.getConversationIds());
        }catch (HttpStatusCodeException e) {
            handleEhrRepoErrorResponse(reRegistrationEvent, e);
        }
    }

    private void handleEhrRepoErrorResponse(ReRegistrationEvent reRegistrationEvent, HttpStatusCodeException e) {

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

    private void sendAuditMessage(ReRegistrationEvent reRegistrationEvent, String status) {
        auditPublisher.sendMessage(new NonSensitiveDataMessage(reRegistrationEvent.getNemsMessageId(), status));
    }
}