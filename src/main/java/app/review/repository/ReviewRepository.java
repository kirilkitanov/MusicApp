package app.review.repository;

import app.album.model.Album;
import app.review.model.Review;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository  extends JpaRepository<Review, UUID> {
    List<Review> findByAlbumOrderByCreatedOnDesc(Album album);

    List<Review> findAllByUserOrderByCreatedOnDesc(User user);

    List<Review> findAllByReportedTrueOrderByCreatedOnDesc();
}
