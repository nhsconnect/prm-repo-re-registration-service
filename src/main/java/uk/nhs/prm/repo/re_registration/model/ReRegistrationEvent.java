package uk.nhs.prm.repo.re_registration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ReRegistrationEvent {
    String nhsNumber;
    String newlyRegisteredOdsCode;
    String nemsMessageId;
    String lastUpdated;

}
