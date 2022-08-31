package uk.nhs.prm.repo.re_registration.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveSuspensionsHandler {
    public void handle(String payload) {
        log.info("Received message from active suspensions queue.");
    }
}
