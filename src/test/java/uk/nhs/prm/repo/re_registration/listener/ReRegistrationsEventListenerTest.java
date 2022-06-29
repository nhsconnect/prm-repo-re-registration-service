package uk.nhs.prm.repo.re_registration.listener;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.config.Tracer;
import uk.nhs.prm.repo.re_registration.parser.ReRegistrationParser;

import javax.jms.JMSException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.nhs.prm.repo.re_registration.logging.TestLogAppender.addTestLogAppender;

@ExtendWith(MockitoExtension.class)
class ReRegistrationsEventListenerTest {

    @Mock
    Tracer tracer;

    @Mock
    ReRegistrationsProcessor reRegistrationsProcessor;

    @Mock
    ReRegistrationParser reRegistrationParser;

    @InjectMocks
    private ReRegistrationsEventListener reRegistrationsEventListener;

    @Test
    void shouldStartTracingWhenReceivesAMessage() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));

        reRegistrationsEventListener.onMessage(message);
        verify(tracer).setMDCContext(message);
    }

    @Test
    void shouldAcknowledgeMessageAfterSuccessfulProcessingOfTheMessageBody() throws JMSException {
        var payload = "re-registrations-message";
        SQSTextMessage message = spy(new SQSTextMessage(payload));
        reRegistrationsEventListener.onMessage(message);
        verify(reRegistrationsProcessor).process(payload);
        verify(message).acknowledge();
    }

    @Test
    void shouldLogExceptionsAsErrorsInProcessingWithoutAcknowledgingMessage() throws JMSException {
        var logged = addTestLogAppender();
        var exception = new IllegalStateException("some exception");
        var message = spy(new SQSTextMessage("not-a-re-registrations-message"));

        doThrow(exception).when(reRegistrationsProcessor).process(anyString());

        reRegistrationsEventListener.onMessage(message);

        assertThat(logged.findLoggedEvent("Failure to handle message").getThrowableProxy().getMessage()).isEqualTo("some exception");
        verify(message, never()).acknowledge();
    }
}