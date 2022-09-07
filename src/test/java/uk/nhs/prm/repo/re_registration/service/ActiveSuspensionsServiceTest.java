package uk.nhs.prm.repo.re_registration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.data.ActiveSuspensionsDb;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActiveSuspensionsServiceTest {

    @Mock
    private ActiveSuspensionsDb activeSuspensionsDb;

    @InjectMocks
    private ActiveSuspensionsService activeSuspensionsService;

    String nhsNumber = "1234567890";

    @Test
    void shouldReturnNullWhenThereIsNoActiveSuspensionRecordFoundInDb(){
        when(activeSuspensionsDb.getByNhsNumber(any())).thenReturn(null);
        activeSuspensionsService.checkActiveSuspension(getReRegistrationEvent());
        assertNull(activeSuspensionsDb.getByNhsNumber(nhsNumber));
    }

    @Test
    public void shouldInvokeCallToDbWhenRequestedToGetByNhsNumber(){
        var activeSuspensionRecord = activeSuspensionsService.checkActiveSuspension(getReRegistrationEvent());
        verify(activeSuspensionsDb).getByNhsNumber(getActiveSuspensionsMessage().getNhsNumber());
    }

    private ReRegistrationEvent getReRegistrationEvent() {
        return new ReRegistrationEvent(nhsNumber, "new-ods-code", "nemsMessageId", "last-updated-reregistration");
    }

    private ActiveSuspensionsMessage getActiveSuspensionsMessage() {
        return new ActiveSuspensionsMessage(nhsNumber, "previous-ods-code", "last-updated-suspension");
    }
}