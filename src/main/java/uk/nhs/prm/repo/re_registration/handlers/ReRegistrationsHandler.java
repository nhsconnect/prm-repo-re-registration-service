package uk.nhs.prm.repo.re_registration.handlers;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.parser.ReRegistrationParser;
import uk.nhs.prm.repo.re_registration.pds_adaptor.IntermittentErrorPdsException;
import uk.nhs.prm.repo.re_registration.pds_adaptor.PdsAdaptorService;

import java.util.function.Function;

@Slf4j
@Component
public class ReRegistrationsHandler {

    private static final Class RETRYABLE_EXCEPTION_CLASS = IntermittentErrorPdsException.class;

    private final ReRegistrationParser parser;
    PdsAdaptorService pdsAdaptorService;
    private int maxAttempts;
    private int initialIntervalMillis;
    private double multiplier;

    public ReRegistrationsHandler(ReRegistrationParser parser, PdsAdaptorService pdsAdaptorService,
                                  @Value("${pdsIntermittentError.retry.max.attempts}")
                                          int maxAttempts,
                                  @Value("${pdsIntermittentError.initial.interval.millisecond}")
                                          int initialIntervalMillis,
                                  @Value("${pdsIntermittentError.initial.interval.multiplier}")
                                          double multiplier) {
        this.parser = parser;
        this.pdsAdaptorService = pdsAdaptorService;
        this.maxAttempts = maxAttempts;
        this.initialIntervalMillis = initialIntervalMillis;
        this.multiplier = multiplier;
    }

    public void handle(String message) {
        Function<String, Void> retryableProcessEvent = Retry
                .decorateFunction(Retry.of("retryablePdsIntermittentError", createRetryConfig()), this::processOnce);
        retryableProcessEvent.apply(message);
    }

    public Void processOnce(String payload) {
        try {
            log.info("RECEIVED: Re-registrations Event Message, payload length: " + payload.length());
            var reRegistrationEvent = parser.parse(payload);
            pdsAdaptorService.getPatientPdsStatus(reRegistrationEvent);
        } catch (Exception e) {
            if (RETRYABLE_EXCEPTION_CLASS.isInstance(e)) {
                log.info("Caught retryable exception in ReRegistrationsHandler", e);
            } else {
                log.error("Uncaught exception in ReRegistrationsHandler", e);
            }
            throw e;
        }
        return null;
    }

    private RetryConfig createRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(initialIntervalMillis, multiplier))
                .retryExceptions(RETRYABLE_EXCEPTION_CLASS)
                .build();
    }
}