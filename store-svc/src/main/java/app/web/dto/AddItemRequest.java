package app.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class AddItemRequest {

        private UUID userId;
        private UUID albumId;
        private String albumName;
        private String artistName;
        private BigDecimal price;
        private int quantity;
}
