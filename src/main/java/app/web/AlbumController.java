package app.web;

import app.album.model.Album;
import app.album.service.AlbumService;
import app.review.model.Review;
import app.review.service.ReviewService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.EditAlbumRequest;
import app.web.dto.NewAlbumRequest;
import app.web.dto.NewReviewRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;


@Controller
@RequestMapping("/albums")
public class AlbumController {

    private final UserService userService;
    private final AlbumService albumService;
    private final ReviewService reviewService;

    @Autowired
    public AlbumController(UserService userService, AlbumService albumService, ReviewService reviewService) {
        this.userService = userService;
        this.albumService = albumService;
        this.reviewService = reviewService;
    }

    @GetMapping("/new")
    public ModelAndView getNewAlbumPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("new-album");
        modelAndView.addObject("user", user);
        modelAndView.addObject("newAlbumRequest", new NewAlbumRequest());

        return modelAndView;
    }

    @PostMapping
    public ModelAndView addNewAlbum(@Valid NewAlbumRequest newAlbumRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("new-album");
            modelAndView.addObject("user", user);
            modelAndView.addObject("newAlbumRequest", newAlbumRequest);
            return modelAndView;
        }

        albumService.addNewAlbum(newAlbumRequest, user);

        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/added")
    public ModelAndView getMyUploadedAlbums(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());

        List<Album> uploadedAlbums = albumService.findAlbumsByUser(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("uploaded-albums");
        modelAndView.addObject("user", user);
        modelAndView.addObject("uploadedAlbums", uploadedAlbums);

        return modelAndView;
    }

    @PutMapping("/{id}/status")
    public String changeAlbumStatus(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        albumService.changeAlbumStatus(id, authenticationDetails.getUserId());

        return "redirect:/albums/added";
    }

    @GetMapping("/{id}/edit")
    public ModelAndView getEditAlbumPage(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) throws AccessDeniedException {

        User user = userService.getById(authenticationDetails.getUserId());

        Album album = albumService.findAndCheckAlbumOwnership(id, authenticationDetails.getUserId());

        EditAlbumRequest editAlbumRequest = DtoMapper.mapAlbumToEditAlbumRequest(album);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("edit-album");
        modelAndView.addObject("user", user);
        modelAndView.addObject("editAlbumRequest", editAlbumRequest);

        return modelAndView;
    }

    @PutMapping("/{id}/edit")
    public ModelAndView updateEditAlbumPage (@PathVariable UUID id, @Valid EditAlbumRequest editAlbumRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) throws AccessDeniedException {

        if (bindingResult.hasErrors()) {
            User user = userService.getById(authenticationDetails.getUserId());
            Album album = albumService.findAndCheckAlbumOwnership(id, authenticationDetails.getUserId());
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("edit-album");
            modelAndView.addObject("user", user);
            modelAndView.addObject("editAlbumRequest", editAlbumRequest);
            return modelAndView;
        }

        User user = userService.getById(authenticationDetails.getUserId());
        albumService.updateAlbum(id, editAlbumRequest, user);

        return new ModelAndView("redirect:/albums/added");
    }

    @GetMapping("/{id}/view")
    public ModelAndView viewAlbumDetails(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        Album album = albumService.getById(id);
        List<Review> reviews = reviewService.getReviewsByAlbum(album);

        NewReviewRequest newReviewRequest = new NewReviewRequest();
        newReviewRequest.setAlbumId(id);

        ModelAndView modelAndView = new ModelAndView("view-album");
        modelAndView.addObject("user", user);
        modelAndView.addObject("album", album);
        modelAndView.addObject("newReviewRequest", newReviewRequest);
        modelAndView.addObject("reviews", reviews);

        return modelAndView;
    }



}
