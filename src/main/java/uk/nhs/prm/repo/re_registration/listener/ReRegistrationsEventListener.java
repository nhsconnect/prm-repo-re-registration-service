package uk.nhs.prm.repo.re_registration.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.repo.re_registration.config.Tracer;
import uk.nhs.prm.repo.re_registration.handlers.ReRegistrationsHandler;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Slf4j
@RequiredArgsConstructor
public class ReRegistrationsEventListener implements MessageListener {

    private final Tracer tracer;
    private final ReRegistrationsHandler reRegistrationsHandler;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContext(message);
            var payload = ((TextMessage) message).getText();
            reRegistrationsHandler.handle(payload);
            message.acknowledge();
            log.info("ACKNOWLEDGED: Re-registrations Event Message");
        }  catch (Exception e) {
            log.error("Failure to handle message", e);
        }
    }
}
