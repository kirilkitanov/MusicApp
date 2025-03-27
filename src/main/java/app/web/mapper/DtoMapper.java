package app.web.mapper;

import app.album.model.Album;
import app.user.model.User;
import app.web.dto.EditAlbumRequest;
import app.web.dto.EditProfileRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static EditProfileRequest mapUserToEditProfileRequest(User user) {

        return EditProfileRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    public static EditAlbumRequest mapAlbumToEditAlbumRequest (Album album){

        return EditAlbumRequest.builder()
                .id(album.getId())
                .albumName(album.getAlbumName())
                .artistName(album.getArtistName())
                .genre(album.getGenre())
                .albumCover(album.getAlbumCover())
                .releaseDate(album.getReleaseDate())
                .build();
    }

}
