package uk.nhs.prm.repo.re_registration.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.pds_adaptor.IntermittentErrorPdsException;
import uk.nhs.prm.repo.re_registration.pds_adaptor.PdsAdaptorService;

@Slf4j
@Component
public class ReRegistrationsHandler {

    private static final Class RETRYABLE_EXCEPTION_CLASS = IntermittentErrorPdsException.class;

    PdsAdaptorService pdsAdaptorService;

    public ReRegistrationsHandler(PdsAdaptorService pdsAdaptorService) {
        this.pdsAdaptorService = pdsAdaptorService;
    }

    public void handle(ReRegistrationEvent reRegistrationEvent) {
        try {
            log.info("RECEIVED: Re-registrations Event Message, payload length: " + reRegistrationEvent.toJsonString().length());
            pdsAdaptorService.getPatientPdsStatus(reRegistrationEvent);
        } catch (Exception e) {
            if (RETRYABLE_EXCEPTION_CLASS.isInstance(e)) {
                log.info("Caught retryable exception in ReRegistrationsHandler", e);
            }
            else {
                log.error("Uncaught exception in ReRegistrationsHandler", e);
            }
            throw e;
        }
    }
}