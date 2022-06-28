package uk.nhs.prm.repo.re_registration.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.repo.re_registration.model.NonSensitiveDataMessage;

public class ReRegistrationAuditPublisher {

    MessagePublisher messagePublisher;
    String reRegistrationAuditTopicArn;

    public ReRegistrationAuditPublisher(@Value("${reRegistration.auditTopic.arn}") String reRegistrationAuditTopicArn, MessagePublisher messagePublisher) {
        this.reRegistrationAuditTopicArn = reRegistrationAuditTopicArn;
        this.messagePublisher = messagePublisher;
    }

    public void sendMessage(NonSensitiveDataMessage message) {
        messagePublisher.sendMessage(reRegistrationAuditTopicArn, message.toJsonString());
    }
}
