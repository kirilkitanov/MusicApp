package app.web;

import app.album.model.Album;
import app.album.service.AlbumService;
import app.review.model.Review;
import app.review.service.ReviewService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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

}
