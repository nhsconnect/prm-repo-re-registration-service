package uk.nhs.prm.repo.re_registration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.data.ActiveSuspensionsDb;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
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
        when(activeSuspensionsDb.getByNhsNumber(any())).thenReturn(any());
        activeSuspensionsService.checkActiveSuspension(getReRegistrationEvent());
        assertNull(activeSuspensionsDb.getByNhsNumber(nhsNumber));
    }

    private ReRegistrationEvent getReRegistrationEvent() {
        return new ReRegistrationEvent("1234567890", "ABC123", "nemsMessageId", "2017-11-01T15:00:33+00:00");
    }
}