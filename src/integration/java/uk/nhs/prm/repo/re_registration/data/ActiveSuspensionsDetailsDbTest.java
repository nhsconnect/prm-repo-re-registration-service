package uk.nhs.prm.repo.re_registration.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.re_registration.infra.LocalStackAwsConfig;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest()
@ContextConfiguration(classes = { LocalStackAwsConfig.class})
@DirtiesContext
class ActiveSuspensionsDetailsDbTest {

    @Autowired
    ActiveSuspensionsDetailsDb activeSuspensionsDetailsDb;

    private String nhsNumber = "0987654321";
    private String previousOdsCode = "TEST00";
    private String nemsLastUpdatedDate = "2022-09-01T15:00:33+00:00";

    @Test
    void shouldSaveAndRetrieveActiveSuspensionFromDb() {
        activeSuspensionsDetailsDb.save(getActiveSuspensionMessage());

        var activeSuspensionsDetails = activeSuspensionsDetailsDb.getByNhsNumber(nhsNumber);
        assertThat(activeSuspensionsDetails.getNhsNumber()).isEqualTo(nhsNumber);
        assertThat(activeSuspensionsDetails.getPreviousOdsCode()).isEqualTo(previousOdsCode);
        assertThat(activeSuspensionsDetails.getNemsLastUpdatedDate()).isEqualTo(nemsLastUpdatedDate);
    }

    @Test
    void shouldHandleNhsNumberThatDoesNotExistInDb() {
        var notExistingNhsNumber = "9898989898";
        var nonExistentActiveSuspensionData = activeSuspensionsDetailsDb.getByNhsNumber(notExistingNhsNumber);
        assertThat(nonExistentActiveSuspensionData).isEqualTo(null);
    }

    private ActiveSuspensionsMessage getActiveSuspensionMessage() {
        return new ActiveSuspensionsMessage(nhsNumber, previousOdsCode, nemsLastUpdatedDate);
    }

}