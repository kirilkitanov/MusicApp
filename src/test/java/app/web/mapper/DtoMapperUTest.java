package app.web.mapper;

import app.album.model.Album;
import app.album.model.Genre;
import app.user.model.User;
import app.web.dto.EditAlbumRequest;
import app.web.dto.EditProfileRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DtoMapperUTest {

    @Test
    void givenHappyPath_whenMappingUserToEditProfileRequest() {

        User user = User.builder()
                .firstName("FirstName")
                .lastName("LastName")
                .email("user@mail.com")
                .build();

        EditProfileRequest resultDto = DtoMapper.mapUserToEditProfileRequest(user);

        assertEquals(user.getFirstName(), resultDto.getFirstName());
        assertEquals(user.getLastName(), resultDto.getLastName());
        assertEquals(user.getEmail(), resultDto.getEmail());
    }

    @Test
    void givenHappyPath_whenMappingAlbumToEditAlbumRequest() {

        UUID albumId = UUID.randomUUID();
        Album album = Album.builder()
                .id(albumId)
                .albumName("AlbumName")
                .artistName("ArtistName")
                .description("Description")
                .genre(Genre.ROCK)
                .albumCover("www.cover.com")
                .releaseDate("2025")
                .youtubeVideoId("abc123xyz")
                .build();

        EditAlbumRequest resultDto = DtoMapper.mapAlbumToEditAlbumRequest(album);

        assertEquals(album.getId(), resultDto.getId());
        assertEquals(album.getAlbumName(), resultDto.getAlbumName());
        assertEquals(album.getArtistName(), resultDto.getArtistName());
        assertEquals(album.getDescription(), resultDto.getDescription());
        assertEquals(album.getGenre(), resultDto.getGenre());
        assertEquals(album.getAlbumCover(), resultDto.getAlbumCover());
        assertEquals(album.getReleaseDate(), resultDto.getReleaseDate());
        assertEquals(album.getYoutubeVideoId(), resultDto.getYoutubeVideoId());
    }
}
