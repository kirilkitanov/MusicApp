package app.notification.service;


import app.notification.client.EmailClient;
import app.notification.client.dto.CreatePreference;
import app.notification.client.dto.EmailPreference;
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

        ResponseEntity<EmailPreference> httpResponse = emailClient.getUserPrefernce(userId);

        return httpResponse.getBody();
    }
}
