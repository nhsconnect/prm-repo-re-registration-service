package uk.nhs.prm.repo.re_registration.ehr_repo;

public class IntermittentErrorEhrRepoException extends RuntimeException {
    public IntermittentErrorEhrRepoException(String message, Throwable ex) {
        super(message, ex);
    }
}