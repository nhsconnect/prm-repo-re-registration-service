package uk.nhs.prm.repo.re_registration.metrics.healthprobes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import uk.nhs.prm.repo.re_registration.metrics.AppConfig;

@Component
@Slf4j
public class ReRegistrationsQueueHealthProbe implements HealthProbe {
    private final AppConfig config;
    private final SqsClient sqsClient;

    @Autowired
    public ReRegistrationsQueueHealthProbe(AppConfig config, SqsClient sqsClient) {
        this.config = config;
        this.sqsClient = sqsClient;
    }

    @Override
    public boolean isHealthy() {
        return true;
    }

}
