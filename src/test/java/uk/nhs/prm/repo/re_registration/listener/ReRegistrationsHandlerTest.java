package uk.nhs.prm.repo.re_registration.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.re_registration.handlers.ReRegistrationsHandler;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.prm.repo.re_registration.logging.TestLogAppender.addTestLogAppender;

class ReRegistrationsHandlerTest {

    private ReRegistrationsHandler processor;

    @BeforeEach
    public void setUp() {
        processor = new ReRegistrationsHandler();
    }

    @Test
    public void shouldLogTheLengthOfMessageReceived() {
        var testLogAppender = addTestLogAppender();

        processor.handle(getParsedMessage());

        var loggedEvent = testLogAppender.findLoggedEvent("RECEIVED");
        assertThat(loggedEvent.getMessage()).endsWith("length: 157");
    }

    private ReRegistrationEvent getParsedMessage(){
        return new ReRegistrationEvent("1234567890", "ABC123", UUID.randomUUID().toString(), "2017-11-01T15:00:33+00:00");
    }
}