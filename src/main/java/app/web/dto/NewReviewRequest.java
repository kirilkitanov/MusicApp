package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewReviewRequest {

    @NotBlank(message = "Review cannot be empty")
    @Size(max = 500, message = "Review cannot have more than 500 symbols")
    private String comment;

    @NotNull
    private UUID albumId;

}
