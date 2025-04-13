package app.web;

import app.album.model.Album;
import app.album.service.AlbumService;
import app.favourite.service.FavouriteAlbumService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;


@Controller
public class IndexController {

    private final UserService userService;
    private final AlbumService albumService;
    private final FavouriteAlbumService favouriteAlbumService;

    @Autowired
    public IndexController(UserService userService, AlbumService albumService, FavouriteAlbumService favouriteAlbumService) {
        this.userService = userService;
        this.albumService = albumService;
        this.favouriteAlbumService = favouriteAlbumService;
    }


    @GetMapping("/")
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public String registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.register(registerRequest);

        redirectAttributes.addFlashAttribute("successMessage", "Your account has been successfully created.");

        return "redirect:/login";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParam) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "Incorrect username or password!");
        }

        return modelAndView;
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());

        List<Album> albums = albumService.getAllAlbums();
        List<String> favouriteAlbums = favouriteAlbumService.getFavouriteAlbumByUser(user)
                .stream()
                .map(UUID::toString)
                .toList();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("albums", albums);
        modelAndView.addObject("favouriteAlbums", favouriteAlbums);

        return modelAndView;
    }

}
