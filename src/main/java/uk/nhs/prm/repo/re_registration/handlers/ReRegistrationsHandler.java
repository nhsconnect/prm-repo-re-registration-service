package uk.nhs.prm.repo.re_registration.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import uk.nhs.prm.repo.re_registration.config.ToggleConfig;
import uk.nhs.prm.repo.re_registration.ehr_repo.EhrRepoServerException;
import uk.nhs.prm.repo.re_registration.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.AuditMessage;
import uk.nhs.prm.repo.re_registration.model.DeleteAuditMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.parser.ReRegistrationParser;
import uk.nhs.prm.repo.re_registration.pds.IntermittentErrorPdsException;
import uk.nhs.prm.repo.re_registration.pds.PdsAdaptorService;
import uk.nhs.prm.repo.re_registration.service.ActiveSuspensionsService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReRegistrationsHandler {

    private final ReRegistrationParser parser;
    private final PdsAdaptorService pdsAdaptorService;
    private final ToggleConfig toggleConfig;
    private final ReRegistrationAuditPublisher auditPublisher;
    private final EhrRepoService ehrRepoService;
    private final ActiveSuspensionsService activeSuspensionsService;

    public void process(String payload) {
        log.info("RECEIVED: Re-registrations Event Message, payload length: " + payload.length());
        var reRegistrationEvent = parser.parse(payload);

        if (toggleConfig.canSendDeleteEhrRequest()) {
            log.info("Toggle canSendDeleteEhrRequest is true: processing event to delete ehr");
            var activeSuspensionsRecord = activeSuspensionsService.checkActiveSuspension(reRegistrationEvent);
            if(activeSuspensionsRecord!=null){
                processReRegistration(reRegistrationEvent);
                activeSuspensionsService.handleActiveSuspensions(activeSuspensionsRecord, reRegistrationEvent);
            }else{
                log.info("Not a re-registration for MOF updated patient.");
                sendAuditMessage(reRegistrationEvent, "NO_ACTION:UNKNOWN_REGISTRATION_EVENT_RECEIVED");
            }
        } else {
            log.info("Toggle canSendDeleteEhrRequest is false: not processing event, sending update to audit");
            sendAuditMessage(reRegistrationEvent, "NO_ACTION:RE_REGISTRATION_EVENT_RECEIVED");
        }
    }

    private void processReRegistration(ReRegistrationEvent reRegistrationEvent) {
        try {
            log.info("Invoking pds to check patient status...");
            var pdsAdaptorResponse = pdsAdaptorService.getPatientPdsStatus(reRegistrationEvent);
            if (pdsAdaptorResponse.isSuspended()) {
                log.info("Patient is suspended, no need to invoke ehr repo to delete records");
                sendAuditMessage(reRegistrationEvent, "NO_ACTION:RE_REGISTRATION_FAILED_STILL_SUSPENDED");
                return;
            }
        } catch (HttpStatusCodeException e) {
            handlePdsErrorResponse(reRegistrationEvent, e);
            return;
        }
        log.info("Patient is not suspended, going ahead invoking ehr repo to delete records");
        deleteEhr(reRegistrationEvent);
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
        try {
            var ehrDeleteResponse = ehrRepoService.deletePatientEhr(reRegistrationEvent);
            var deleteAuditMessage = new DeleteAuditMessage(reRegistrationEvent.getNemsMessageId(), ehrDeleteResponse.getConversationIds());
            auditPublisher.sendMessage(deleteAuditMessage);
        } catch (HttpStatusCodeException e) {
            handleEhrRepoErrorResponse(reRegistrationEvent, e);
        } catch (JsonProcessingException e) {
            log.info("error during the mapping of delete response");
        }
    }

    private void handleEhrRepoErrorResponse(ReRegistrationEvent reRegistrationEvent, HttpStatusCodeException e) {
        if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            log.info("Encountered client error with status code : {}", e.getStatusCode());
            auditPublisher.sendMessage(new AuditMessage(reRegistrationEvent.getNemsMessageId(), "NO_ACTION:RE_REGISTRATION_EHR_NOT_IN_REPO"));
        } else if (e.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
            log.info("Encountered client error with status code : {}", e.getStatusCode());
            auditPublisher.sendMessage(new AuditMessage(reRegistrationEvent.getNemsMessageId(), "NO_ACTION:RE_REGISTRATION_EHR_FAILED_TO_DELETE"));
        } else if (e.getStatusCode().is5xxServerError()) {
            log.info("Encountered server error with status code : {}", e.getStatusCode());
            throw new EhrRepoServerException("Encountered error when calling ehr-repo DELETE patient endpoint", e);
        }
    }

    private void sendAuditMessage(ReRegistrationEvent reRegistrationEvent, String status) {
        auditPublisher.sendMessage(new AuditMessage(reRegistrationEvent.getNemsMessageId(), status));
    }
}