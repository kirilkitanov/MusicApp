package app.web;

import app.notification.client.dto.EmailPreference;
import app.notification.service.EmailService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static app.web.TestBuilder.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void getEditProfile_shouldReturnEditProfileView() throws Exception {
        User user = aRandomUser();
        AuthenticationDetails principal = userDetails(user);
        EmailPreference preference = defaultEmailPreference();

        when(userService.getById(user.getId())).thenReturn(user);
        when(emailService.getEmailPreference(user.getId())).thenReturn(preference);

        mockMvc.perform(get("/users/{id}/profile", user.getId()).with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-profile"))
                .andExpect(model().attributeExists("user", "editProfileRequest", "emailPreference"));

        verify(userService).getById(user.getId());
        verify(emailService).getEmailPreference(user.getId());
    }

    @Test
    void getAllUsers_withAdminUser_shouldReturnUsersView() throws Exception {
        User admin = aRandomUser();
        admin.setRole(UserRole.ADMIN);
        AuthenticationDetails principal = userDetails(admin);

        when(userService.getById(admin.getId())).thenReturn(admin);
        when(userService.getAllUsers()).thenReturn(List.of(aRandomUser()));

        mockMvc.perform(get("/users").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users", "user"));

        verify(userService).getById(admin.getId());
        verify(userService).getAllUsers();
    }

    @Test
    void putChangeUserRole_shouldRedirectToUsersAndCallService() throws Exception {
        UUID userId = UUID.randomUUID();
        User admin = aRandomUser();
        admin.setRole(UserRole.ADMIN);
        AuthenticationDetails principal = userDetails(admin);

        mockMvc.perform(put("/users/{id}/role", userId)
                        .param("role", "ADMIN")
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService).changeRole(eq(userId), eq(UserRole.ADMIN));
    }

    @Test
    void putChangeUserStatus_shouldRedirectToUsersAndCallService() throws Exception {
        UUID userId = UUID.randomUUID();
        User admin = aRandomUser();
        admin.setRole(UserRole.ADMIN);
        AuthenticationDetails principal = userDetails(admin);

        mockMvc.perform(put("/users/{id}/status", userId)
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService).changeStatus(eq(userId));
    }

    @Test
    void putToggleNotifications_shouldRedirectToProfileAndCallEmailService() throws Exception {
        UUID id = UUID.randomUUID();
        User user = aRandomUser();
        AuthenticationDetails principal = userDetails(user);

        mockMvc.perform(put("/users/{id}/notifications", id)
                        .param("isPreferenceActive", "true")
                        .param("emailAddress", "user@email.com")
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/" + id + "/profile"));

        verify(emailService).savePreference(eq(id), eq(true), eq("user@email.com"));
    }

}
