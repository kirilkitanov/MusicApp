package app;

import app.model.EmailPreference;
import app.repository.EmailPreferenceRepository;
import app.service.EmailService;
import app.web.dto.PreferenceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class EmailServiceITest {
    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailPreferenceRepository emailPreferenceRepository;

    @Test
    void upsertAndGetPreference_happyPath() {

        UUID userId = UUID.randomUUID();

        PreferenceRequest request = new PreferenceRequest();
        request.setUserId(userId);
        request.setPreferenceActive(true);
        request.setEmailAddress("test@email.com");

        EmailPreference preference = emailService.upsertPreference(request);

        assertEquals(userId, preference.getUserId());
        assertEquals(true, preference.isActive());
        assertEquals("test@email.com", preference.getEmailAddress());

        EmailPreference fetchedPreference = emailService.getPreferenceByUserId(userId);

        assertEquals(userId, fetchedPreference.getUserId());
        assertEquals(true, fetchedPreference.isActive());
        assertEquals("test@email.com", fetchedPreference.getEmailAddress());
        assertEquals(1, emailPreferenceRepository.count());
    }
}
