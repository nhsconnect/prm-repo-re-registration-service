package uk.nhs.prm.repo.re_registration.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.config.ToggleConfig;
import uk.nhs.prm.repo.re_registration.message_publishers.ReRegistrationAuditPublisher;
import uk.nhs.prm.repo.re_registration.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.parser.ReRegistrationParser;
import uk.nhs.prm.repo.re_registration.pds_adaptor.PdsAdaptorService;
import uk.nhs.prm.repo.re_registration.pds_adaptor.model.PdsAdaptorSuspensionStatusResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.nhs.prm.repo.re_registration.logging.TestLogAppender.addTestLogAppender;

@ExtendWith(MockitoExtension.class)
class ReRegistrationsHandlerTest {

    @Mock
    private PdsAdaptorService pdsAdaptorService;

    @Mock
    private ReRegistrationParser parser;

    @Mock
    private ToggleConfig toggleConfig;

    @Mock
    private ReRegistrationAuditPublisher auditPublisher;

    @InjectMocks
    private ReRegistrationsHandler reRegistrationsHandler;

    private ReRegistrationEvent reRegistrationEvent = getParsedMessage();

    @BeforeEach
    void setUp(){
        when(parser.parse(any())).thenReturn(reRegistrationEvent);
    }

    @Test
    public void shouldLogTheLengthOfMessageReceived() {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(false);
        var testLogAppender = addTestLogAppender();
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        var loggedEvent = testLogAppender.findLoggedEvent("RECEIVED");
        assertThat(loggedEvent.getMessage()).endsWith("length: 134");
    }

    @Test
    void shouldCallPdsAdaptorServiceToGetPatientsPdsStatusWhenHandleMessageIsInvoked() {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenReturn(getPdsResponseStringWithSuspendedStatus(true));
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(pdsAdaptorService, times(1)).getPatientPdsStatus(reRegistrationEvent);
    }

    @Test
    void shouldNotCallPdsAndSendMessageToAuditTopicWithCanSendDeleteEhrRequestIsFalse() {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(false);
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verifyNoInteractions(pdsAdaptorService);
        var auditMessage = new NonSensitiveDataMessage(reRegistrationEvent.getNemsMessageId(), "NO_ACTION:RE_REGISTRATION_EVENT_RECEIVED");
        verify(auditPublisher).sendMessage(auditMessage);
    }

    @Test
    void shouldPublishToQueueWhenPatientIsSuspended() {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenReturn(getPdsResponseStringWithSuspendedStatus(true));
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(auditPublisher).sendMessage(new NonSensitiveDataMessage("nemsMessageId", "NO_ACTION:RE_REGISTRATION_FAILED_STILL_SUSPENDED"));
    }

    @Test
    void shouldNotPublishToQueueWhenPatientIsNotSuspended() {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenReturn(getPdsResponseStringWithSuspendedStatus(false));
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(auditPublisher, times(0)).sendMessage(any());
    }

    private ReRegistrationEvent getParsedMessage() {
        return new ReRegistrationEvent("1234567890", "ABC123", "nemsMessageId", "2017-11-01T15:00:33+00:00");
    }

    private PdsAdaptorSuspensionStatusResponse getPdsResponseStringWithSuspendedStatus(boolean isSuspended) {
        return new PdsAdaptorSuspensionStatusResponse("0000000000",isSuspended ,"currentOdsCode","managingOrganisation","etag",false);
    }
}