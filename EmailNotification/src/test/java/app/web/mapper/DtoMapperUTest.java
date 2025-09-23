package app.web.mapper;

import app.model.Email;
import app.model.EmailPreference;
import app.model.EmailStatus;
import app.web.dto.EmailPreferenceResponse;
import app.web.dto.EmailResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DtoMapperUTest {

    @Test
    void givenHappyPath_whenMappingEmailPreferenceToResponse() {

        // Given
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EmailPreference emailPreference = EmailPreference.builder()
                .id(id)
                .userId(userId)
                .active(true)
                .emailAddress("user@mail.com")
                .build();

        // When
        EmailPreferenceResponse resultDto = DtoMapper.fromEmailPreference(emailPreference);

        // Then
        assertEquals(emailPreference.getId(), resultDto.getId());
        assertEquals(emailPreference.getUserId(), resultDto.getUserId());
        assertEquals(emailPreference.isActive(), resultDto.isActive());
        assertEquals(emailPreference.getEmailAddress(), resultDto.getEmailAddress());
    }

    @Test
    void givenHappyPath_whenMappingEmailToResponse() {

        // Given
        Email email = Email.builder()
                .status(EmailStatus.SUCCEEDED)
                .build();

        // When
        EmailResponse resultDto = DtoMapper.fromEmail(email);

        // Then
        assertEquals(email.getStatus(), resultDto.getStatus());
    }
}
