package uk.nhs.prm.repo.re_registration.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.data.ActiveSuspensionsDetailsDb;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveSuspensionsHandler {

    private final ActiveSuspensionsDetailsDb activeSuspensionsDetailsDb;

    public void handle(ActiveSuspensionsMessage activeSuspensionsMessage) {
        log.info("RECEIVED: Active suspensions message.");
        activeSuspensionsDetailsDb.save(activeSuspensionsMessage);
        log.info("Successfully stored active suspensions message in db.");
    }
}
