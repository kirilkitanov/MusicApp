package app.web;

import app.album.model.Album;
import app.album.service.AlbumService;
import app.album.model.FavouriteAlbum;
import app.album.service.FavouriteAlbumService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/albums/favourites")
public class FavouriteAlbumController {

    private final FavouriteAlbumService favouriteAlbumService;
    private final AlbumService albumService;
    private final UserService userService;

    @Autowired
    public FavouriteAlbumController(FavouriteAlbumService favouriteAlbumService, AlbumService albumService, UserService userService) {
        this.favouriteAlbumService = favouriteAlbumService;
        this.albumService = albumService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView showFavourites(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        List<FavouriteAlbum> favorites = favouriteAlbumService.getFavoritesByUser(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("favourite-albums");
        modelAndView.addObject("user", user);
        modelAndView.addObject("favorites", favorites);

        return modelAndView;
    }
    @PostMapping("/{albumId}")
    public String makeFavouriteAlbum(@PathVariable UUID albumId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails){

        User user = userService.getById(authenticationDetails.getUserId());
        Album album = albumService.getById(albumId);

        favouriteAlbumService.addToFavourites(user, album);

        return "redirect:/home";

    }

    @DeleteMapping("/{albumId}")
    public String deleteFavouriteAlbum(@PathVariable UUID albumId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails){

        User user = userService.getById(authenticationDetails.getUserId());
        Album album = albumService.getById(albumId);

        favouriteAlbumService.deleteFavourites(user, album);

        return "redirect:/albums/favourites";

    }

    @DeleteMapping("/{albumId}/home")
    public String deleteFavouriteAlbumAndRedirectHome(@PathVariable UUID albumId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails){

        User user = userService.getById(authenticationDetails.getUserId());
        Album album = albumService.getById(albumId);

        favouriteAlbumService.deleteFavourites(user, album);

        return "redirect:/home";

    }




}
