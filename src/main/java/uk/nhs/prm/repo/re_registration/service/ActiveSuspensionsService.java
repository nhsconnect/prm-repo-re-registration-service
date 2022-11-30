package uk.nhs.prm.repo.re_registration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.re_registration.data.ActiveSuspensionsDb;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

@Service
@Slf4j
public class ActiveSuspensionsService {
    private final ActiveSuspensionsDb activeSuspensionsDb;

    public ActiveSuspensionsService(ActiveSuspensionsDb activeSuspensionsDb) {
        this.activeSuspensionsDb = activeSuspensionsDb;
    }

    public ActiveSuspensionsMessage checkActiveSuspension(ReRegistrationEvent reRegistrationEvent) {
        return activeSuspensionsDb.getByNhsNumber(reRegistrationEvent.getNhsNumber());
    }

    public void deleteRecord(ActiveSuspensionsMessage activeSuspensionsRecord, ReRegistrationEvent reRegistrationEvent) {
        log.info("Re-registration event received for suspended patient. From {} to {} at {}", activeSuspensionsRecord.getPreviousOdsCode(), reRegistrationEvent.getNewlyRegisteredOdsCode(), reRegistrationEvent.getLastUpdated());

        if (!activeSuspensionsRecord.getPreviousOdsCode().equals(reRegistrationEvent.getNewlyRegisteredOdsCode())) {
            log.info("Patient has been re-registered at a different GP practice");
        }

        activeSuspensionsDb.deleteByNhsNumber(activeSuspensionsRecord.getNhsNumber());
        log.info("Successfully deleted active-suspensions record from the DB.");
    }
}
