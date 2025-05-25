package app.notification.client;

import app.notification.client.dto.CreatePreference;
import app.notification.client.dto.EmailPreference;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "emailNotification", url = "http://localhost:8081/api/v1/emails")
public interface EmailClient {


    @PostMapping("/preferences")
    ResponseEntity<Void> createPreference (@RequestBody CreatePreference createPreference);

    @GetMapping("/preferences")
    ResponseEntity<EmailPreference> getUserPrefernce(@RequestParam (name = "userId") UUID userID);

}
