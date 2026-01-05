package com.example.account.receivable;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.example.account.receivable.Auth.service.JwtService;

@TestConfiguration
public class ControllerTestSecurityConfig {

    @Bean
    public JwtService jwtService() {
        return Mockito.mock(JwtService.class);
    }
}
