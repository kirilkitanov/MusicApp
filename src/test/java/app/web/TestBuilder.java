package app.web;


import app.user.model.User;
import app.user.model.UserRole;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .password("123123")
                .email("user@email.com")
                .role(UserRole.FAN)
                .isActive(true)
                .build();

        return user;
    }
}
