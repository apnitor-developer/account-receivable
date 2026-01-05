package com.example.account.receivable.Auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Auth.dto.LoginDto;
import com.example.account.receivable.Auth.dto.LoginResponseDto;
import com.example.account.receivable.User.entity.Role;
import com.example.account.receivable.User.entity.UserRole;
import com.example.account.receivable.User.entity.UserStatus;
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.User.repository.UsersRepository;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    @Test
    void login_whenUserMissing_throwsNotFound() {
        LoginDto dto = new LoginDto();
        dto.setEmail("missing@example.com");
        dto.setPassword("secret");

        when(usersRepository.findByEmailAndDeletedFalse(dto.getEmail())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> loginService.login(dto));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void login_whenPasswordInvalid_throwsUnauthorized() {
        LoginDto dto = new LoginDto();
        dto.setEmail("user@example.com");
        dto.setPassword("plain");

        Users user = buildUser();
        when(usersRepository.findByEmailAndDeletedFalse(dto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getPassword(), user.getPassword())).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> loginService.login(dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void login_whenCredentialsValid_returnsTokenWithRoles() {
        LoginDto dto = new LoginDto();
        dto.setEmail("user@example.com");
        dto.setPassword("plain");

        Users user = buildUser();
        when(usersRepository.findByEmailAndDeletedFalse(dto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(eq(user.getEmail()), anyMap())).thenReturn("jwt-token");

        LoginResponseDto response = loginService.login(dto);

        assertEquals("jwt-token", response.getToken());
        assertEquals(user, response.getUser());
        verify(jwtService).generateToken(eq(user.getEmail()), anyMap());
        assertTrue(user.getUserRoles().stream().anyMatch(r -> "OWNER".equals(r.getRole().getName())));
    }

    private Users buildUser() {
        Users user = Users.builder()
                .id(1L)
                .email("user@example.com")
                .password("hashed")
                .status(UserStatus.ACTIVE)
                .build();

        Role role = Role.builder().id(2L).name("OWNER").build();
        UserRole userRole = UserRole.builder()
                .id(3L)
                .role(role)
                .user(user)
                .build();
        user.setUserRoles(List.of(userRole));
        return user;
    }
}
