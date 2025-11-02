package app.notification.service;

import app.notification.client.EmailClient;
import app.notification.client.dto.CreatePreference;
import app.notification.client.dto.SendEmailRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceUTest {

    @Mock
    private EmailClient emailClient;

    @InjectMocks
    private EmailService emailService;

    @Test
    void givenValidPreference_whenSavePreference_thenCallEmailClient() {

        UUID userId = UUID.randomUUID();
        when(emailClient.createPreference(any(CreatePreference.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        emailService.savePreference(userId, true, "user@email.com");

        verify(emailClient, times(1)).createPreference(any(CreatePreference.class));
    }

    @Test
    void givenValidRequest_whenSendEmail_thenCallEmailClient() {

        UUID userId = UUID.randomUUID();
        when(emailClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        emailService.sendEmail(userId, "Subject", "Body");

        verify(emailClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void givenInvalidResponse_whenSavePreference_thenLogsError() {

        UUID userId = UUID.randomUUID();
        when(emailClient.createPreference(any(CreatePreference.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        emailService.savePreference(userId, true, "user@email.com");

        verify(emailClient, times(1)).createPreference(any(CreatePreference.class));
    }

    @Test
    void givenInvalidResponse_whenSendEmail_thenLogsError() {

        UUID userId = UUID.randomUUID();
        when(emailClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        emailService.sendEmail(userId, "Subject", "Body");

        verify(emailClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }
}
