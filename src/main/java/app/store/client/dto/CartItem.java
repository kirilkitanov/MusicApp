package app.store.client.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CartItem {
    private UUID albumId;
    private String albumName;
    private String artistName;
    private BigDecimal price;
}
