package app.album.service;

import app.album.model.Album;
import app.album.model.AlbumStatus;
import app.album.repository.AlbumRepository;
import app.notification.service.EmailService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.EditAlbumRequest;
import app.web.dto.NewAlbumRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final FavouriteAlbumService favouriteAlbumService;

    @Autowired
    public AlbumService(AlbumRepository albumRepository, UserService userService, EmailService emailService, FavouriteAlbumService favouriteAlbumService) {
        this.albumRepository = albumRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.favouriteAlbumService = favouriteAlbumService;
    }

    public void addNewAlbum(NewAlbumRequest newAlbumRequest, User user) {

        Album album = Album.builder()
                .albumName(newAlbumRequest.getAlbumName())
                .artistName(newAlbumRequest.getArtistName())
                .description(newAlbumRequest.getDescription())
                .genre(newAlbumRequest.getGenre())
                .albumCover(newAlbumRequest.getAlbumCover())
                .releaseDate(newAlbumRequest.getReleaseDate())
                .youtubeVideoId(newAlbumRequest.getYoutubeVideoId())
                .price(newAlbumRequest.getPrice())
                .albumStatus(AlbumStatus.VISIBLE)
                .createdOn(LocalDateTime.now())
                .user(user)
                .build();

        albumRepository.save(album);

        List<User> subscribers = favouriteAlbumService.getUsersWhoFavoritedArtist(album.getArtistName());

        String subject = "We have a new album from " + album.getArtistName() + " for you";
        String body = album.getArtistName() + " released a new album: " + album.getAlbumName() +
                "\n Follow the link for more information: http://localhost:8080/albums/" + album.getId() + "/view";

        try {
            for (User subscriber : subscribers) {
                emailService.sendEmail(subscriber.getId(), subject, body);
            }
        } catch (Exception ex) {
            log.error("Failed to send notification for album with ID: {} Error: {}", album.getId(), ex.getMessage());
        }
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
        album.setYoutubeVideoId(editAlbumRequest.getYoutubeVideoId());
        album.setPrice(editAlbumRequest.getPrice());

        albumRepository.save(album);
    }

    public List<Album> getAllAlbums() {
        return albumRepository.findAllByOrderByCreatedOnDesc();

    }

    public Album getById(UUID albumId) {
        return albumRepository.findById(albumId).orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Album not found with id %s".formatted(albumId)));
    }
}
