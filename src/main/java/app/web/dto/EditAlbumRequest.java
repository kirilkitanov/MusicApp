package app.web.dto;

import app.album.model.Genre;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditAlbumRequest {

    @NotNull
    private UUID id;

    @NotBlank(message = "Album name cannot be blank")
    @Size(max = 40, message = "Album's name cannot have more than 40 symbols")
    public String albumName;

    @NotBlank(message = "Artist cannot be blank")
    @Size(max = 40, message = "Artist's name cannot have more than 40 symbols")
    private String artistName;

    @NotNull(message = "Please select genre")
    private Genre genre;

    @URL(message = "Requires correct web link format")
    private String albumCover;

    @Size(max = 20, message = "Release Date cannot have more than 20 symbols")
    private String releaseDate;

    @Size(max = 1500, message = "Description cannot have more than 1500 symbols")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal price;

    @Size(max = 20, message = "YouTube Video ID cannot have more than 20 symbols")
    @Pattern(regexp = "^$|^[a-zA-Z0-9_-]+$", message = "Enter valid ID")
    private String youtubeVideoId;
}
