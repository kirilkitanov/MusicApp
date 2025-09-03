package app.web.dto;

import app.model.EmailStatus;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class EmailResponse {

    private EmailStatus status;

}
