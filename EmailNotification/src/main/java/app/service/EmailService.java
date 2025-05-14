package app.service;

import app.model.EmailPreference;
import app.repository.EmailPreferenceRepository;
import app.repository.EmailRepository;
import app.web.dto.Preference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailService {

    private final EmailPreferenceRepository emailPreferenceRepository;
    private final EmailRepository emailRepository;
    private final MailSender mailSender;

    @Autowired
    public EmailService(EmailPreferenceRepository emailPreferenceRepository, EmailRepository emailRepository, MailSender mailSender) {
        this.emailPreferenceRepository = emailPreferenceRepository;
        this.emailRepository = emailRepository;
        this.mailSender = mailSender;
    }


    public EmailPreference createPreference(Preference preference) {

        Optional<EmailPreference> userEmailPreferenceOptional = emailPreferenceRepository.findByUserId(preference.getUserId());

        if (userEmailPreferenceOptional.isPresent()) {
            throw new RuntimeException("Email preference for user [%s] already exists.".formatted(preference.getUserId()));
        }

        EmailPreference emailPreference = EmailPreference.builder()
                .userId(preference.getUserId())
                .active(preference.isPreferenceActive())
                .emailAddress(preference.getEmailAddress())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        return emailPreferenceRepository.save(emailPreference);
    }


    public EmailPreference updatePreference(Preference preference) {

        Optional<EmailPreference> userEmailPreferenceOptional = emailPreferenceRepository.findByUserId(preference.getUserId());

        if (userEmailPreferenceOptional.isEmpty()) {
            throw new RuntimeException("Preference not found for user [%s].".formatted(preference.getUserId()));
        }

        EmailPreference emailPreference = userEmailPreferenceOptional.get();
        emailPreference.setActive(preference.isPreferenceActive());
        emailPreference.setEmailAddress(preference.getEmailAddress());
        emailPreference.setUpdatedOn(LocalDateTime.now());

        return emailPreferenceRepository.save(emailPreference);
    }


    public EmailPreference getPreferenceByUserId(UUID userId) {

        return emailPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Preference not found for user [%s].".formatted(userId)));
    }
}


