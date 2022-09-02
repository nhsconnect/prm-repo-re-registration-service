package uk.nhs.prm.repo.re_registration.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.data.ActiveSuspensionsDetailsDb;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ActiveSuspensionsHandlerTest {

    @Mock
    private ActiveSuspensionsDetailsDb activeSuspensionsDetailsDb;

    @InjectMocks
    private ActiveSuspensionsHandler activeSuspensionsHandler;

    @Test
    void shouldCallDbWithActiveSuspensionsMessage() {
        var activeSuspensionMessage = getActiveSuspensionsMessage();
        activeSuspensionsHandler.handle(activeSuspensionMessage);
        verify(activeSuspensionsDetailsDb).save(activeSuspensionMessage);
    }

    private ActiveSuspensionsMessage getActiveSuspensionsMessage() {
        return new ActiveSuspensionsMessage("some-nhs-number", "some-ods-code", "last-updated");
    }
}