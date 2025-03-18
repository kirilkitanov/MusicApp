package app.web.dto;

import app.album.model.Genre;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewAlbumRequest {

    @NotBlank(message = "Album name cannot be blank")
    public String albumName;
    @NotBlank(message = "Artist cannot be blank")
    private String artistName;
    private Genre genre;
    private String albumCover;
    private String releaseDate;

}
