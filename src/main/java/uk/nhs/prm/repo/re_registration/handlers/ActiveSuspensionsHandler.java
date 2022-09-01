package uk.nhs.prm.repo.re_registration.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveSuspensionsHandler {
    public void handle(ActiveSuspensionsMessage activeSuspensionsMessage) {
        log.info("Received message from active suspensions queue.");
    }
}
