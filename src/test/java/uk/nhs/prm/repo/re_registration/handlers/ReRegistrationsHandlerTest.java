package uk.nhs.prm.repo.re_registration.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.parser.ReRegistrationParser;
import uk.nhs.prm.repo.re_registration.pds_adaptor.PdsAdaptorService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.nhs.prm.repo.re_registration.logging.TestLogAppender.addTestLogAppender;

@ExtendWith(MockitoExtension.class)
class ReRegistrationsHandlerTest {

    @Mock
    private PdsAdaptorService pdsAdaptorService;

    @Mock
    private ReRegistrationParser parser;

    @InjectMocks
    private ReRegistrationsHandler reRegistrationsHandler;

    @Test
    public void shouldLogTheLengthOfMessageReceived() {
        var testLogAppender = addTestLogAppender();

        reRegistrationsHandler.process(getParsedMessage().toJsonString());

        var loggedEvent = testLogAppender.findLoggedEvent("RECEIVED");
        assertThat(loggedEvent.getMessage()).endsWith("length: 157");
    }

    @Test
    void shouldCallPdsAdaptorServiceToGetPatientsPdsStatusWhenHandleMessageIsInvoked() {
        var reRegistrationEvent = getParsedMessage();
        when(parser.parse(any())).thenReturn(reRegistrationEvent);
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(pdsAdaptorService, times(1)).getPatientPdsStatus(reRegistrationEvent);
    }

    private ReRegistrationEvent getParsedMessage() {
        return new ReRegistrationEvent("1234567890", "ABC123", UUID.randomUUID().toString(), "2017-11-01T15:00:33+00:00");
    }
}