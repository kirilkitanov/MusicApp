package app.notification.client.dto;


import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreatePreference {

    private UUID userId;

    private boolean preferenceActive;

    private String emailAddress;
}
