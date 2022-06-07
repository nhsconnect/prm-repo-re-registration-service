package uk.nhs.prm.repo.re_registration.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        processor.process("123");

        var loggedEvent = testLogAppender.findLoggedEvent("RECEIVED");
        assertThat(loggedEvent.getMessage()).endsWith("length: 3");
    }
}