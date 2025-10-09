package app.web;

import app.album.model.Album;
import app.album.model.FavouriteAlbum;
import app.album.service.AlbumService;
import app.album.service.FavouriteAlbumService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static app.web.TestBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(FavouriteAlbumController.class)
public class FavouriteAlbumControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavouriteAlbumService favouriteAlbumService;

    @MockitoBean
    private AlbumService albumService;

    @MockitoBean
    private UserService userService;

    @Test
    void showFavourites_shouldReturnFavouritesViewWithUserAndFavorites() throws Exception {

        User user = aRandomUser();
        AuthenticationDetails principal = userDetails(user);
        Album album = aRandomAlbum(user);
        FavouriteAlbum favouriteAlbum = new FavouriteAlbum();
        favouriteAlbum.setAlbum(album);
        favouriteAlbum.setUser(user);

        when(userService.getById(user.getId())).thenReturn(user);
        when(favouriteAlbumService.getFavoritesByUser(user)).thenReturn(List.of(favouriteAlbum));


        mockMvc.perform(get("/favourites").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("favourite-albums"))
                .andExpect(model().attributeExists("user", "favorites"));

        verify(userService).getById(user.getId());
        verify(favouriteAlbumService).getFavoritesByUser(user);
    }

    @Test
    void makeFavouriteAlbum_shouldCallServiceAndRedirectToHome() throws Exception {

        User user = aRandomUser();
        AuthenticationDetails principal = userDetails(user);
        Album album = aRandomAlbum(user);
        UUID albumId = album.getId();

        when(userService.getById(user.getId())).thenReturn(user);
        when(albumService.getById(albumId)).thenReturn(album);


        mockMvc.perform(post("/favourites/{albumId}", albumId)
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService).getById(user.getId());
        verify(albumService).getById(albumId);
        verify(favouriteAlbumService).addToFavourites(user, album);
    }

    @Test
    void deleteFavouriteAlbum_shouldCallServiceAndRedirectToGivenUrl() throws Exception {

        User user = aRandomUser();
        AuthenticationDetails principal = userDetails(user);
        Album album = aRandomAlbum(user);
        UUID albumId = album.getId();

        when(userService.getById(user.getId())).thenReturn(user);
        when(albumService.getById(albumId)).thenReturn(album);


        mockMvc.perform(delete("/favourites/{albumId}", albumId)
                        .param("redirect", "/custom-page")
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/custom-page"));

        verify(userService).getById(user.getId());
        verify(albumService).getById(albumId);
        verify(favouriteAlbumService).deleteFavourites(user, album);
    }

}
