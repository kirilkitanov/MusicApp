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

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditAlbumRequest {

    @NotNull
    private UUID id;

    @NotBlank(message = "Album name cannot be blank")
    @Size(max = 40, message = "Album's name can't have more than 40 symbols")
    public String albumName;

    @NotBlank(message = "Artist cannot be blank")
    @Size(max = 20, message = "Artist's name can't have more than 40 symbols")
    private String artistName;

    private Genre genre;

    @URL(message = "Requires correct web link format")
    private String albumCover;

    private String releaseDate;
}
