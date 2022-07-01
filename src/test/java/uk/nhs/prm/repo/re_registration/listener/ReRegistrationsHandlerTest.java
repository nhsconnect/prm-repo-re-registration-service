package uk.nhs.prm.repo.re_registration.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.handlers.ReRegistrationsHandler;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.parser.ReRegistrationParser;
import uk.nhs.prm.repo.re_registration.pds_adaptor.IntermittentErrorPdsException;
import uk.nhs.prm.repo.re_registration.pds_adaptor.PdsAdaptorService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.nhs.prm.repo.re_registration.logging.TestLogAppender.addTestLogAppender;

@ExtendWith(MockitoExtension.class)
class ReRegistrationsHandlerTest {

    @Mock
    PdsAdaptorService pdsAdaptorService;
    @Mock
    ReRegistrationParser parser;

    private ReRegistrationsHandler handler;


    @BeforeEach
    public void setUp() {
        handler = new ReRegistrationsHandler(parser, pdsAdaptorService,3,1,2.0);
    }

    @Test
    public void shouldLogTheLengthOfMessageReceived() {
        var testLogAppender = addTestLogAppender();

        handler.handle(getParsedMessage().toJsonString());

        var loggedEvent = testLogAppender.findLoggedEvent("RECEIVED");
        assertThat(loggedEvent.getMessage()).endsWith("length: 157");
    }

    @Test
    void shouldCallPdsAdaptorServiceToGetPatientsPdsStatusWhenHandleMessageIsInvoked() {
        var reRegistrationEvent = getParsedMessage();
        when(parser.parse(any())).thenReturn(reRegistrationEvent);
        handler.handle(reRegistrationEvent.toJsonString());
        verify(pdsAdaptorService, times(1)).getPatientPdsStatus(reRegistrationEvent);
    }

    @Test
    public void shouldLogRetryableExceptionIfIntermittentErrorPdsExceptionIsThrown() {
        var testLogAppender = addTestLogAppender();

        doThrow(IntermittentErrorPdsException.class).when(pdsAdaptorService).getPatientPdsStatus(any());

        assertThrows(IntermittentErrorPdsException.class, () -> handler.handle(getParsedMessage().toJsonString()));

        var loggedEvent = testLogAppender.findLoggedEvent("Caught retryable exception in ReRegistrationsHandler");
        assertThat(loggedEvent).isNotNull();
    }

    private ReRegistrationEvent getParsedMessage() {
        return new ReRegistrationEvent("1234567890", "ABC123", UUID.randomUUID().toString(), "2017-11-01T15:00:33+00:00");
    }
}