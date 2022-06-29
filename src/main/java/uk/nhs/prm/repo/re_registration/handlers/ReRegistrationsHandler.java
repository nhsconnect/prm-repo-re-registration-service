package uk.nhs.prm.repo.re_registration.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

@Slf4j
@Component
public class ReRegistrationsHandler {
    public void handle(ReRegistrationEvent reRegistrationEvent) {
        log.info("RECEIVED: Re-registrations Event Message, payload length: " + reRegistrationEvent.toJsonString().length());
    }
}