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
        // Given
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

        // When
        albumService.addNewAlbum(request, user);

        // Then
        verify(albumRepository, times(1)).save(any(Album.class));
        verify(emailService, times(1))
                .sendEmail(eq(user.getId()), contains("We have a new album"), contains("Test Album"));
    }

    @Test
    void givenUserIsAdmin_whenFindAlbumsByUser_thenReturnAllAlbums() {
        // Given
        User admin = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.ADMIN)
                .build();

        when(albumRepository.findAllByOrderByCreatedOnDesc()).thenReturn(List.of(new Album()));

        // When
        List<Album> result = albumService.findAlbumsByUser(admin);

        // Then
        assertEquals(1, result.size());
        verify(albumRepository, times(1)).findAllByOrderByCreatedOnDesc();
    }

    @Test
    void givenUserIsOwner_whenChangeAlbumStatus_thenStatusIsUpdated() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        Album album = Album.builder()
                .id(UUID.randomUUID())
                .albumStatus(AlbumStatus.VISIBLE)
                .user(User.builder().id(userId).role(UserRole.FAN).build())
                .build();

        when(albumRepository.findById(album.getId())).thenReturn(Optional.of(album));
        when(userService.getById(userId)).thenReturn(album.getUser());

        // When
        albumService.changeAlbumStatus(album.getId(), userId);

        // Then
        assertEquals(AlbumStatus.INVISIBLE, album.getAlbumStatus());
        verify(albumRepository, times(1)).save(album);
    }

    @Test
    void givenAlbumNotFound_whenFindAndCheckOwnership_thenThrowAccessDeniedException() {
        // Given
        UUID albumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(AccessDeniedException.class, () -> albumService.findAndCheckAlbumOwnership(albumId, userId));
    }

    @Test
    void givenInvalidUser_whenFindAndCheckOwnership_thenThrowAccessDeniedException() {
        // Given
        UUID albumId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User owner = User.builder().id(UUID.randomUUID()).role(UserRole.FAN).build();
        Album album = Album.builder().id(albumId).user(owner).build();

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(userService.getById(userId)).thenReturn(User.builder().id(userId).role(UserRole.FAN).build());

        // When / Then
        assertThrows(AccessDeniedException.class, () -> albumService.findAndCheckAlbumOwnership(albumId, userId));
    }

    @Test
    void givenRegularUser_whenFindAlbumsByUser_thenReturnOnlyTheirAlbums() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(UserRole.FAN)
                .build();

        Album album1 = Album.builder().id(UUID.randomUUID()).user(user).albumName("Album1").build();
        Album album2 = Album.builder().id(UUID.randomUUID()).user(user).albumName("Album2").build();
        List<Album> userAlbums = List.of(album1, album2);

        when(albumRepository.findByUserOrderByCreatedOnDesc(user)).thenReturn(userAlbums);

        // When
        List<Album> result = albumService.findAlbumsByUser(user);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(album1));
        assertTrue(result.contains(album2));
    }

    @Test
    void givenInvisibleAlbum_whenChangeAlbumStatus_thenBecomeVisible() throws Exception {
        // Given
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

        // When
        albumService.changeAlbumStatus(albumId, userId);

        // Then
        assertEquals(AlbumStatus.VISIBLE, album.getAlbumStatus());
    }

    @Test
    void givenAlbumsInDatabase_whenGetAllAlbums_thenReturnAllAlbums() {
        // Given
        Album album1 = Album.builder().id(UUID.randomUUID()).albumName("Album1").build();
        Album album2 = Album.builder().id(UUID.randomUUID()).albumName("Album2").build();
        List<Album> albums = List.of(album1, album2);

        when(albumRepository.findAllByOrderByCreatedOnDesc()).thenReturn(albums);

        // When
        List<Album> result = albumService.getAllAlbums();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(album1));
        assertTrue(result.contains(album2));
    }

    @Test
    void givenExistingAlbum_whenGetById_thenReturnAlbum() {
        // Given
        UUID albumId = UUID.randomUUID();
        Album album = Album.builder().id(albumId).albumName("Test Album").build();
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

        // When
        Album result = albumService.getById(albumId);

        // Then
        assertEquals(albumId, result.getId());
        assertEquals("Test Album", result.getAlbumName());
    }

    @Test
    void givenMissingAlbum_whenGetById_thenThrowAccessDeniedException() {
        // Given
        UUID albumId = UUID.randomUUID();
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> albumService.getById(albumId));
    }

    @Test
    void givenValidEditAlbumRequest_whenUpdateAlbum_thenAlbumIsUpdated() throws Exception {
        // Given
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

        // When
        albumService.updateAlbum(albumId, editRequest, user);

        // Then
        assertEquals("New Album", album.getAlbumName());
        assertEquals("New Artist", album.getArtistName());
        assertEquals("New Description", album.getDescription());
        assertEquals(Genre.POP, album.getGenre());
        assertEquals("new_cover.png", album.getAlbumCover());
        assertEquals("2023-01-01", album.getReleaseDate());
        assertEquals("newYT", album.getYoutubeVideoId());
    }

}
