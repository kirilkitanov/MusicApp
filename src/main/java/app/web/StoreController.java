package app.web;


import app.album.model.Album;
import app.album.service.AlbumService;
import app.review.service.ReviewService;
import app.security.AuthenticationDetails;
import app.store.service.StoreService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.NewReviewRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequestMapping
public class StoreController {
    private final StoreService storeService;
    private final AlbumService albumService;
    private final UserService userService;
    private final ReviewService reviewService;

    public StoreController(StoreService storeService, AlbumService albumService, UserService userService, ReviewService reviewService) {
        this.storeService = storeService;
        this.albumService = albumService;
        this.userService = userService;
        this.reviewService = reviewService;
    }

    @PostMapping("/cart/add/{albumId}")
    public ModelAndView addToCart(@PathVariable UUID albumId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        storeService.addToCart(authenticationDetails.getUserId(), albumId);

        Album album = albumService.getById(albumId);
        User user = userService.getById(authenticationDetails.getUserId());
        BigDecimal cartTotal = storeService.getCartTotal(authenticationDetails.getUserId());
        ModelAndView modelAndView = new ModelAndView("view-album");
        modelAndView.addObject("album", album);
        modelAndView.addObject("user", user);
        modelAndView.addObject("newReviewRequest", new NewReviewRequest());
        modelAndView.addObject("reviews", reviewService.getReviewsByAlbum(album));
        modelAndView.addObject("cartTotal", cartTotal);

        return modelAndView;
    }

    @GetMapping("/cart")
    public ModelAndView viewCart(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView("cart");
        modelAndView.addObject("user", user);
        modelAndView.addObject("cartItems", storeService.getCart(authenticationDetails.getUserId()));
        modelAndView.addObject("cartTotal", storeService.getCartTotal(authenticationDetails.getUserId()));
        return modelAndView;
    }

    @PostMapping("/cart/remove/{albumId}")
    public ModelAndView removeFromCart(@PathVariable UUID albumId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());

        storeService.removeCartItem(authenticationDetails.getUserId(), albumId);

        ModelAndView modelAndView = new ModelAndView("cart");
        modelAndView.addObject("user", user);
        modelAndView.addObject("cartItems", storeService.getCart(authenticationDetails.getUserId()));
        modelAndView.addObject("cartTotal", storeService.getCartTotal(authenticationDetails.getUserId()));
        return modelAndView;
    }

    @PostMapping("/cart/checkout")
    public ModelAndView checkout(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        Object order = storeService.placeOrder(authenticationDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView("order-success");
        modelAndView.addObject("order", order);

        return modelAndView;
    }

    @GetMapping("/orders")
    public ModelAndView viewOrders(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView("orders-history");
        modelAndView.addObject("user", user);
        modelAndView.addObject("orders", storeService.getOrders(authenticationDetails.getUserId()));

        return modelAndView;
    }

}
