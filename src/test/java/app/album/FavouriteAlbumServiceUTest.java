package app.album;

import app.album.model.Album;
import app.album.model.FavouriteAlbum;
import app.album.repository.FavouriteAlbumRepository;
import app.album.service.FavouriteAlbumService;
import app.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavouriteAlbumServiceUTest {

    @Mock
    private FavouriteAlbumRepository favouriteAlbumRepository;

    @InjectMocks
    private FavouriteAlbumService favouriteAlbumService;

    @Test
    void givenUserWithFavorites_whenGetFavoritesByUser_thenReturnList() {

        User user = new User();
        FavouriteAlbum fav1 = FavouriteAlbum.builder().id(UUID.randomUUID()).build();
        FavouriteAlbum fav2 = FavouriteAlbum.builder().id(UUID.randomUUID()).build();
        when(favouriteAlbumRepository.findByUserOrderByAddedOnDesc(user)).thenReturn(List.of(fav1, fav2));

        List<FavouriteAlbum> result = favouriteAlbumService.getFavoritesByUser(user);

        assertEquals(2, result.size());
    }

    @Test
    void givenAlbumNotAlreadyFavorited_whenAddToFavourites_thenSaveFavourite() {

        User user = new User();
        Album album = Album.builder().id(UUID.randomUUID()).build();
        when(favouriteAlbumRepository.existsByUserAndAlbum(user, album)).thenReturn(false);

        favouriteAlbumService.addToFavourites(user, album);

        verify(favouriteAlbumRepository, times(1)).save(any(FavouriteAlbum.class));
    }

    @Test
    void givenAlbumAlreadyFavorited_whenAddToFavourites_thenDoNotSave() {

        User user = new User();
        Album album = Album.builder().id(UUID.randomUUID()).build();
        when(favouriteAlbumRepository.existsByUserAndAlbum(user, album)).thenReturn(true);

        favouriteAlbumService.addToFavourites(user, album);

        verify(favouriteAlbumRepository, never()).save(any(FavouriteAlbum.class));
    }

    @Test
    void givenExistingFavourite_whenDeleteFavourites_thenDeleteFavourite() {

        User user = new User();
        Album album = Album.builder().id(UUID.randomUUID()).build();
        FavouriteAlbum favouriteAlbum = FavouriteAlbum.builder().id(UUID.randomUUID()).user(user).album(album).build();
        when(favouriteAlbumRepository.findByUserAndAlbum(user, album)).thenReturn(Optional.of(favouriteAlbum));

        favouriteAlbumService.deleteFavourites(user, album);

        verify(favouriteAlbumRepository, times(1)).delete(favouriteAlbum);
    }

    @Test
    void givenMissingFavourite_whenDeleteFavourites_thenThrowException() {

        User user = new User();
        Album album = Album.builder().id(UUID.randomUUID()).build();
        when(favouriteAlbumRepository.findByUserAndAlbum(user, album)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> favouriteAlbumService.deleteFavourites(user, album));
    }

    @Test
    void givenUserWithFavourites_whenGetFavouriteAlbumByUser_thenReturnAlbumIds() {

        User user = new User();
        Album album1 = Album.builder().id(UUID.randomUUID()).build();
        Album album2 = Album.builder().id(UUID.randomUUID()).build();
        FavouriteAlbum fav1 = FavouriteAlbum.builder().album(album1).build();
        FavouriteAlbum fav2 = FavouriteAlbum.builder().album(album2).build();
        when(favouriteAlbumRepository.findByUserOrderByAddedOnDesc(user)).thenReturn(List.of(fav1, fav2));

        List<UUID> result = favouriteAlbumService.getFavouriteAlbumByUser(user);

        assertEquals(2, result.size());
        assertTrue(result.contains(album1.getId()));
        assertTrue(result.contains(album2.getId()));
    }

    @Test
    void givenArtistWithFavorites_whenGetUsersWhoFavoritedArtist_thenReturnUsers() {

        User user1 = new User();
        User user2 = new User();
        when(favouriteAlbumRepository.findDistinctUsersByAlbumArtistName("Artist")).thenReturn(List.of(user1, user2));

        List<User> result = favouriteAlbumService.getUsersWhoFavoritedArtist("Artist");

        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
    }
}
