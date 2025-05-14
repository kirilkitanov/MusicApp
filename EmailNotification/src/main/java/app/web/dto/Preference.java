package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class Preference {

    @NotNull
    private UUID userId;

    private boolean preferenceActive;

    @NotNull
    @NotBlank
    private String emailAddress;

}
