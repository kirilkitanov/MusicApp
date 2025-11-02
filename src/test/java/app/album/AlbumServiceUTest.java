package app.album;

import app.album.model.Album;
import app.album.model.AlbumStatus;
import app.album.model.Genre;
import app.album.repository.AlbumRepository;
import app.album.service.AlbumService;
import app.album.service.FavouriteAlbumService;
import app.notification.service.EmailService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.EditAlbumRequest;
import app.web.dto.NewAlbumRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
public class AlbumServiceUTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private FavouriteAlbumService favouriteAlbumService;

    @InjectMocks
    private AlbumService albumService;

    @Test
    void givenNewAlbumRequest_whenAddNewAlbum_thenSaveAlbumAndSendEmails() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .role(UserRole.ARTIST)
                .build();

        NewAlbumRequest request = NewAlbumRequest.builder()
                .albumName("Test Album")
                .artistName("Test Artist")
                .genre(Genre.ROCK)
                .albumCover("cover.png")
                .releaseDate("2023-01-01")
                .description("Description")
                .youtubeVideoId("yt123")
                .build();

        Album savedAlbum = Album.builder()
                .id(UUID.randomUUID())
                .albumName("Test Album")
                .artistName("Test Artist")
                .createdOn(LocalDateTime.now())
                .user(user)
                .build();

        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);
        when(favouriteAlbumService.getUsersWhoFavoritedArtist("Test Artist"))
                .thenReturn(List.of(user));


        albumService.addNewAlbum(request, user);


        verify(albumRepository, times(1)).save(any(Album.class));
        verify(emailService, times(1))
                .sendEmail(eq(user.getId()), contains("We have a new album"), contains("Test Album"));
    }

    @Test
    void givenUserIsAdmin_whenFindAlbumsByUser_thenReturnAllAlbums() {

        User admin = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.ADMIN)
                .build();

        when(albumRepository.findAllByOrderByCreatedOnDesc()).thenReturn(List.of(new Album()));

        List<Album> result = albumService.findAlbumsByUser(admin);

        assertEquals(1, result.size());
        verify(albumRepository, times(1)).findAllByOrderByCreatedOnDesc();
    }

    @Test
    void givenUserIsOwner_whenChangeAlbumStatus_thenStatusIsUpdated() throws Exception {

        UUID userId = UUID.randomUUID();
        Album album = Album.builder()
                .id(UUID.randomUUID())
                .albumStatus(AlbumStatus.VISIBLE)
                .user(User.builder().id(userId).role(UserRole.FAN).build())
                .build();

        when(albumRepository.findById(album.getId())).thenReturn(Optional.of(album));
        when(userService.getById(userId)).thenReturn(album.getUser());

        albumService.changeAlbumStatus(album.getId(), userId);

        assertEquals(AlbumStatus.INVISIBLE, album.getAlbumStatus());
        verify(albumRepository, times(1)).save(album);
    }

    @Test
    void givenAlbumNotFound_whenFindAndCheckOwnership_thenThrowAccessDeniedException() {

        UUID albumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> albumService.findAndCheckAlbumOwnership(albumId, userId));
    }

    @Test
    void givenInvalidUser_whenFindAndCheckOwnership_thenThrowAccessDeniedException() {

        UUID albumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User owner = User.builder().id(UUID.randomUUID()).role(UserRole.FAN).build();
        Album album = Album.builder().id(albumId).user(owner).build();

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(userService.getById(userId)).thenReturn(User.builder().id(userId).role(UserRole.FAN).build());

        assertThrows(AccessDeniedException.class, () -> albumService.findAndCheckAlbumOwnership(albumId, userId));
    }

    @Test
    void givenRegularUser_whenFindAlbumsByUser_thenReturnOnlyTheirAlbums() {

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(UserRole.FAN)
                .build();

        Album album1 = Album.builder().id(UUID.randomUUID()).user(user).albumName("Album1").build();
        Album album2 = Album.builder().id(UUID.randomUUID()).user(user).albumName("Album2").build();
        List<Album> userAlbums = List.of(album1, album2);

        when(albumRepository.findByUserOrderByCreatedOnDesc(user)).thenReturn(userAlbums);

        List<Album> result = albumService.findAlbumsByUser(user);

        assertEquals(2, result.size());
        assertTrue(result.contains(album1));
        assertTrue(result.contains(album2));
    }

    @Test
    void givenInvisibleAlbum_whenChangeAlbumStatus_thenBecomeVisible() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID albumId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(UserRole.FAN)
                .build();

        Album album = Album.builder()
                .id(albumId)
                .albumStatus(AlbumStatus.INVISIBLE)
                .user(user)
                .build();

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(userService.getById(userId)).thenReturn(user);

        albumService.changeAlbumStatus(albumId, userId);

        assertEquals(AlbumStatus.VISIBLE, album.getAlbumStatus());
    }

    @Test
    void givenAlbumsInDatabase_whenGetAllAlbums_thenReturnAllAlbums() {

        Album album1 = Album.builder().id(UUID.randomUUID()).albumName("Album1").build();
        Album album2 = Album.builder().id(UUID.randomUUID()).albumName("Album2").build();
        List<Album> albums = List.of(album1, album2);

        when(albumRepository.findAllByOrderByCreatedOnDesc()).thenReturn(albums);

        List<Album> result = albumService.getAllAlbums();

        assertEquals(2, result.size());
        assertTrue(result.contains(album1));
        assertTrue(result.contains(album2));
    }

    @Test
    void givenExistingAlbum_whenGetById_thenReturnAlbum() {

        UUID albumId = UUID.randomUUID();
        Album album = Album.builder().id(albumId).albumName("Test Album").build();
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

        Album result = albumService.getById(albumId);

        assertEquals(albumId, result.getId());
        assertEquals("Test Album", result.getAlbumName());
    }

    @Test
    void givenMissingAlbum_whenGetById_thenThrowAccessDeniedException() {

        UUID albumId = UUID.randomUUID();
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> albumService.getById(albumId));
    }

    @Test
    void givenValidEditAlbumRequest_whenUpdateAlbum_thenAlbumIsUpdated() throws Exception {

        UUID albumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(UserRole.FAN)
                .build();

        Album album = Album.builder()
                .id(albumId)
                .albumName("Old Album")
                .artistName("Old Artist")
                .description("Old Description")
                .genre(Genre.ROCK)
                .albumCover("old_cover.png")
                .releaseDate("2022-01-01")
                .youtubeVideoId("oldYT")
                .user(user)
                .build();

        EditAlbumRequest editRequest = EditAlbumRequest.builder()
                .albumName("New Album")
                .artistName("New Artist")
                .description("New Description")
                .genre(Genre.POP)
                .albumCover("new_cover.png")
                .releaseDate("2023-01-01")
                .youtubeVideoId("newYT")
                .build();

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(userService.getById(userId)).thenReturn(user);

        albumService.updateAlbum(albumId, editRequest, user);

        assertEquals("New Album", album.getAlbumName());
        assertEquals("New Artist", album.getArtistName());
        assertEquals("New Description", album.getDescription());
        assertEquals(Genre.POP, album.getGenre());
        assertEquals("new_cover.png", album.getAlbumCover());
        assertEquals("2023-01-01", album.getReleaseDate());
        assertEquals("newYT", album.getYoutubeVideoId());
    }
}
