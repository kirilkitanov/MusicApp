package app.web.dto;


import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EmailPreferenceResponse {

    private UUID id;

    private UUID userId;

    private boolean active;

    private String emailAddress;

}
