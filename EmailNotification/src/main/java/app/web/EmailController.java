package app.web;

import app.model.Email;
import app.model.EmailPreference;
import app.service.EmailService;
import app.web.dto.EmailResponse;
import app.web.dto.PreferenceRequest;
import app.web.dto.EmailPreferenceResponse;
import app.web.dto.SendEmailRequest;
import app.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emails")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/preferences")
    public ResponseEntity<EmailPreferenceResponse> upsertPreference(@RequestBody PreferenceRequest preferenceRequest) {

        EmailPreference emailPreference = emailService.upsertPreference(preferenceRequest);

        EmailPreferenceResponse emailPreferenceResponse = DtoMapper.fromEmailPreference(emailPreference);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(emailPreferenceResponse);
    }

    @GetMapping("/preferences")
    public ResponseEntity<EmailPreferenceResponse> getUserPreference(@RequestParam(name = "userId") UUID userId) {

        EmailPreference emailPreference = emailService.getPreferenceByUserId(userId);

        EmailPreferenceResponse emailPreferenceResponse = DtoMapper.fromEmailPreference(emailPreference);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(emailPreferenceResponse);
    }

    @PostMapping()
    public ResponseEntity<EmailResponse> sendEmail(@RequestBody SendEmailRequest sendEmailRequest) {

        Email email = emailService.sendEmail(sendEmailRequest);

        EmailResponse emailResponse = DtoMapper.fromEmail(email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(emailResponse);
    }
}