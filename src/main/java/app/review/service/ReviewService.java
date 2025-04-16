package app.review.service;

import app.album.model.Album;
import app.album.service.AlbumService;
import app.review.model.Review;
import app.review.repository.ReviewRepository;
import app.user.model.User;
import app.web.dto.NewReviewRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


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
        return reviewRepository.findByAlbum(album);
    }
}
