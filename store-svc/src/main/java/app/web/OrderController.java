package app.web;

import app.service.OrderService;
import app.web.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public ResponseEntity<OrderResponse> placeOrder(@RequestParam UUID userId) {
        OrderResponse order = orderService.placeOrder(userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(order);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getOrders(@RequestParam UUID userId) {
        List<OrderResponse> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }
}
