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

    // Register tests
    @Test
    void givenExistingUsername_whenRegister_thenThrowException() {
        // Given
        RegisterRequest request = new RegisterRequest("testUser", "pass", "test@email.com", UserRole.FAN);
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(new User()));

        // When & Then
        assertThrows(UsernameAlreadyExistException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
        verify(emailService, never()).savePreference(any(), anyBoolean(), anyString());
    }

    @Test
    void givenExistingEmail_whenRegister_thenThrowException() {
        // Given
        RegisterRequest request = new RegisterRequest("testUser", "pass", "test@email.com", UserRole.FAN);
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        // When & Then
        assertThrows(EmailAlreadyExistException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
        verify(emailService, never()).savePreference(any(), anyBoolean(), anyString());
    }

    @Test
    void givenValidRegisterRequest_whenRegister_thenSaveUserAndSendEmail() {

        // Given
        RegisterRequest request = RegisterRequest.builder()
                .username("testUser")
                .password("testPass")
                .email("test@email.com")
                .role(UserRole.FAN)
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");

        // Симулираме, че save() задава ID на user
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        // When
        userService.register(request);

        // Then
        verify(emailService).savePreference(any(UUID.class), eq(true), eq(request.getEmail()));
        verify(emailService).sendEmail(any(UUID.class), eq("Welcome to MusicApp!"), contains("Hello testUser"));
    }

    // loadUserByUsername tests
    @Test
    void givenExistingUser_whenLoadUserByUsername_thenReturnAuthenticationDetails() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("testUser")
                .password("encodedPass")
                .role(UserRole.FAN)
                .isActive(true)
                .build();
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        // When
        var result = userService.loadUserByUsername("testUser");

        // Then
        assertInstanceOf(AuthenticationDetails.class, result);
        assertEquals("testUser", result.getUsername());
    }

    @Test
    void givenMissingUser_whenLoadUserByUsername_thenThrowException() {
        // Given
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("missing"));
    }

    // editProfileRequest tests
    @Test
    void givenValidEditRequest_whenEditProfile_thenUpdateAndSaveUser() {
        // Given
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

        // When
        userService.editProfileRequest(userId, dto);

        // Then
        assertEquals("NewFirst", user.getFirstName());
        assertEquals("NewLast", user.getLastName());
        assertEquals("new@email.com", user.getEmail());
        assertEquals("encodedPass", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    // changeRole tests
    @Test
    void givenUser_whenChangeRole_thenRoleIsUpdated() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).role(UserRole.FAN).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.changeRole(userId, UserRole.ADMIN);

        // Then
        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    // changeStatus tests
    @Test
    void givenActiveUser_whenChangeStatus_thenBecomeInactive() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).isActive(true).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.changeStatus(userId);

        // Then
        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenInactiveUser_whenChangeStatus_thenBecomeActive() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).isActive(false).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.changeStatus(userId);

        // Then
        assertTrue(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    // getAllUsers test
    @Test
    void givenUsersInDatabase_whenGetAllUsers_thenReturnList() {
        // Given
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    void givenExistingUser_whenGetById_thenReturnUser() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("testUser").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        User result = userService.getById(userId);

        // Then
        assertEquals(userId, result.getId());
        assertEquals("testUser", result.getUsername());
    }

    @Test
    void givenMissingUser_whenGetById_thenThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> userService.getById(userId));
    }

    @Test
    void givenExistingEmail_whenEditProfile_thenThrowEmailAlreadyExistException() {
        // Given
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

        // When & Then
        assertThrows(EmailAlreadyExistException.class, () -> userService.editProfileRequest(userId, request));

        // Проверяваме, че save не е извикан
        verify(userRepository, never()).save(any(User.class));
    }



}
