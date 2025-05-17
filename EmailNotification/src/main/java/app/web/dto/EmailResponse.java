package app.web.dto;

import app.model.EmailStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmailResponse {

    private EmailStatus status;

}
