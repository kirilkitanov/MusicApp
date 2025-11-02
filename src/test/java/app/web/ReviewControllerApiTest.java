package app.web;

import app.album.model.Album;
import app.album.service.AlbumService;
import app.review.model.ReportReason;
import app.review.model.Review;
import app.review.service.ReviewService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.NewReviewRequest;
import app.web.dto.ReportRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.UUID;

import static app.TestBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ReviewController.class)
public class ReviewControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private AlbumService albumService;

    @MockitoBean
    private UserService userService;

    @Test
    void getReportPage_shouldReturnReportView() throws Exception {
        User user = aRandomUser();
        Album album = aRandomAlbum(user);
        AuthenticationDetails principal = userDetails(user);
        Review review = aRandomReview(user, album);
        UUID reviewId = review.getId();

        when(userService.getById(user.getId())).thenReturn(user);
        when(reviewService.getById(reviewId)).thenReturn(review);

        MockHttpServletRequestBuilder request = get("/reviews/{id}/report", reviewId)
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("new-report"))
                .andExpect(model().attributeExists("user", "review", "reportRequest"));

        verify(reviewService).getById(reviewId);
    }

    @Test
    void getMyReviewsPage_shouldReturnMyReviewsView() throws Exception {
        User user = aRandomUser();
        Album album = aRandomAlbum(user);
        AuthenticationDetails principal = userDetails(user);
        Review review = aRandomReview(user, album);

        when(userService.getById(user.getId())).thenReturn(user);
        when(reviewService.getReviewsByUser(user)).thenReturn(List.of(review));

        MockHttpServletRequestBuilder request = get("/reviews/my-reviews")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("my-reviews"))
                .andExpect(model().attributeExists("user", "reviews"));

        verify(reviewService).getReviewsByUser(user);
    }

    @Test
    void deleteReview_shouldRedirectToDefaultPage() throws Exception {
        User user = aRandomUser();
        AuthenticationDetails principal = userDetails(user);
        UUID reviewId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = delete("/reviews/{id}", reviewId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews/my-reviews"));

        verify(reviewService).deleteReviewById(reviewId);
    }

    @Test
    void restoreReportedReview_shouldRedirectToReportedPage() throws Exception {
        User admin = aRandomUser();
        admin.setRole(UserRole.ADMIN);
        AuthenticationDetails principal = userDetails(admin);
        UUID reviewId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = put("/reviews/{id}/restore-reported", reviewId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews/reported"));

        verify(reviewService).restoreReportedReview(reviewId);
    }

    @Test
    void addNewReview_shouldReturnRedirect_whenValidData() throws Exception {

        UUID albumId = UUID.randomUUID();
        String validComment = "Test review.";

        NewReviewRequest newReviewRequest = new NewReviewRequest();
        newReviewRequest.setAlbumId(albumId);
        newReviewRequest.setComment(validComment);

        User user = aRandomUser();
        AuthenticationDetails principal = userDetails(user);

        Album album = aRandomAlbum(user);
        when(albumService.getById(albumId)).thenReturn(album);
        when(reviewService.getReviewsByAlbum(album)).thenReturn(List.of());
        when(userService.getById(user.getId())).thenReturn(user);

        mockMvc.perform(post("/reviews")
                        .with(user(principal))
                        .with(csrf())
                        .flashAttr("newReviewRequest", newReviewRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/albums/" + albumId + "/view"));

        verify(reviewService).addNewReview(newReviewRequest, user);
    }

    @Test
    void addNewReview_shouldReturnSamePage_whenInvalidData() throws Exception {

        UUID albumId = UUID.randomUUID();
        String invalidComment = "";

        NewReviewRequest newReviewRequest = new NewReviewRequest();
        newReviewRequest.setAlbumId(albumId);
        newReviewRequest.setComment(invalidComment);

        User user = aRandomUser();
        AuthenticationDetails principal = userDetails(user);

        Album album = aRandomAlbum(user);
        when(albumService.getById(albumId)).thenReturn(album);
        when(reviewService.getReviewsByAlbum(album)).thenReturn(List.of());
        when(userService.getById(user.getId())).thenReturn(user);

        mockMvc.perform(post("/reviews")
                        .with(user(principal))
                        .with(csrf())
                        .flashAttr("newReviewRequest", newReviewRequest))
                .andExpect(status().isOk())
                .andExpect(view().name("view-album"))
                .andExpect(model().attributeHasFieldErrors("newReviewRequest", "comment"));
    }

    @Test
    void getReportedReviewPage_shouldReturnReportedReviews_whenAdminHasAccess() throws Exception {

        User adminUser = aRandomUser();
        adminUser.setRole(UserRole.ADMIN);

        Review reportedReview = aRandomReview(adminUser, aRandomAlbum(adminUser));
        reportedReview.setReported(true);
        reportedReview.setReportReason(ReportReason.SPAM);

        List<Review> reportedReviews = List.of(reportedReview);

        when(userService.getById(adminUser.getId())).thenReturn(adminUser);
        when(reviewService.getAllReportedReviews()).thenReturn(reportedReviews);

        MockHttpServletRequestBuilder request = get("/reviews/reported")
                .with(user(userDetails(adminUser)));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("reported-reviews"))
                .andExpect(model().attributeExists("user", "reportedReviews"))
                .andExpect(model().attribute("reportedReviews", reportedReviews));

        verify(reviewService).getAllReportedReviews();
    }

    @Test
    void reportReview_shouldRedirectToAlbumPage_whenValidReportRequest() throws Exception {

        User user = aRandomUser();
        user.setRole(UserRole.FAN);

        Review review = aRandomReview(user, aRandomAlbum(user));
        UUID reviewId = review.getId();
        Album album = review.getAlbum();

        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReportReason(ReportReason.SPAM);

        when(userService.getById(user.getId())).thenReturn(user);
        when(reviewService.getById(reviewId)).thenReturn(review);

        MockHttpServletRequestBuilder request = put("/reviews/{id}/report", reviewId)
                .param("reportReason", reportRequest.getReportReason().name())
                .with(csrf())
                .with(user(userDetails(user)));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/albums/" + album.getId() + "/view"));

        verify(reviewService).reportReview(reviewId, reportRequest);
    }

    @Test
    void reportReview_shouldReturnNewReportView_whenBindingResultHasErrors() throws Exception {

        User user = aRandomUser();
        user.setRole(UserRole.FAN);

        Review review = aRandomReview(user, aRandomAlbum(user));
        UUID reviewId = review.getId();

        ReportRequest reportRequest = new ReportRequest();

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        when(userService.getById(user.getId())).thenReturn(user);
        when(reviewService.getById(reviewId)).thenReturn(review);

        MockHttpServletRequestBuilder request = put("/reviews/{id}/report", reviewId)
                .param("reportReason", reportRequest.getReportReason() == null ? "" : reportRequest.getReportReason().name())
                .with(csrf())
                .with(user(userDetails(user)));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("new-report"))
                .andExpect(model().attributeExists("user", "review", "reportRequest"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("review", review))
                .andExpect(model().attribute("reportRequest", reportRequest));
    }
}
