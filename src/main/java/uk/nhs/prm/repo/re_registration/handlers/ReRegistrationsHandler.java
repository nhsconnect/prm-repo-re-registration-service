package uk.nhs.prm.repo.re_registration.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.parser.ReRegistrationParser;
import uk.nhs.prm.repo.re_registration.pds_adaptor.PdsAdaptorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReRegistrationsHandler {

    private final ReRegistrationParser parser;
    private final PdsAdaptorService pdsAdaptorService;

    public void process(String payload) {
        log.info("RECEIVED: Re-registrations Event Message, payload length: " + payload.length());
        var reRegistrationEvent = parser.parse(payload);
        pdsAdaptorService.getPatientPdsStatus(reRegistrationEvent);
    }
}