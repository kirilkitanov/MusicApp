package app.store.client;


import app.store.client.dto.AddItemRequest;
import app.store.client.dto.CartItem;
import app.store.client.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "store-svc", url = "http://localhost:8082/api")
public interface StoreClient {

    @PostMapping("/cart/add")
    CartItem addToCart(@RequestBody AddItemRequest request);

    @GetMapping("/cart/sum")
    BigDecimal getCartSum(@RequestParam UUID userId);

    @GetMapping("/cart")
    List<CartItem> getCart(@RequestParam UUID userId);

    @DeleteMapping("/cart/remove")
    void removeCartItem(@RequestParam UUID userId, @RequestParam UUID albumId);

    @PostMapping("/orders/place")
    OrderResponse placeOrder(@RequestParam UUID userId);

    @GetMapping("/orders/my-orders")
    List<OrderResponse> getOrders(@RequestParam UUID userId);

}
