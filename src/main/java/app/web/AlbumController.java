package app.web;

import app.album.service.AlbumService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.NewAlbumRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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


}
