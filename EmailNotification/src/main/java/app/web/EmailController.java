package app.web;

import app.model.EmailPreference;
import app.service.EmailService;
import app.web.dto.Preference;
import app.web.dto.EmailPreferenceResponse;
import app.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/email")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/preferences")
    public ResponseEntity<EmailPreferenceResponse> createPreference(@RequestBody Preference preference){

       EmailPreference emailPreference = emailService.createPreference(preference);

        EmailPreferenceResponse emailPreferenceResponse = DtoMapper.fromEmailPreference(emailPreference);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(emailPreferenceResponse);
    }

    @PutMapping("/preferences")
    public ResponseEntity<EmailPreferenceResponse> updatePreference(@RequestBody Preference preference) {

        EmailPreference emailPreference = emailService.updatePreference(preference);

        EmailPreferenceResponse emailPreferenceResponse = DtoMapper.fromEmailPreference(emailPreference);

        return ResponseEntity
                .status(HttpStatus.OK)
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

}
