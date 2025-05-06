package app.album.service;

import app.album.model.Album;
import app.album.model.AlbumStatus;
import app.album.repository.AlbumRepository;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.EditAlbumRequest;
import app.web.dto.NewAlbumRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.nio.file.AccessDeniedException;
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
                .description(newAlbumRequest.getDescription())
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

        if (user.getRole().name().equals("ADMIN")) {
            return albumRepository.findAllByOrderByCreatedOnDesc();
        } else {
            return albumRepository.findByUserOrderByCreatedOnDesc(user);
        }
    }

    public void changeAlbumStatus(UUID albumId, UUID userId) throws AccessDeniedException {

            Album album = findAndCheckAlbumOwnership(albumId, userId);

            if (album.getAlbumStatus() == AlbumStatus.VISIBLE) {
                album.setAlbumStatus(AlbumStatus.INVISIBLE);
            } else {
                album.setAlbumStatus(AlbumStatus.VISIBLE);
            }

            albumRepository.save(album);
        }


    public Album findAndCheckAlbumOwnership(UUID albumId, UUID userId) throws AccessDeniedException {

        Optional<Album> optionalAlbum = albumRepository.findById(albumId);

        if (optionalAlbum.isEmpty()) {
            throw new AccessDeniedException("Album not found.");
        }

        Album album = optionalAlbum.get();

        User user = userService.getById(userId);

        if (!user.getRole().name().equals("ADMIN") && !album.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to edit this album.");
        }

        return album;
    }

    public void updateAlbum(UUID id, EditAlbumRequest editAlbumRequest, User user) throws AccessDeniedException{

        Album album = findAndCheckAlbumOwnership(id, user.getId());

        album.setAlbumName(editAlbumRequest.getAlbumName());
        album.setArtistName(editAlbumRequest.getArtistName());
        album.setDescription(editAlbumRequest.getDescription());
        album.setGenre(editAlbumRequest.getGenre());
        album.setAlbumCover(editAlbumRequest.getAlbumCover());
        album.setReleaseDate(editAlbumRequest.getReleaseDate());
        album.setDescription(editAlbumRequest.getDescription());

        albumRepository.save(album);
    }

    public List<Album> getAllAlbums() {
        return albumRepository.findAllByOrderByCreatedOnDesc();

    }

    public Album getById(UUID albumId) {
        return albumRepository.findById(albumId).orElseThrow(() -> new RuntimeException("Album not found with id [%s]".formatted(albumId)));
    }
}
