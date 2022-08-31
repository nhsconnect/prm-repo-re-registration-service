package uk.nhs.prm.repo.re_registration.listener;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.config.Tracer;
import uk.nhs.prm.repo.re_registration.handlers.ActiveSuspensionsHandler;

import javax.jms.JMSException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActiveSuspensionsMessageListenerTest {

    @Mock
    Tracer tracer;

    @Mock
    ActiveSuspensionsHandler activeSuspensionsHandler;

    @InjectMocks
    private ActiveSuspensionsMessageListener activeSuspensionsMessageListener;

    @Test
    void shouldStartTracingWhenReceivesAMessage() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));

        activeSuspensionsMessageListener.onMessage(message);
        verify(tracer).setMDCContext(message);
    }

    @Test
    void shouldHandleMessageWhenActiveSuspensionsEventReceived() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));
        activeSuspensionsMessageListener.onMessage(message);

        verify(activeSuspensionsHandler, times(1)).handle(payload);
    }

}