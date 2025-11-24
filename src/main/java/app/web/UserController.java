package app.web;

import app.exception.EmailAlreadyExistException;
import app.notification.client.dto.EmailPreference;
import app.notification.service.EmailService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.EditProfileRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getEditProfile(@PathVariable UUID id) {

        User user = userService.getById(id);

        EmailPreference emailPreference = emailService.getEmailPreference(user.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("edit-profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("editProfileRequest", DtoMapper.mapUserToEditProfileRequest(user));
        modelAndView.addObject("emailPreference", emailPreference);

        return modelAndView;
    }

//  Извинявам се за коментара, просто не намерих друго решение и затова използвам try/catch тук
    @PutMapping("/{id}/profile")
    public ModelAndView updateUserProfile(@PathVariable UUID id, @Valid EditProfileRequest editProfileRequest, BindingResult bindingResult) {
        User user = userService.getById(id);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName ("edit-profile");

        if (bindingResult.hasErrors()) {
            modelAndView.addObject("user", user);
            modelAndView.addObject("editProfileRequest", editProfileRequest);
            modelAndView.addObject("emailPreference", emailService.getEmailPreference(id));
            return modelAndView;
        }

        try {
            userService.editProfileRequest(id, editProfileRequest);

            user = userService.getById(id);
            EmailPreference preference = emailService.getEmailPreference(id);
            emailService.savePreference(id, preference.isActive(), user.getEmail());

            modelAndView.addObject("user", user);
            modelAndView.addObject("editProfileRequest", DtoMapper.mapUserToEditProfileRequest(user));
            modelAndView.addObject("emailPreference", emailService.getEmailPreference(id));
        }
        catch (EmailAlreadyExistException exception) {
            modelAndView.addObject("emailAlreadyExistMessage", exception.getMessage());
            modelAndView.addObject("user", user);
            modelAndView.addObject("editProfileRequest", editProfileRequest);
            modelAndView.addObject("emailPreference", emailService.getEmailPreference(id));
        }

        return modelAndView;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView getAllUsersPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        List<User> users = userService.getAllUsers();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users");
        modelAndView.addObject("users", users);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PutMapping("/{id}/role")
    public String changeUserRole(@PathVariable UUID id, @RequestParam("role") UserRole userRole) {

        userService.changeRole(id, userRole);

        return "redirect:/users";
    }

    @PutMapping("/{id}/status")
    public String changeUserStatus(@PathVariable UUID id) {

        userService.changeStatus(id);

        return "redirect:/users";
    }

    @PutMapping("/{id}/notifications")
    public String toggleNotificationPreference(@PathVariable UUID id, @RequestParam boolean isPreferenceActive, @RequestParam String emailAddress) {

        emailService.savePreference(id, isPreferenceActive, emailAddress);

        return "redirect:/users/" + id + "/profile";
    }
}
