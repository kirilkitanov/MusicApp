package app.favorite.repository;

import app.album.model.Album;
import app.favorite.model.FavouriteAlbum;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavouriteAlbumRepository extends JpaRepository<FavouriteAlbum, UUID> {


    List<FavouriteAlbum> findByUserOrderByAddedOnDesc(User user);


  boolean existsByUserAndAlbum(User user, Album album);

    Optional<FavouriteAlbum> findByUserAndAlbum(User user, Album album);
}
