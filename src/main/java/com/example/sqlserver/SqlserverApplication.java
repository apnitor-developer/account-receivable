package com.example.sqlserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "EAM Onboarding API",
                version = "v1",
                description = "Company onboarding, financial settings, banking, and roles"
        ),
        servers = {
                @Server(
                        url = "https://47fec906757e.ngrok-free.app",  
                        description = "Ngrok tunnel"
                )
        }
)
public class SqlserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(SqlserverApplication.class, args);
	}

}
