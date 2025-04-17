package app.web.dto;

import app.review.model.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {

    @NotNull(message = "Please select reason")
    private ReportReason reportReason;
}
