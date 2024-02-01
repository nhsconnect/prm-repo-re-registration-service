package uk.nhs.prm.repo.re_registration.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.nhs.prm.repo.re_registration.utility.TestDataUtility.UNESCAPED_HTML;

class ReRegistrationEventTest {
    private static final String NHS_NUMBER = "1234567890";
    private static final String ODS_CODE = "B74158";
    private static final String TIMESTAMP = "2024-02-01T11:40:03+0000";

    @Test
    void Given_ReRegistrationEventContainingUnescapedHtml_When_ToJsonStringIsCalled_Then_ReturnValueShouldNotContainUnescapedHtml() {
        // Given
        final ReRegistrationEvent reRegistrationEvent = new ReRegistrationEvent(
                NHS_NUMBER,
                ODS_CODE,
                UNESCAPED_HTML,
                TIMESTAMP
        );

        // When
        final String json = reRegistrationEvent.toJsonString();

        // Then
        assertFalse(json.contains(UNESCAPED_HTML));
    }
}