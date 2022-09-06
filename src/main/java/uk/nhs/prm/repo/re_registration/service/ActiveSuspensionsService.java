package uk.nhs.prm.repo.re_registration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.re_registration.data.ActiveSuspensionsDb;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

@Service
@Slf4j
public class ActiveSuspensionsService {
    private ActiveSuspensionsDb activeSuspensionsDb;

    public ActiveSuspensionsService(ActiveSuspensionsDb activeSuspensionsDb) {
        this.activeSuspensionsDb = activeSuspensionsDb;
    }

    public ActiveSuspensionsMessage checkActiveSuspension(ReRegistrationEvent reRegistrationEvent){
        return activeSuspensionsDb.getByNhsNumber(reRegistrationEvent.getNhsNumber());
    }
}
