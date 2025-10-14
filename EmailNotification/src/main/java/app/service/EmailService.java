package app.service;

import app.model.Email;
import app.model.EmailPreference;
import app.model.EmailStatus;
import app.repository.EmailPreferenceRepository;
import app.repository.EmailRepository;
import app.web.dto.PreferenceRequest;
import app.web.dto.SendEmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
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


//    public EmailPreference createPreference(PreferenceRequest preferenceRequest) {
//
//        Optional<EmailPreference> userEmailPreferenceOptional = emailPreferenceRepository.findByUserId(preferenceRequest.getUserId());
//
//        if (userEmailPreferenceOptional.isPresent()) {
//            throw new EmailPreferenceAlreadyExistsException("Email preference for user [%s] already exists.".formatted(preferenceRequest.getUserId()));
//        }
//
//        EmailPreference emailPreference = EmailPreference.builder()
//                .userId(preferenceRequest.getUserId())
//                .active(preferenceRequest.isPreferenceActive())
//                .emailAddress(preferenceRequest.getEmailAddress())
//                .createdOn(LocalDateTime.now())
//                .updatedOn(LocalDateTime.now())
//                .build();
//        return emailPreferenceRepository.save(emailPreference);
//    }


//    public EmailPreference updatePreference(PreferenceRequest preferenceRequest) {
//
//        Optional<EmailPreference> userEmailPreferenceOptional = emailPreferenceRepository.findByUserId(preferenceRequest.getUserId());
//
//        if (userEmailPreferenceOptional.isEmpty()) {
//            throw new EmailPreferenceNotFoundException("Preference not found for user [%s].".formatted(preferenceRequest.getUserId()));
//        }
//
//        EmailPreference emailPreference = userEmailPreferenceOptional.get();
//        emailPreference.setActive(preferenceRequest.isPreferenceActive());
//        emailPreference.setEmailAddress(preferenceRequest.getEmailAddress());
//        emailPreference.setUpdatedOn(LocalDateTime.now());
//
//        return emailPreferenceRepository.save(emailPreference);
//    }

    public EmailPreference upsertPreference(PreferenceRequest preferenceRequest) {

        Optional<EmailPreference> userEmailPreferenceOptional = emailPreferenceRepository.findByUserId(preferenceRequest.getUserId());

        EmailPreference emailPreference;
        if (userEmailPreferenceOptional.isPresent()) {

            emailPreference = userEmailPreferenceOptional.get();
            emailPreference.setActive(preferenceRequest.isPreferenceActive());
            emailPreference.setEmailAddress(preferenceRequest.getEmailAddress());
            emailPreference.setUpdatedOn(LocalDateTime.now());
        } else {
            emailPreference = EmailPreference.builder()
                    .userId(preferenceRequest.getUserId())
                    .active(preferenceRequest.isPreferenceActive())
                    .emailAddress(preferenceRequest.getEmailAddress())
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .build();
        }

        return emailPreferenceRepository.save(emailPreference);
    }


    public EmailPreference getPreferenceByUserId(UUID userId) {
        Optional<EmailPreference> existingPreference = emailPreferenceRepository.findByUserId(userId);

        if (existingPreference.isPresent()) {
            return existingPreference.get();
        }

        EmailPreference defaultPreference = EmailPreference.builder()
                .userId(userId)
                .active(false)
                .emailAddress("")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return emailPreferenceRepository.save(defaultPreference);
    }


    public Email sendEmail(SendEmailRequest sendEmailRequest) {

        UUID userId = sendEmailRequest.getUserId();
        EmailPreference userPreference = getPreferenceByUserId(userId);

        if (!userPreference.isActive()) {
            log.info("Skipping email for user {} because notifications are disabled", userId);
            return emailRepository.save(
                    Email.builder()
                            .userId(userId)
                            .subject(sendEmailRequest.getSubject())
                            .body(sendEmailRequest.getBody())
                            .status(EmailStatus.SKIPPED)
                            .createdOn(LocalDateTime.now())
                            .build()
            );
        }

        Email email = Email.builder().
                userId(userId)
                .subject(sendEmailRequest.getSubject())
                .body(sendEmailRequest.getBody())
                .createdOn(LocalDateTime.now())
                .build();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userPreference.getEmailAddress());
            message.setSubject(sendEmailRequest.getSubject());
            message.setText(sendEmailRequest.getBody());

            mailSender.send(message);

            email.setStatus(EmailStatus.SUCCEEDED);
            log.info("Successfully sent email to {}", userPreference.getEmailAddress());

        } catch (Exception e) {
            email.setStatus(EmailStatus.FAILED);
            log.warn("Failed to send email to {}", userPreference.getEmailAddress());
        }

        return emailRepository.save(email);
    }
}



