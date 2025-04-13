package app.web.dto;

import app.album.model.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewAlbumRequest {

    @NotBlank(message = "Album name cannot be blank")
    @Size(max = 40, message = "Album's name can't have more than 40 symbols")
    public String albumName;

    @NotBlank(message = "Artist cannot be blank")
    @Size(max = 20, message = "Artist's name can't have more than 40 symbols")
    private String artistName;

    @NotNull(message = "Please select genre")
    private Genre genre;

    @URL(message = "Requires correct web link format")
    private String albumCover;

    @Size(max = 20, message = "Release Date can't have more than 20 symbols")
    private String releaseDate;

    @Size(max = 1500, message = "Description can't have more than 1500 symbols")
    private String description;

}
