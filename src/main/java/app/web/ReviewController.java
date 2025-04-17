package app.web;

import app.album.model.Album;
import app.album.service.AlbumService;
import app.review.model.Review;
import app.review.service.ReviewService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.NewReviewRequest;
import app.web.dto.ReportRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    private final AlbumService albumService;

    private final UserService userService;

    @Autowired
    public ReviewController(ReviewService reviewService, AlbumService albumService, UserService userService) {
        this.reviewService = reviewService;
        this.albumService = albumService;
        this.userService = userService;
    }

    @PostMapping("/new")
    public ModelAndView addNewReview(@Valid NewReviewRequest newReviewRequest, BindingResult bindingResult,
                                     @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        Album album = albumService.getById(newReviewRequest.getAlbumId());
        List<Review> reviews = reviewService.getReviewsByAlbum(album);

        ModelAndView modelAndView = new ModelAndView("view-album");

        modelAndView.addObject("user", user);
        modelAndView.addObject("album", album);
        modelAndView.addObject("reviews", reviews);
        modelAndView.addObject("newReviewRequest", newReviewRequest);

        if (bindingResult.hasErrors()) {
            return modelAndView;
        }

        reviewService.addNewReview(newReviewRequest, user);
        return new ModelAndView ("redirect:/albums/" + newReviewRequest.getAlbumId() + "/view");
    }

    @GetMapping("/{id}/report")
    public ModelAndView getReportPage(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        Review review = reviewService.getById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("new-report");
        modelAndView.addObject("user", user);
        modelAndView.addObject("review", review);
        modelAndView.addObject("reportRequest", new ReportRequest());

        return modelAndView;
    }

    @PutMapping("/{id}/report")
    public ModelAndView reportReview(@PathVariable UUID id,@Valid ReportRequest reportRequest, BindingResult bindingResult,
                                     @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        Review review = reviewService.getById(id);
        Album album = review.getAlbum();

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("new-report");
            modelAndView.addObject("user", user);
            modelAndView.addObject("review", review);
            modelAndView.addObject("reportRequest", reportRequest);
            return modelAndView;
        }

        reviewService.reportReview(id, reportRequest);

        return new ModelAndView("redirect:/albums/" + album.getId() + "/view");
    }

}
