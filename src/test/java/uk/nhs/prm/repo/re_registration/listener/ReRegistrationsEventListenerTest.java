package uk.nhs.prm.repo.re_registration.listener;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.config.Tracer;
import uk.nhs.prm.repo.re_registration.handlers.ReRegistrationsHandler;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.parser.ReRegistrationParser;

import javax.jms.JMSException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.nhs.prm.repo.re_registration.logging.TestLogAppender.addTestLogAppender;

@ExtendWith(MockitoExtension.class)
class ReRegistrationsEventListenerTest {

    @Mock
    Tracer tracer;

    @Mock
    ReRegistrationsHandler reRegistrationsHandler;

    @Mock
    ReRegistrationParser reRegistrationParser;

    @InjectMocks
    private ReRegistrationsEventListener reRegistrationsEventListener;

    private final static String NHS_NUMBER = "some-nhs-number";
    private final static String NEWLY_REGISTERED_ODS_CODE = "some-ods-code";
    private final static String NEMS_MESSAGE_ID = "some-nems-message-id";
    private final static String LAST_UPDATED = "last-updated";

    @Test
    void shouldStartTracingWhenReceivesAMessage() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));

        reRegistrationsEventListener.onMessage(message);
        verify(tracer).setMDCContext(message);
    }

    @Test
    void shouldAcknowledgeMessageAfterSuccessfulProcessingOfTheMessageBody() throws JMSException {
        var reRegistrationMessage = "{\"nhsNumber\":\"" + NHS_NUMBER + "\",\"newlyRegisteredOdsCode\":\"" + NEWLY_REGISTERED_ODS_CODE + "\", \"nemsMessageId\":\"" + NEMS_MESSAGE_ID + "\",\"lastUpdated\":\"" + LAST_UPDATED + "\"}";
        var parsedMessage = getParsedMessage();
        SQSTextMessage message = spy(new SQSTextMessage(reRegistrationMessage));
        when(reRegistrationParser.parse(any())).thenReturn(parsedMessage);
        reRegistrationsEventListener.onMessage(message);
        verify(reRegistrationsHandler).handle(parsedMessage);
        verify(message).acknowledge();
    }

    @Test
    void shouldLogExceptionsAsErrorsInProcessingWithoutAcknowledgingMessage() throws JMSException {
        var logged = addTestLogAppender();
        var exception = new IllegalStateException("some exception");
        var message = spy(new SQSTextMessage("not-a-re-registrations-message"));

        doThrow(exception).when(reRegistrationsHandler).handle(any());

        reRegistrationsEventListener.onMessage(message);

        assertThat(logged.findLoggedEvent("Failure to handle message").getThrowableProxy().getMessage()).isEqualTo("some exception");
        verify(message, never()).acknowledge();
    }

    private ReRegistrationEvent getParsedMessage(){
        return new ReRegistrationEvent("1234567890", "ABC123", UUID.randomUUID().toString(), "2017-11-01T15:00:33+00:00");
    }
}