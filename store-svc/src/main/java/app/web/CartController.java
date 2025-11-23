package app.web;

import app.model.CartItem;
import app.service.CartService;
import app.web.dto.AddItemRequest;
import app.web.dto.CartItemResponse;
import app.web.mapper.CartMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart(@RequestParam UUID userId) {
        List<CartItemResponse> cartItems = cartService.getCart(userId)
                .stream()
                .map(CartMapper::toResponse)
                .toList();
        return ResponseEntity.ok(cartItems);
    }

    @GetMapping("/sum")
    public ResponseEntity<BigDecimal> getCartSum(@RequestParam UUID userId) {
        return ResponseEntity.ok(cartService.getCurrentSum(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<CartItemResponse> addItem(@RequestBody AddItemRequest request) {
        CartItem item = CartMapper.toCartItem(request);
        CartItem savedItem = cartService.addItemToCart(item);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CartMapper.toResponse(savedItem));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeItem(@RequestParam UUID userId, @RequestParam UUID albumId) {
        cartService.removeItemByAlbum(userId, albumId);
        return ResponseEntity.noContent().build();
    }
}
