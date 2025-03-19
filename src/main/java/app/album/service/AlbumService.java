package app.album.service;

import app.album.model.Album;
import app.album.model.AlbumStatus;
import app.album.repository.AlbumRepository;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.NewAlbumRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.List;

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

        return albumRepository.findByUser(user);
    }
}
