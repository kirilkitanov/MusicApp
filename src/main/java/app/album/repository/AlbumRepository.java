package app.album.repository;

import app.album.model.Album;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlbumRepository extends JpaRepository<Album, UUID> {
    List<Album> findByUserOrderByCreatedOnDesc(User user);


    Optional<Album> findByIdAndUserId(UUID albumId, UUID userId);

    List<Album> findAllByOrderByCreatedOnDesc();

}
