package app.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private UUID userId;
    private LocalDateTime createdOn;
    private BigDecimal total;
    private List<OrderItemResponse> items;
}
