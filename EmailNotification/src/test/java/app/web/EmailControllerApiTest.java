package app.web;

import app.model.Email;
import app.model.EmailPreference;
import app.model.EmailStatus;
import app.service.EmailService;
import app.web.dto.PreferenceRequest;
import app.web.dto.SendEmailRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
public class EmailControllerApiTest {

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getUserPreference_shouldReturnEmailPreference() throws Exception {
        UUID userId = UUID.randomUUID();
        EmailPreference preference = new EmailPreference();
        preference.setId(UUID.randomUUID());
        preference.setUserId(userId);
        preference.setActive(true);
        preference.setEmailAddress("user@email.com");

        when(emailService.getPreferenceByUserId(any())).thenReturn(preference);

        mockMvc.perform(get("/api/v1/emails/preferences")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.emailAddress").value("user@email.com"));
    }

    @Test
    void upsertPreference_shouldReturnCreatedPreference() throws Exception {
        UUID userId = UUID.randomUUID();

        PreferenceRequest requestDto = new PreferenceRequest();
        requestDto.setUserId(userId);
        requestDto.setPreferenceActive(true);
        requestDto.setEmailAddress("user@email.com");

        EmailPreference createdPreference = new EmailPreference();
        createdPreference.setId(UUID.randomUUID());
        createdPreference.setUserId(userId);
        createdPreference.setActive(true);
        createdPreference.setEmailAddress("user@email.com");

        when(emailService.upsertPreference(any())).thenReturn(createdPreference);

        ObjectMapper objectMapper = new ObjectMapper();

        MockHttpServletRequestBuilder request = post("/api/v1/emails/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(requestDto));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.emailAddress").value("user@email.com"));
    }

    @Test
    void sendEmail_shouldReturnCreatedEmail() throws Exception {
        UUID userId = UUID.randomUUID();

        SendEmailRequest requestDto = new SendEmailRequest();
        requestDto.setUserId(userId);
        requestDto.setSubject("Test subject");
        requestDto.setBody("Test body");

        Email sentEmail = new Email();
        sentEmail.setStatus(EmailStatus.SUCCEEDED);
        when(emailService.sendEmail(any())).thenReturn(sentEmail);

        ObjectMapper objectMapper = new ObjectMapper();

        MockHttpServletRequestBuilder request = post("/api/v1/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(requestDto));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));
    }
}
