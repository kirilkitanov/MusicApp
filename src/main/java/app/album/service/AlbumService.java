package app.album.service;

import app.album.model.Album;
import app.album.model.AlbumStatus;
import app.album.repository.AlbumRepository;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.NewAlbumRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final UserService userService;

    @Autowired
    public AlbumService(AlbumRepository albumRepository, UserService userService) {
        this.albumRepository = albumRepository;
        this.userService = userService;
    }


    public void addNewAlbum(NewAlbumRequest newAlbumRequest, User user) {

        Album album = Album.builder()
                .albumName(newAlbumRequest.getAlbumName())
                .artistName(newAlbumRequest.getArtistName())
                .genre(newAlbumRequest.getGenre())
                .albumCover(newAlbumRequest.getAlbumCover())
                .releaseDate(newAlbumRequest.getReleaseDate())
                .albumStatus(AlbumStatus.VISIBLE)
                .createdOn(LocalDateTime.now())
                .user(user)
                .build();

        albumRepository.save(album);

    }

    public List<Album> findAlbumsByUser(User user) {

        return albumRepository.findByUserOrderByCreatedOnDesc(user);

    }

    public void changeAlbumStatus(UUID albumId, UUID userId) {

        Optional<Album> optionalAlbum = albumRepository.findByIdAndUserId(albumId, userId);
        if (optionalAlbum.isEmpty()){
            throw new RuntimeException("Album with id [%s] does not belong to user with id [%s]".formatted(albumId, userId));
        }

        Album album = optionalAlbum.get();

        if (album.getAlbumStatus() == AlbumStatus.VISIBLE){
            album.setAlbumStatus(AlbumStatus.INVISIBLE);
        } else {
            album.setAlbumStatus(AlbumStatus.VISIBLE);
        }

        albumRepository.save(album);
    }
}
