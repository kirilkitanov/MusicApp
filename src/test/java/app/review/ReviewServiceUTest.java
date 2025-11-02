package app.review;

import app.album.model.Album;
import app.album.service.AlbumService;
import app.notification.service.EmailService;
import app.review.model.ReportReason;
import app.review.model.Review;
import app.review.repository.ReviewRepository;
import app.review.service.ReviewService;
import app.user.model.User;
import app.web.dto.NewReviewRequest;
import app.web.dto.ReportRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceUTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private AlbumService albumService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void givenValidRequest_whenAddNewReview_thenSaveAndSendEmail() {

        User user = new User();
        user.setUsername("Reviewer");

        User albumOwner = new User();
        albumOwner.setId(UUID.randomUUID());

        Album album = Album.builder().id(UUID.randomUUID()).albumName("Test Album").user(albumOwner).build();

        NewReviewRequest request = NewReviewRequest.builder()
                .albumId(album.getId())
                .comment("Nice album!")
                .build();

        when(albumService.getById(request.getAlbumId())).thenReturn(album);

        reviewService.addNewReview(request, user);

        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(emailService, times(1)).sendEmail(
                eq(albumOwner.getId()),
                contains("New review on your album"),
                contains("Nice album!")
        );
    }

    @Test
    void givenAlbum_whenGetReviewsByAlbum_thenReturnReviews() {

        Album album = new Album();
        Review review = new Review();
        when(reviewRepository.findByAlbumOrderByCreatedOnDesc(album)).thenReturn(List.of(review));

        List<Review> result = reviewService.getReviewsByAlbum(album);

        assertEquals(1, result.size());
    }

    @Test
    void givenReviewId_whenGetById_thenReturnReview() {

        UUID id = UUID.randomUUID();
        Review review = new Review();
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));

        Review result = reviewService.getById(id);

        assertNotNull(result);
    }

    @Test
    void givenReportRequest_whenReportReview_thenSetReportedAndSave() {

        UUID id = UUID.randomUUID();
        Review review = new Review();
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));
        ReportRequest request = new ReportRequest();
        request.setReportReason(ReportReason.SPAM);

        reviewService.reportReview(id, request);

        assertTrue(review.isReported());
        assertEquals(ReportReason.SPAM, review.getReportReason());
        verify(reviewRepository).save(review);
    }

    @Test
    void givenUser_whenGetReviewsByUser_thenReturnList() {

        User user = new User();
        Review review = new Review();
        when(reviewRepository.findAllByUserOrderByCreatedOnDesc(user)).thenReturn(List.of(review));

        List<Review> result = reviewService.getReviewsByUser(user);

        assertEquals(1, result.size());
    }

    @Test
    void givenExistingReview_whenDeleteReviewById_thenDeleteReview() {

        UUID id = UUID.randomUUID();
        Review review = new Review();
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));

        reviewService.deleteReviewById(id);

        verify(reviewRepository).delete(review);
    }

    @Test
    void givenMissingReview_whenDeleteReviewById_thenThrowException() {

        UUID id = UUID.randomUUID();
        when(reviewRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reviewService.deleteReviewById(id));
    }

    @Test
    void whenGetAllReportedReviews_thenReturnList() {

        Review review = new Review();
        when(reviewRepository.findAllByReportedTrueOrderByCreatedOnDesc()).thenReturn(List.of(review));

        List<Review> result = reviewService.getAllReportedReviews();

        assertEquals(1, result.size());
    }

    @Test
    void givenReportedReview_whenRestoreReportedReview_thenUnreportAndSave() {

        UUID id = UUID.randomUUID();
        Review review = new Review();
        review.setReported(true);
        review.setReportReason(ReportReason.SPAM);
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));

        reviewService.restoreReportedReview(id);

        assertFalse(review.isReported());
        assertNull(review.getReportReason());
        verify(reviewRepository).save(review);
    }

    @Test
    void givenMissingReview_whenRestoreReportedReview_thenThrowException() {

        UUID id = UUID.randomUUID();
        when(reviewRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reviewService.restoreReportedReview(id));
    }

    @Test
    void whenDeleteAllReportedReviews_thenDeleteReported() {

        Review review = new Review();
        when(reviewRepository.findAllByReportedTrueOrderByCreatedOnDesc()).thenReturn(List.of(review));

        reviewService.deleteAllReportedReviews();

        verify(reviewRepository).deleteAll(anyList());
    }
}
