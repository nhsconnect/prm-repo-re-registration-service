package uk.nhs.prm.repo.re_registration.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.config.ToggleConfig;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.parser.ReRegistrationParser;
import uk.nhs.prm.repo.re_registration.pds_adaptor.PdsAdaptorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReRegistrationsHandler {

    private final ReRegistrationParser parser;
    private final PdsAdaptorService pdsAdaptorService;
    private final ToggleConfig toggleConfig;
    private final ReRegistrationAuditPublisher auditPublisher;

    public void process(String payload) {
        log.info("RECEIVED: Re-registrations Event Message, payload length: " + payload.length());
        var reRegistrationEvent = parser.parse(payload);
        if (toggleConfig.canSendDeleteEhrRequest()) {
            deleteEhr(reRegistrationEvent);
        } else {
            sendAuditMessage(reRegistrationEvent);
        }
    }

    private void deleteEhr(ReRegistrationEvent reRegistrationEvent) {
        log.info("Toggle canSendEhrRequest is true: processing event to delete ehr");
        pdsAdaptorService.getPatientPdsStatus(reRegistrationEvent);
    }

    private void sendAuditMessage(ReRegistrationEvent reRegistrationEvent) {
        log.info("Toggle canSendEhrRequest is false: not processing event, sending update to audit");
        auditPublisher.sendMessage(new NonSensitiveDataMessage(reRegistrationEvent.getNemsMessageId(), "NO_ACTION:RE_REGISTRATION_EVENT_RECEIVED"));
    }
}