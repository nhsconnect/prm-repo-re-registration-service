package uk.nhs.prm.repo.re_registration.audit;

public enum AuditMessages {

    UNKNOWN_REREGISTRATIONS("NO_ACTION:UNKNOWN_REGISTRATION_EVENT_RECEIVED"),
    NOT_PROCESSING_REREGISTRATIONS("NO_ACTION:RE_REGISTRATION_EVENT_RECEIVED"),
    STILL_SUSPENDED("NO_ACTION:RE_REGISTRATION_FAILED_STILL_SUSPENDED"),
    PDS_ERROR("NO_ACTION:RE_REGISTRATION_FAILED_PDS_ERROR"),
    EHR_NOT_IN_REPO("NO_ACTION:RE_REGISTRATION_EHR_NOT_IN_REPO"),
    FAILURE_TO_DELETE_EHR("NO_ACTION:RE_REGISTRATION_EHR_FAILED_TO_DELETE");

    private String status;

    AuditMessages(String status) {
        this.status = status;
    }
    public String status() {
        return status;
    }
}
