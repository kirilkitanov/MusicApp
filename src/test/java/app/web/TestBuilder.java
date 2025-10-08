package app.web;
import app.album.model.Album;
import app.album.model.AlbumStatus;
import app.album.model.Genre;
import app.notification.client.dto.EmailPreference;
import app.review.model.Review;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .password("123123")
                .email("user@email.com")
                .role(UserRole.FAN)
                .isActive(true)
                .build();

        return user;

    }

    public static Album aRandomAlbum(User user) {
        return Album.builder()
                .id(UUID.randomUUID())
                .albumName("Test Album")
                .artistName("Test Artist")
                .description("Test Description")
                .genre(Genre.POP)
                .albumCover("http://cover.jpg")
                .releaseDate("2025")
                .albumStatus(AlbumStatus.VISIBLE)
                .youtubeVideoId("abcd1234")
                .user(user)
                .build();
    }

    public static Review aRandomReview(User user, Album album) {
        return Review.builder()
                .id(UUID.randomUUID())
                .comment("Test Review.")
                .reported(false)
                .user(user)
                .album(album)
                .build();
    }

    public static AuthenticationDetails userDetails(User user) {
        return new AuthenticationDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                true
        );
    }

    public static EmailPreference defaultEmailPreference() {
        return EmailPreference.builder()
                .active(true)
                .build();
    }

}
