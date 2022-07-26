package uk.nhs.prm.repo.re_registration.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.config.ToggleConfig;
import uk.nhs.prm.repo.re_registration.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.parser.ReRegistrationParser;
import uk.nhs.prm.repo.re_registration.pds.PdsAdaptorService;
import uk.nhs.prm.repo.re_registration.pds.model.PdsAdaptorSuspensionStatusResponse;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReRegistrationsHandler {

    private final ReRegistrationParser parser;
    private final PdsAdaptorService pdsAdaptorService;
    private final ToggleConfig toggleConfig;
    private final ReRegistrationAuditPublisher auditPublisher;
    private final EhrRepoService ehrRepoService;

    public void process(String payload) throws IOException, InterruptedException {

        log.info("RECEIVED: Re-registrations Event Message, payload length: " + payload.length());
        var reRegistrationEvent = parser.parse(payload);

        if (toggleConfig.canSendDeleteEhrRequest()) {
            var parsedPdsAdaptorResponse = pdsAdaptorService.getPatientPdsStatus(reRegistrationEvent);
            var isSuspended = checkSuspendedStatus(parsedPdsAdaptorResponse);
            handlePdsResponse(reRegistrationEvent, isSuspended);
        } else {
            log.info("Toggle canSendDeleteEhrRequest is false: not processing event, sending update to audit");
            sendAuditMessage(reRegistrationEvent, "NO_ACTION:RE_REGISTRATION_EVENT_RECEIVED");
        }
    }

    private void handlePdsResponse(ReRegistrationEvent reRegistrationEvent, boolean isSuspended) throws IOException, InterruptedException {
        if (isSuspended) {
            log.info("Patient is suspended");
            sendAuditMessage(reRegistrationEvent, "NO_ACTION:RE_REGISTRATION_FAILED_STILL_SUSPENDED");
        } else {
            log.info("Patient is not suspended");
            deleteEhr(reRegistrationEvent);
        }
    }

    private boolean checkSuspendedStatus(PdsAdaptorSuspensionStatusResponse pdsAdaptorResponse) {
        return pdsAdaptorResponse.isSuspended();
    }

    private void deleteEhr(ReRegistrationEvent reRegistrationEvent) throws IOException, InterruptedException {
        log.info("Toggle canSendDeleteEhrRequest is true: processing event to delete ehr");
        var ehrDeleteResponse = ehrRepoService.deletePatientEhr(reRegistrationEvent);
        sendAuditMessage(reRegistrationEvent, "ACTION:RE_REGISTRATION_EHR_DELETED with conversationIds: " + ehrDeleteResponse.getConversationIds());

    }

    private void sendAuditMessage(ReRegistrationEvent reRegistrationEvent, String status) {
        auditPublisher.sendMessage(new NonSensitiveDataMessage(reRegistrationEvent.getNemsMessageId(), status));
    }
}