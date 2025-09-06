package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditProfileRequest {

    @Size(max = 30, message = "First name must be up to 30 characters!")
    private String firstName;

    @Size(max = 30, message = "First name must be up to 30 characters!")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be a valid email address")
    private String email;

    @Pattern(regexp = "^$|.{6,}$", message = "Password must be at least 6 characters")
    private String password;

}
