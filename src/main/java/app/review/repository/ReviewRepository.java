package app.review.repository;

import app.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository  extends JpaRepository<Review, UUID> {
}
