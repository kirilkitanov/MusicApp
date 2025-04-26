package app.review.service;

import app.album.model.Album;
import app.album.service.AlbumService;
import app.review.model.Review;
import app.review.repository.ReviewRepository;
import app.user.model.User;
import app.web.dto.NewReviewRequest;
import app.web.dto.ReportRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AlbumService albumService;


    @Autowired
    public ReviewService(ReviewRepository reviewRepository, AlbumService albumService) {
        this.reviewRepository = reviewRepository;
        this.albumService = albumService;
    }


    public void addNewReview(NewReviewRequest newReviewRequest, User user) {

        Album album = albumService.getById(newReviewRequest.getAlbumId());

        Review review = Review.builder()
                .comment(newReviewRequest.getComment())
                .createdOn(LocalDateTime.now())
                .reported(false)
                .user(user)
                .album(album)
                .build();

        reviewRepository.save(review);
    }

    public List<Review> getReviewsByAlbum(Album album) {
        return reviewRepository.findByAlbumOrderByCreatedOnDesc(album);
    }

    public Review getById(UUID id) {
        return reviewRepository.findById(id).orElseThrow();
    }

    public void reportReview(UUID reviewId, ReportRequest reportRequest) {

        Review review = getById(reviewId);
        review.setReported(true);
        review.setReportReason(reportRequest.getReportReason());
        reviewRepository.save(review);
    }

    public List<Review> getReviewsByUser(User user) {
        return reviewRepository.findAllByUserOrderByCreatedOnDesc(user);
    }

    public void deleteReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with id [%s]".formatted(reviewId)));

        reviewRepository.delete(review);
    }

    public List<Review> getAllReportedReviews() {
        return reviewRepository.findAllByReportedTrueOrderByCreatedOnDesc();
    }

    public void restoreReportedReview(UUID reviewId) {

        Optional<Review> optionalReview = reviewRepository.findById(reviewId);

        if (optionalReview.isEmpty()) {
            throw new RuntimeException("Review not found with id [%s]".formatted(reviewId));
        }

        Review review = optionalReview.get();

        if (review.isReported()) {
            review.setReported(false);
            review.setReportReason(null);

            reviewRepository.save(review);
        }
    }

    public void deleteAllReportedReviews() {
        List<Review> reportedReviews = getAllReportedReviews();
        reviewRepository.deleteAll(reportedReviews);
    }

}
