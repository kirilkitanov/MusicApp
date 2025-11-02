package app.web;

import app.TestBuilder;
import app.album.model.Album;
import app.album.service.AlbumService;
import app.album.service.FavouriteAlbumService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AlbumService albumService;

    @MockitoBean
    private FavouriteAlbumService favouriteAlbumService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexView() throws Exception {

        MockHttpServletRequestBuilder request = get("/");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRequestToRegisterEndpoint_shouldReturnRegisterView() throws Exception {

        MockHttpServletRequestBuilder request = get("/register");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void postRequestToRegisterEndpoint_withValidData_shouldCallServiceAndSetFlashMessage() throws Exception {

        MockHttpServletRequestBuilder request = post("/register")
                .param("username", "testUser")
                .param("password", "123123")
                .param("email", "user@email.com")
                .param("role", "FAN")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("successMessage", "Your account has been successfully created."));

        verify(userService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void postRequestToRegisterEndpoint_withInvalidData_shouldReturnRegisterViewAndNotCallService() throws Exception {

        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "")
                .formField("password", "")
                .formField("email", "")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));

        verify(userService, never()).register(any());
    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginView() throws Exception {

        MockHttpServletRequestBuilder request = get("/login");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getRequestToLoginEndpoint_withErrorParam_shouldReturnLoginViewWithErrorMessage() throws Exception {

        MockHttpServletRequestBuilder request = get("/login").param("error", "");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest", "errorMessage"));
    }

    @Test
    void getAuthenticatedRequestToHomeEndpoint_shouldReturnHomeViewWithUserAndAlbums() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = TestBuilder.aRandomUser();

        when(userService.getById(userId)).thenReturn(user);
        when(albumService.getAllAlbums()).thenReturn(List.of(new Album()));
        when(favouriteAlbumService.getFavouriteAlbumByUser(user)).thenReturn(List.of(UUID.randomUUID()));

        AuthenticationDetails principal = new AuthenticationDetails(userId, "testUser", "123123", UserRole.FAN, true);

        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user", "albums", "favouriteAlbums"));

        verify(userService, times(1)).getById(userId);
        verify(albumService, times(1)).getAllAlbums();
        verify(favouriteAlbumService, times(1)).getFavouriteAlbumByUser(user);
    }

    @Test
    void getUnauthenticatedRequestToHomeEndpoint_shouldRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/home");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        verify(userService, never()).getById(any());
        verify(albumService, never()).getAllAlbums();
        verify(favouriteAlbumService, never()).getFavouriteAlbumByUser(any());
    }
}
