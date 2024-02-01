package uk.nhs.prm.repo.re_registration.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.nhs.prm.repo.re_registration.utility.TestDataUtility.UNESCAPED_HTML;

class AuditMessageTest {
    private static final String NEMS_MESSAGE_ID = "1234567890";

    @Test
    void Given_AuditMessageContainingUnescapedHtml_When_ToJsonStringIsCalled_Then_ReturnValueShouldNotContainUnescapedHtml() {
        // Given
        final AuditMessage auditMessage = new AuditMessage(
                NEMS_MESSAGE_ID,
                UNESCAPED_HTML
        );

        // When
        final String json = auditMessage.toJsonString();

        // Then
        assertFalse(json.contains(UNESCAPED_HTML));
    }
}