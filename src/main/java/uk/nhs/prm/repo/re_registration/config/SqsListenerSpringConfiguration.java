package uk.nhs.prm.repo.re_registration.config;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazon.sqs.javamessaging.SQSSession;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.nhs.prm.repo.re_registration.handlers.ActiveSuspensionsHandler;
import uk.nhs.prm.repo.re_registration.handlers.ReRegistrationsRetryHandler;
import uk.nhs.prm.repo.re_registration.listener.ActiveSuspensionsMessageListener;
import uk.nhs.prm.repo.re_registration.listener.ReRegistrationsEventListener;
import uk.nhs.prm.repo.re_registration.parser.ActiveSuspensionsParser;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SqsListenerSpringConfiguration {

    @Value("${aws.reRegistrationsQueueName}")
    private String reRegistrationsQueueName;

    @Value("${aws.activeSuspensionsQueueName}")
    private String activeSuspensionsQueueName;

    private final Tracer tracer;
    private final ReRegistrationsRetryHandler reRegistrationsRetryHandler;
    private final ActiveSuspensionsHandler activeSuspensionsHandler;
    private final ActiveSuspensionsParser activeSuspensionsParser;

    @Bean
    public AmazonSQSAsync amazonSQSAsync() {
        return AmazonSQSAsyncClientBuilder.defaultClient();
    }

    @Bean
    public SQSConnection createConnection(AmazonSQSAsync amazonSQSAsync) throws JMSException {
        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(new ProviderConfiguration(), amazonSQSAsync);
        return connectionFactory.createConnection();
    }

    @Bean
    public Session createListeners(SQSConnection connection) throws JMSException {
        Session session = connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);
        log.info("Re-registrations event queue name : {}", reRegistrationsQueueName);
        MessageConsumer consumer = session.createConsumer(session.createQueue(reRegistrationsQueueName));

        consumer.setMessageListener(new ReRegistrationsEventListener(tracer, reRegistrationsRetryHandler));

        connection.start();

        return session;
    }

    @Bean
    public Session createActiveSuspensionsQueueListener(SQSConnection connection) throws JMSException {
        var session = getSession(connection);

        log.info("active suspensions queue name : {}", activeSuspensionsQueueName);
        var activeSuspensionsConsumer = session.createConsumer(session.createQueue(activeSuspensionsQueueName));
        activeSuspensionsConsumer.setMessageListener(new ActiveSuspensionsMessageListener(tracer, activeSuspensionsHandler));

        connection.start();

        return session;
    }

    private Session getSession(SQSConnection connection) throws JMSException {
        return connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);
    }

}
