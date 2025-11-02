package app;

import app.album.model.Album;
import app.album.model.AlbumStatus;
import app.album.model.Genre;
import app.album.repository.AlbumRepository;
import app.album.service.AlbumService;
import app.notification.service.EmailService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.NewAlbumRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class AlbumITest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private AlbumRepository albumRepository;

    @MockitoBean
    private EmailService emailService;

    @Test
    void addNewAlbum_happyPath() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("TestUser")
                .password("123123")
                .email("test@email.com")
                .role(UserRole.ARTIST)
                .build();

        userService.register(registerRequest);

        User registeredUser = userRepository.findByUsername(registerRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        NewAlbumRequest newAlbumRequest = NewAlbumRequest.builder()
                .albumName("Test Album")
                .artistName("Test Artist")
                .description("Test Description")
                .genre(Genre.POP)
                .albumCover("https://website.com/cover.jpg")
                .releaseDate("2025")
                .youtubeVideoId("abcd1234")
                .build();

        albumService.addNewAlbum(newAlbumRequest, registeredUser);

        List<Album> albums = albumRepository.findAll();

        assertEquals(1, albums.size());
        assertEquals("Test Album", albums.get(0).getAlbumName());
        assertEquals("Test Artist", albums.get(0).getArtistName());
        assertEquals(AlbumStatus.VISIBLE, albums.get(0).getAlbumStatus());
        assertEquals(registeredUser.getId(), albums.get(0).getUser().getId());
        assertEquals(1, albumRepository.count());
    }
}
