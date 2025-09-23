package app.service;

import app.model.Email;
import app.model.EmailPreference;
import app.model.EmailStatus;
import app.repository.EmailPreferenceRepository;
import app.repository.EmailRepository;
import app.web.dto.PreferenceRequest;
import app.web.dto.SendEmailRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceUTest {
    @Mock
    private EmailPreferenceRepository emailPreferenceRepository;

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private MailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void givenPreferenceExists_whenUpsertPreference_thenUpdateFields() {
        // Given
        UUID userId = UUID.randomUUID();
        EmailPreference existing = EmailPreference.builder()
                .userId(userId)
                .active(false)
                .emailAddress("old@mail.com")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        when(emailPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(emailPreferenceRepository.save(existing)).thenReturn(existing);

        PreferenceRequest request = new PreferenceRequest();
        request.setUserId(userId);
        request.setPreferenceActive(true);
        request.setEmailAddress("new@mail.com");

        // When
        EmailPreference result = emailService.upsertPreference(request);

        // Then
        assertTrue(result.isActive());
        assertEquals("new@mail.com", result.getEmailAddress());
        verify(emailPreferenceRepository, times(1)).save(existing);
    }

    @Test
    void givenPreferenceDoesNotExist_whenUpsertPreference_thenCreateNew() {
        // Given
        UUID userId = UUID.randomUUID();
        when(emailPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        EmailPreference saved = EmailPreference.builder()
                .userId(userId)
                .active(true)
                .emailAddress("new@mail.com")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        when(emailPreferenceRepository.save(any())).thenReturn(saved);

        PreferenceRequest request = new PreferenceRequest();
        request.setUserId(userId);
        request.setPreferenceActive(true);
        request.setEmailAddress("new@mail.com");

        // When
        EmailPreference result = emailService.upsertPreference(request);

        // Then
        assertTrue(result.isActive());
        assertEquals("new@mail.com", result.getEmailAddress());
    }

    @Test
    void givenPreferenceDisabled_whenSendEmail_thenStatusSkipped() {
        // Given
        UUID userId = UUID.randomUUID();
        EmailPreference preference = EmailPreference.builder()
                .userId(userId)
                .active(false)
                .emailAddress("user@mail.com")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        when(emailPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));

        Email skippedEmail = Email.builder()
                .userId(userId)
                .subject("Hello")
                .body("Body")
                .status(EmailStatus.SKIPPED)
                .createdOn(LocalDateTime.now())
                .build();
        when(emailRepository.save(any())).thenReturn(skippedEmail);

        SendEmailRequest request = new SendEmailRequest();
        request.setUserId(userId);
        request.setSubject("Hello");
        request.setBody("Body");

        // When
        Email result = emailService.sendEmail(request);

        // Then
        assertEquals(EmailStatus.SKIPPED, result.getStatus());
    }

    @Test
    void givenPreferenceEnabled_whenSendEmail_thenStatusSucceeded() {
        // Given
        UUID userId = UUID.randomUUID();
        EmailPreference preference = EmailPreference.builder()
                .userId(userId)
                .active(true)
                .emailAddress("user@mail.com")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        when(emailPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));

        Email succeededEmail = Email.builder()
                .userId(userId)
                .subject("Hello")
                .body("Body")
                .status(EmailStatus.SUCCEEDED)
                .createdOn(LocalDateTime.now())
                .build();
        when(emailRepository.save(any())).thenReturn(succeededEmail);

        SendEmailRequest request = new SendEmailRequest();
        request.setUserId(userId);
        request.setSubject("Hello");
        request.setBody("Body");

        // When
        Email result = emailService.sendEmail(request);

        // Then
        assertEquals(EmailStatus.SUCCEEDED, result.getStatus());
        verify(emailRepository, times(1)).save(any(Email.class));
    }

    @Test
    void givenNoPreference_whenGetPreferenceByUserId_thenCreateDefault() {
        // Given
        UUID userId = UUID.randomUUID();
        when(emailPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        EmailPreference saved = EmailPreference.builder()
                .userId(userId)
                .active(false)
                .emailAddress("")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        when(emailPreferenceRepository.save(any())).thenReturn(saved);

        // When
        EmailPreference result = emailService.getPreferenceByUserId(userId);

        // Then
        assertFalse(result.isActive());
        assertEquals("", result.getEmailAddress());
        assertEquals(userId, result.getUserId());
    }
}
