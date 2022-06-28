package uk.nhs.prm.repo.re_registration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ReRegistrationEvent {
    private String nhsNumber;
    private String newlyRegisteredOdsCode;
    private String nemsMessageId;
    private String lastUpdated;

}
