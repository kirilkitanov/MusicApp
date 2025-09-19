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
        // Given
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

        // When
        reviewService.addNewReview(request, user);

        // Then
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(emailService, times(1)).sendEmail(
                eq(albumOwner.getId()),
                contains("New review on your album"),
                contains("Nice album!")
        );
    }

    @Test
    void givenAlbum_whenGetReviewsByAlbum_thenReturnReviews() {
        // Given
        Album album = new Album();
        Review review = new Review();
        when(reviewRepository.findByAlbumOrderByCreatedOnDesc(album)).thenReturn(List.of(review));

        // When
        List<Review> result = reviewService.getReviewsByAlbum(album);

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void givenReviewId_whenGetById_thenReturnReview() {
        // Given
        UUID id = UUID.randomUUID();
        Review review = new Review();
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));

        // When
        Review result = reviewService.getById(id);

        // Then
        assertNotNull(result);
    }

    @Test
    void givenReportRequest_whenReportReview_thenSetReportedAndSave() {
        // Given
        UUID id = UUID.randomUUID();
        Review review = new Review();
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));
        ReportRequest request = new ReportRequest();
        request.setReportReason(ReportReason.SPAM);

        // When
        reviewService.reportReview(id, request);

        // Then
        assertTrue(review.isReported());
        assertEquals(ReportReason.SPAM, review.getReportReason());
        verify(reviewRepository).save(review);
    }

    @Test
    void givenUser_whenGetReviewsByUser_thenReturnList() {
        // Given
        User user = new User();
        Review review = new Review();
        when(reviewRepository.findAllByUserOrderByCreatedOnDesc(user)).thenReturn(List.of(review));

        // When
        List<Review> result = reviewService.getReviewsByUser(user);

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void givenExistingReview_whenDeleteReviewById_thenDeleteReview() {
        // Given
        UUID id = UUID.randomUUID();
        Review review = new Review();
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));

        // When
        reviewService.deleteReviewById(id);

        // Then
        verify(reviewRepository).delete(review);
    }

    @Test
    void givenMissingReview_whenDeleteReviewById_thenThrowException() {
        // Given
        UUID id = UUID.randomUUID();
        when(reviewRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> reviewService.deleteReviewById(id));
    }

    @Test
    void whenGetAllReportedReviews_thenReturnList() {
        // Given
        Review review = new Review();
        when(reviewRepository.findAllByReportedTrueOrderByCreatedOnDesc()).thenReturn(List.of(review));

        // When
        List<Review> result = reviewService.getAllReportedReviews();

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void givenReportedReview_whenRestoreReportedReview_thenUnreportAndSave() {
        // Given
        UUID id = UUID.randomUUID();
        Review review = new Review();
        review.setReported(true);
        review.setReportReason(ReportReason.SPAM);
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));

        // When
        reviewService.restoreReportedReview(id);

        // Then
        assertFalse(review.isReported());
        assertNull(review.getReportReason());
        verify(reviewRepository).save(review);
    }

    @Test
    void givenMissingReview_whenRestoreReportedReview_thenThrowException() {
        // Given
        UUID id = UUID.randomUUID();
        when(reviewRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> reviewService.restoreReportedReview(id));
    }

    @Test
    void whenDeleteAllReportedReviews_thenDeleteReported() {
        // Given
        Review review = new Review();
        when(reviewRepository.findAllByReportedTrueOrderByCreatedOnDesc()).thenReturn(List.of(review));

        // When
        reviewService.deleteAllReportedReviews();

        // Then
        verify(reviewRepository).deleteAll(anyList());
    }


}
