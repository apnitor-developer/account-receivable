package com.example.account.receivable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.account.receivable.Auth.controller.LoginController;
import com.example.account.receivable.Auth.dto.LoginDto;
import com.example.account.receivable.Auth.dto.LoginResponseDto;
import com.example.account.receivable.Auth.service.LoginService;
import com.example.account.receivable.User.controller.SignupController;
import com.example.account.receivable.User.controller.UserController;
import com.example.account.receivable.User.dto.SignupVerifyDto;
import com.example.account.receivable.User.dto.UserCreateDto;
import com.example.account.receivable.User.entity.UserStatus;
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.User.service.SignupService;
import com.example.account.receivable.User.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest({LoginController.class, SignupController.class, UserController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({AuthControllersTest.TestConfig.class, ControllerTestSecurityConfig.class})
class AuthControllersTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private LoginService loginService;

    @Autowired
    private SignupService signupService;

    @Autowired
    private UserService userService;

    @Test
    void login_returnsTokenAndUser() throws Exception {
        Users user = Users.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .status(UserStatus.ACTIVE)
                .build();
        LoginDto request = new LoginDto();
        request.setEmail("jane@example.com");
        request.setPassword("secret");

        when(loginService.login(any(LoginDto.class))).thenReturn(new LoginResponseDto("token-123", user));

        mockMvc.perform(
                post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("token-123"))
                .andExpect(jsonPath("$.data.user.email").value("jane@example.com"));

        verify(loginService).login(any(LoginDto.class));
    }

    @Test
    void signup_sendsOtp() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setFirstName("John");
        dto.setLastName("Smith");
        dto.setEmail("john@yopmail.com");
        dto.setPassword("password123");

        doNothing().when(signupService).startSignup(any(UserCreateDto.class));

        mockMvc.perform(
                post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("OTP sent to email. Please verify."));

        verify(signupService).startSignup(any(UserCreateDto.class));
    }

    @Test
    void verifyOtp_createsUser() throws Exception {
        SignupVerifyDto verifyDto = new SignupVerifyDto();
        verifyDto.setEmail("john@example.com");
        verifyDto.setOtp("123456");

        Users created = Users.builder()
                .id(10L)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .status(UserStatus.ACTIVE)
                .build();

        when(signupService.verifyOtpAndCreateUser(any(SignupVerifyDto.class))).thenReturn(created);

        mockMvc.perform(
                post("/auth/signup/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("john@example.com"));

        verify(signupService).verifyOtpAndCreateUser(any(SignupVerifyDto.class));
    }

    @Test
    void createUser_registersSuccessfully() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setFirstName("Allison");
        dto.setLastName("Lee");
        dto.setEmail("ally@example.com");
        dto.setPassword("password");

        Users user = Users.builder()
                .id(2L)
                .firstName("Allison")
                .lastName("Lee")
                .email("ally@example.com")
                .status(UserStatus.INACTIVE)
                .build();

        when(userService.register(any(UserCreateDto.class))).thenReturn(user);

        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.email").value("ally@example.com"));

        verify(userService).register(any(UserCreateDto.class));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        LoginService loginService() {
            return Mockito.mock(LoginService.class);
        }

        @Bean
        SignupService signupService() {
            return Mockito.mock(SignupService.class);
        }

        @Bean
        UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }
}
