package app.album.service;

import app.album.model.Album;
import app.album.model.FavouriteAlbum;
import app.album.repository.FavouriteAlbumRepository;
import app.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FavouriteAlbumService {

    private final FavouriteAlbumRepository favouriteAlbumRepository;

    @Autowired
    public FavouriteAlbumService(FavouriteAlbumRepository favouriteAlbumRepository) {
        this.favouriteAlbumRepository = favouriteAlbumRepository;
    }

    public List<FavouriteAlbum> getFavoritesByUser(User user) {

        return favouriteAlbumRepository.findByUserOrderByAddedOnDesc(user);
    }

    public void addToFavourites(User user, Album album) {
        if (!favouriteAlbumRepository.existsByUserAndAlbum(user, album)) {
            FavouriteAlbum favouriteAlbum = FavouriteAlbum.builder()
                    .user(user)
                    .album(album)
                    .addedOn(LocalDateTime.now())
                    .build();
            favouriteAlbumRepository.save(favouriteAlbum);
        }
    }

    public void deleteFavourites(User user, Album album) {
        FavouriteAlbum favouriteAlbum = favouriteAlbumRepository.findByUserAndAlbum(user, album)
                .orElseThrow(() -> new RuntimeException("Favourite album not found with id [%s]".formatted(album)));

        favouriteAlbumRepository.delete(favouriteAlbum);

    }

    public List<UUID> getFavouriteAlbumByUser(User user) {
        return favouriteAlbumRepository.findByUserOrderByAddedOnDesc(user)
                .stream()
                .map(fav -> fav.getAlbum().getId())
                .toList();
    }
}
