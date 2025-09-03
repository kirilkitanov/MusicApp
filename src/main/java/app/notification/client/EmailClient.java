package app.notification.client;

import app.notification.client.dto.CreatePreference;
import app.notification.client.dto.EmailPreference;
import app.notification.client.dto.SendEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "emailNotification", url = "http://localhost:8081/api/v1/emails")
public interface EmailClient {


    @PostMapping("/preferences")
    ResponseEntity<Void> createPreference (@RequestBody CreatePreference createPreference);

    @GetMapping("/preferences")
    ResponseEntity<EmailPreference> getUserPreference(@RequestParam (name = "userId") UUID userID);

    @PostMapping()
    ResponseEntity<Void> sendEmail(@RequestBody SendEmailRequest sendEmailRequest);

}
