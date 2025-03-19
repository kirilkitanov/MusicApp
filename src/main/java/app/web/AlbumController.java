package app.web;

import app.album.model.Album;
import app.album.service.AlbumService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.NewAlbumRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;


@Controller
@RequestMapping("/albums")
public class AlbumController {

    private final UserService userService;
    private final AlbumService albumService;

    @Autowired
    public AlbumController(UserService userService, AlbumService albumService) {
        this.userService = userService;
        this.albumService = albumService;
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
    public String addNewAlbum(@Valid NewAlbumRequest newAlbumRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        if (bindingResult.hasErrors()) {
            return "new-album";
        }

         User user = userService.getById(authenticationDetails.getUserId());

        albumService.addNewAlbum(newAlbumRequest, user);

        return "redirect:/home";
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


}
