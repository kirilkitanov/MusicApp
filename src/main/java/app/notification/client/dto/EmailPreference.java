package app.notification.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailPreference {

    private boolean active;

}
