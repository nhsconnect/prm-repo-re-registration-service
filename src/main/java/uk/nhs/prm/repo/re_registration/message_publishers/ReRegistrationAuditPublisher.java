package uk.nhs.prm.repo.re_registration.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.model.NonSensitiveDataMessage;

@Component
public class ReRegistrationAuditPublisher {

    private final MessagePublisher messagePublisher;
    private final String reRegistrationAuditTopicArn;

    public ReRegistrationAuditPublisher(MessagePublisher messagePublisher, @Value("${reRegistration.auditTopic.arn}") String reRegistrationAuditTopicArn) {
        this.messagePublisher = messagePublisher;
        this.reRegistrationAuditTopicArn = reRegistrationAuditTopicArn;
    }

    public void sendMessage(NonSensitiveDataMessage message) {
        messagePublisher.sendMessage(reRegistrationAuditTopicArn, message.toJsonString());
    }
}
