package uk.nhs.prm.repo.re_registration.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.prm.repo.re_registration.logging.TestLogAppender.addTestLogAppender;

class ReRegistrationsProcessorTest {

    private ReRegistrationsProcessor processor;

    @BeforeEach
    public void setUp() {
        processor = new ReRegistrationsProcessor();
    }

    @Test
    public void shouldLogTheLengthOfMessageReceived() {
        var testLogAppender = addTestLogAppender();

        processor.process(getParsedMessage());

        var loggedEvent = testLogAppender.findLoggedEvent("RECEIVED");
        assertThat(loggedEvent.getMessage()).endsWith("length: 157");
    }

    private ReRegistrationEvent getParsedMessage(){
        return new ReRegistrationEvent("1234567890", "ABC123", UUID.randomUUID().toString(), "2017-11-01T15:00:33+00:00");
    }
}