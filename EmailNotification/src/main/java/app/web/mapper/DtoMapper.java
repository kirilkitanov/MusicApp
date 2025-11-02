package app.web.mapper;

import app.model.Email;
import app.model.EmailPreference;
import app.web.dto.EmailPreferenceResponse;
import app.web.dto.EmailResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static EmailPreferenceResponse fromEmailPreference(EmailPreference emailPreference) {

        return EmailPreferenceResponse.builder()
                .id(emailPreference.getId())
                .userId(emailPreference.getUserId())
                .active(emailPreference.isActive())
                .emailAddress(emailPreference.getEmailAddress())
                .build();
    }

    public static EmailResponse fromEmail(Email email) {

        return EmailResponse.builder()
                .status(email.getStatus())
                .build();
    }
}
