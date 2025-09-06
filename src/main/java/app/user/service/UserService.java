package app.user.service;

import app.notification.service.EmailService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.web.dto.EditProfileRequest;
import app.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void register(RegisterRequest registerRequest) {

        Optional<User> optionUser = userRepository.findByUsername(registerRequest.getUsername());
        if (optionUser.isPresent()) {
            throw new RuntimeException("Username [%s] already exist.".formatted(registerRequest.getUsername()));
        }

        Optional<User> optionEmail = userRepository.findByEmail(registerRequest.getEmail());
        if (optionEmail.isPresent()) {
            throw new RuntimeException("Email [%s] already exist.".formatted(registerRequest.getEmail()));
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
//                .role(UserRole.FAN)
                .role(registerRequest.getRole())
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        userRepository.save(user);

        emailService.savePreference(user.getId(), true, user.getEmail());

        String subject = "Welcome to MusicApp!";
        String body = "Hello " + user.getUsername() + ",\n\n" +
                        "Thank you for registering at MusicApp.\n" +
                        "You can now explore your favourite albums and write reviews.\n\n" +
                        "Enjoy!\nMusicApp Team";

        emailService.sendEmail (user.getId(), subject, body);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new AuthenticationDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.isActive());
    }

    public User getById(UUID userId) {

        return userRepository.findById(userId).orElseThrow();
    }

    public void editProfileRequest(UUID userId, EditProfileRequest editProfileRequest) {

        User user = getById(userId);

        user.setFirstName(editProfileRequest.getFirstName());
        user.setLastName(editProfileRequest.getLastName());
        user.setEmail(editProfileRequest.getEmail());
        user.setUpdatedOn(LocalDateTime.now());

        if (editProfileRequest.getPassword() != null && !editProfileRequest.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(editProfileRequest.getPassword()));
        }

        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void changeRole(UUID userId, UserRole userRole) {

        User user = getById(userId);
        user.setRole(userRole);
        userRepository.save(user);
    }

    public void changeStatus(UUID userId) {

        User user = getById(userId);

        user.setActive(!user.isActive());

        userRepository.save(user);
    }

}


