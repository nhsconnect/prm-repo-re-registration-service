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
import uk.nhs.prm.repo.re_registration.pds.PdsAdaptorService;
import uk.nhs.prm.repo.re_registration.pds.model.PdsAdaptorSuspensionStatusResponse;
import uk.nhs.prm.repo.re_registration.ehr_repo.EhrDeleteResponse;

import java.util.Arrays;

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

    private ReRegistrationEvent reRegistrationEvent = createReRegistrationEvent();

    @BeforeEach
    void setUp(){
        when(parser.parse(any())).thenReturn(reRegistrationEvent);
    }

    @Test
    public void shouldLogTheLengthOfMessageReceived() throws Exception {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(false);
        var testLogAppender = addTestLogAppender();
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        var loggedEvent = testLogAppender.findLoggedEvent("RECEIVED");
        assertThat(loggedEvent.getMessage()).endsWith("length: 134");
    }

    @Test
    void shouldCallPdsAdaptorServiceToGetPatientsPdsStatusWhenHandleMessageIsInvoked() throws Exception {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenReturn(getPdsResponseStringWithSuspendedStatus(true));
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(pdsAdaptorService, times(1)).getPatientPdsStatus(reRegistrationEvent);
    }

    @Test
    void shouldNotCallPdsAndSendMessageToAuditTopicWithCanSendDeleteEhrRequestIsFalse() throws Exception {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(false);
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verifyNoInteractions(pdsAdaptorService);
        var auditMessage = new NonSensitiveDataMessage(reRegistrationEvent.getNemsMessageId(), "NO_ACTION:RE_REGISTRATION_EVENT_RECEIVED");
        verify(auditPublisher).sendMessage(auditMessage);
    }

    @Test
    void shouldPublishToQueueWhenPatientIsSuspendedWithNoAction() throws Exception {
        when(toggleConfig.canSendDeleteEhrRequest()).thenReturn(true);
        when(pdsAdaptorService.getPatientPdsStatus(any())).thenReturn(getPdsResponseStringWithSuspendedStatus(true));
        reRegistrationsHandler.process(reRegistrationEvent.toJsonString());
        verify(auditPublisher).sendMessage(new NonSensitiveDataMessage("nemsMessageId", "NO_ACTION:RE_REGISTRATION_FAILED_STILL_SUSPENDED"));
    }


    private ReRegistrationEvent createReRegistrationEvent() {
        return new ReRegistrationEvent("1234567890", "ABC123", "nemsMessageId", "2017-11-01T15:00:33+00:00");
    }

    private PdsAdaptorSuspensionStatusResponse getPdsResponseStringWithSuspendedStatus(boolean isSuspended) {
        return new PdsAdaptorSuspensionStatusResponse("0000000000",isSuspended ,"currentOdsCode","managingOrganisation","etag",false);
    }

    private EhrDeleteResponse createSuccessfulEhrDeleteResponse() {
        return new EhrDeleteResponse("patients", "1234567890", Arrays.asList("2431d4ff-f760-4ab9-8cd8-a3fc47846762", "c184cc19-86e9-4a95-b5b5-2f156900bb3c"));
    }
}