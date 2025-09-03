package app.notification.service;


import app.notification.client.EmailClient;
import app.notification.client.dto.CreatePreference;
import app.notification.client.dto.EmailPreference;
import app.notification.client.dto.SendEmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class EmailService {

    private final EmailClient emailClient;

    @Autowired
    public EmailService(EmailClient emailClient) {
        this.emailClient = emailClient;
    }

public void savePreference(UUID userID, boolean isPreferenceActive, String emailAddress) {

        CreatePreference createPreference = CreatePreference.builder()
                .userId(userID)
                .preferenceActive(isPreferenceActive)
                .emailAddress(emailAddress)
                .build();
    ResponseEntity<Void> httpResponse = emailClient.createPreference(createPreference);

    if (!httpResponse.getStatusCode().is2xxSuccessful())
        log.error("Cannot create preference for user [%s]".formatted(userID));
}

    public EmailPreference getEmailPreference(UUID userId) {

        ResponseEntity<EmailPreference> httpResponse = emailClient.getUserPreference(userId);

        return httpResponse.getBody();
    }

    public void sendEmail(UUID userId, String subject, String body) {
        SendEmailRequest request = SendEmailRequest.builder()
                .userId(userId)
                .subject(subject)
                .body(body)
                .build();

        ResponseEntity<Void> httpResponse = emailClient.sendEmail(request);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Cannot send email to user [%s]".formatted(userId));
        }
    }
}
