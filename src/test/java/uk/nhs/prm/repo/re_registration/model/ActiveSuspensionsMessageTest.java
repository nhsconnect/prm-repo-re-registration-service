package uk.nhs.prm.repo.re_registration.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.nhs.prm.repo.re_registration.utility.TestDataUtility.UNESCAPED_HTML;

class ActiveSuspensionsMessageTest {
    private static final String NHS_NUMBER = "1234567890";
    private static final String TIMESTAMP = "2024-02-01T11:40:03+0000";

    @Test
    void Given_ActiveSuspensionsMessageContainingUnescapedHtml_When_ToJsonStringIsCalled_Then_ReturnValueShouldNotContainUnescapedHtml() {
        // Given
        final ActiveSuspensionsMessage activeSuspensionsMessage = new ActiveSuspensionsMessage(
                NHS_NUMBER,
                UNESCAPED_HTML,
                TIMESTAMP
        );

        // When
        final String json = activeSuspensionsMessage.toJsonString();

        // Then
        assertFalse(json.contains(UNESCAPED_HTML));
    }
}