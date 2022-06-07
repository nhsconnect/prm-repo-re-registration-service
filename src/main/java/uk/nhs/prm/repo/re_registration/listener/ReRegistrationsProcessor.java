package uk.nhs.prm.repo.re_registration.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReRegistrationsProcessor {
    public void process(String payload) {
        log.info("RECEIVED: Re-registrations Event Message, payload length: " + payload.length());
    }
}