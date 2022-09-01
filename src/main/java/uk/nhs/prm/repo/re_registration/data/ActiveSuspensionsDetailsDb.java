package uk.nhs.prm.repo.re_registration.data;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import uk.nhs.prm.repo.re_registration.metrics.AppConfig;

@Component
@RequiredArgsConstructor
public class ActiveSuspensionsDetailsDb {
    private final DynamoDbClient dynamoDbClient;
    private final AppConfig config;
}
