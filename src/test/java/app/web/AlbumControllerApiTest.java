package app.web;

import app.TestBuilder;
import app.album.model.Album;
import app.album.service.AlbumService;
import app.review.model.Review;
import app.review.service.ReviewService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.NewAlbumRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlbumController.class)
public class AlbumControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AlbumService albumService;

    @MockitoBean
    private ReviewService reviewService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getNewAlbumPage_withArtistUser_shouldReturnNewAlbumView() throws Exception {

        User user = TestBuilder.aRandomUser();
        user.setRole(UserRole.ARTIST);
        AuthenticationDetails principal = new AuthenticationDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), true);

        when(userService.getById(user.getId())).thenReturn(user);

         MockHttpServletRequestBuilder request = get("/albums/new").with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("new-album"))
                .andExpect(model().attributeExists("user", "newAlbumRequest"));

        verify(userService, times(1)).getById(user.getId());
    }


    @Test
    void postAddNewAlbum_withInvalidData_shouldReturnNewAlbumView() throws Exception {
        User user = TestBuilder.aRandomUser();
        user.setRole(UserRole.ARTIST);
        AuthenticationDetails principal = new AuthenticationDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), true);

        when(userService.getById(user.getId())).thenReturn(user);

        MockHttpServletRequestBuilder request = post("/albums")
                .with(user(principal))
                .with(csrf())
                .param("albumName", "")
                .param("artistName", "")
                .param("description", "")
                .param("genre", "");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("new-album"))
                .andExpect(model().attributeExists("user", "newAlbumRequest"));

        verify(albumService, never()).addNewAlbum(any(), any());
    }

    @Test
    void getMyUploadedAlbums_withArtist_shouldReturnUploadedAlbumsView() throws Exception {
        User user = TestBuilder.aRandomUser();
        user.setRole(UserRole.ARTIST);
        AuthenticationDetails principal = new AuthenticationDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), true);

        when(userService.getById(user.getId())).thenReturn(user);
        when(albumService.findAlbumsByUser(user)).thenReturn(List.of(TestBuilder.aRandomAlbum(user)));

        MockHttpServletRequestBuilder request = get("/albums/personal").with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("uploaded-albums"))
                .andExpect(model().attributeExists("user", "uploadedAlbums"));

        verify(albumService, times(1)).findAlbumsByUser(user);
    }


    @Test
    void putChangeAlbumStatus_shouldRedirectToPersonalAndCallService() throws Exception {
        User user = TestBuilder.aRandomUser();
        user.setRole(UserRole.ARTIST);
        AuthenticationDetails principal = new AuthenticationDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), true);

        UUID albumId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = put("/albums/{id}/status", albumId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/albums/personal"));

        verify(albumService, times(1)).changeAlbumStatus(albumId, user.getId());
    }

    @Test
    void getEditAlbumPage_shouldReturnEditAlbumView() throws Exception {
        User user = TestBuilder.aRandomUser();
        user.setRole(UserRole.ARTIST);
        AuthenticationDetails principal = new AuthenticationDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), true);

        UUID albumId = UUID.randomUUID();
        Album album = TestBuilder.aRandomAlbum(user);
        when(userService.getById(user.getId())).thenReturn(user);
        when(albumService.findAndCheckAlbumOwnership(albumId, user.getId())).thenReturn(album);

        MockHttpServletRequestBuilder request = get("/albums/{id}/form", albumId).with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-album"))
                .andExpect(model().attributeExists("user", "editAlbumRequest"));

        verify(albumService, times(1)).findAndCheckAlbumOwnership(albumId, user.getId());
    }

    @Test
    void getViewAlbumDetails_shouldReturnViewAlbumView() throws Exception {
        User user = TestBuilder.aRandomUser();
        Album album = TestBuilder.aRandomAlbum(user);
        Review review = TestBuilder.aRandomReview(user, album);
        AuthenticationDetails principal = new AuthenticationDetails(
                user.getId(), user.getUsername(), user.getPassword(), user.getRole(), true
        );

        when(userService.getById(user.getId())).thenReturn(user);
        when(albumService.getById(album.getId())).thenReturn(album);
        when(reviewService.getReviewsByAlbum(album)).thenReturn(List.of(review));

        mockMvc.perform(get("/albums/{id}/view", album.getId()).with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("view-album"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("album", album))
                .andExpect(model().attributeExists("newReviewRequest"))
                .andExpect(model().attribute("reviews", List.of(review)));

        verify(userService).getById(user.getId());
        verify(albumService).getById(album.getId());
        verify(reviewService).getReviewsByAlbum(album);
    }

    @Test
    void postAddNewAlbum_withValidData_shouldRedirectToHomeAndCallService() throws Exception {

        User user = TestBuilder.aRandomUser();
        user.setRole(UserRole.ARTIST);
        AuthenticationDetails principal = new AuthenticationDetails(
                user.getId(), user.getUsername(), user.getPassword(), user.getRole(), true
        );

        when(userService.getById(user.getId())).thenReturn(user);

        MockHttpServletRequestBuilder request = post("/albums")
                .with(user(principal))
                .with(csrf())
                .param("albumName", "Test Album")
                .param("artistName", "Test Artist")
                .param("description", "Test Description")
                .param("genre", "POP")
                .param("albumCover", "http://cover.jpg")
                .param("releaseDate", "2015")
                .param("youtubeVideoId", "abcd1234");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(albumService, times(1)).addNewAlbum(any(NewAlbumRequest.class), eq(user));
    }

}
