package app.album.repository;

import app.album.model.Album;
import app.album.model.FavouriteAlbum;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavouriteAlbumRepository extends JpaRepository<FavouriteAlbum, UUID> {


    List<FavouriteAlbum> findByUserOrderByAddedOnDesc(User user);

    boolean existsByUserAndAlbum(User user, Album album);

    Optional<FavouriteAlbum> findByUserAndAlbum(User user, Album album);

    @Query("SELECT DISTINCT f.user FROM FavouriteAlbum f WHERE f.album.artistName = :artistName")
    List<User> findDistinctUsersByAlbumArtistName(@Param("artistName") String artistName);

}
