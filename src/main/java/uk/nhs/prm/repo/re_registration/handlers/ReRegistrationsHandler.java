package uk.nhs.prm.repo.re_registration.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.pds_adaptor.IntermittentErrorPdsException;
import uk.nhs.prm.repo.re_registration.pds_adaptor.PdsAdaptorService;

@Slf4j
@Component
public class ReRegistrationsHandler {


    PdsAdaptorService pdsAdaptorService;

    public ReRegistrationsHandler(PdsAdaptorService pdsAdaptorService) {
        this.pdsAdaptorService = pdsAdaptorService;
    }

    public void handle(ReRegistrationEvent reRegistrationEvent) {
        try {
            pdsAdaptorService.getPatientPdsStatus(reRegistrationEvent);
            log.info("RECEIVED: Re-registrations Event Message, payload length: " + reRegistrationEvent.toJsonString().length());
        } catch (Exception e) {
            throw new IntermittentErrorPdsException(e.getMessage(), e);
        }
    }
}