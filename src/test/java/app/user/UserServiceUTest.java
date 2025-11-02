package app.user;

import app.exception.EmailAlreadyExistException;
import app.exception.UsernameAlreadyExistException;
import app.notification.service.EmailService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.EditProfileRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @Test
    void givenExistingUsername_whenRegister_thenThrowException() {

        RegisterRequest request = new RegisterRequest("testUser", "pass", "test@email.com", UserRole.FAN);
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(new User()));

        assertThrows(UsernameAlreadyExistException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
        verify(emailService, never()).savePreference(any(), anyBoolean(), anyString());
    }

    @Test
    void givenExistingEmail_whenRegister_thenThrowException() {

        RegisterRequest request = new RegisterRequest("testUser", "pass", "test@email.com", UserRole.FAN);
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyExistException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
        verify(emailService, never()).savePreference(any(), anyBoolean(), anyString());
    }

    @Test
    void givenValidRegisterRequest_whenRegister_thenSaveUserAndSendEmail() {

        RegisterRequest request = RegisterRequest.builder()
                .username("testUser")
                .password("testPass")
                .email("test@email.com")
                .role(UserRole.FAN)
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        userService.register(request);

        verify(emailService).savePreference(any(UUID.class), eq(true), eq(request.getEmail()));
        verify(emailService).sendEmail(any(UUID.class), eq("Welcome to MusicApp!"), contains("Hello testUser"));
    }

    @Test
    void givenExistingUser_whenLoadUserByUsername_thenReturnAuthenticationDetails() {

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("testUser")
                .password("encodedPass")
                .role(UserRole.FAN)
                .isActive(true)
                .build();
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        var result = userService.loadUserByUsername("testUser");

        assertInstanceOf(AuthenticationDetails.class, result);
        assertEquals("testUser", result.getUsername());
    }

    @Test
    void givenMissingUser_whenLoadUserByUsername_thenThrowException() {

        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("missing"));
    }

    @Test
    void givenValidEditRequest_whenEditProfile_thenUpdateAndSaveUser() {

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("testUser")
                .email("old@email.com")
                .password("oldPass")
                .build();

        EditProfileRequest dto = new EditProfileRequest("NewFirst", "NewLast", "new@email.com", "newPass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPass");

        userService.editProfileRequest(userId, dto);

        assertEquals("NewFirst", user.getFirstName());
        assertEquals("NewLast", user.getLastName());
        assertEquals("new@email.com", user.getEmail());
        assertEquals("encodedPass", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUser_whenChangeRole_thenRoleIsUpdated() {

        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).role(UserRole.FAN).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeRole(userId, UserRole.ADMIN);

        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenActiveUser_whenChangeStatus_thenBecomeInactive() {

        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).isActive(true).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeStatus(userId);

        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenInactiveUser_whenChangeStatus_thenBecomeActive() {

        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).isActive(false).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeStatus(userId);

        assertTrue(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUsersInDatabase_whenGetAllUsers_thenReturnList() {

        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
    }

    @Test
    void givenExistingUser_whenGetById_thenReturnUser() {

        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("testUser").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getById(userId);

        assertEquals(userId, result.getId());
        assertEquals("testUser", result.getUsername());
    }

    @Test
    void givenMissingUser_whenGetById_thenThrowException() {

        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.getById(userId));
    }

    @Test
    void givenExistingEmail_whenEditProfile_thenThrowEmailAlreadyExistException() {

        UUID userId = UUID.randomUUID();
        EditProfileRequest request = EditProfileRequest.builder()
                .firstName("firstName")
                .lastName("lastName")
                .email("existing@email.com")
                .password("newPass")
                .build();

        User user = User.builder()
                .id(userId)
                .email("old@email.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyExistException.class, () -> userService.editProfileRequest(userId, request));

        verify(userRepository, never()).save(any(User.class));
    }
}
