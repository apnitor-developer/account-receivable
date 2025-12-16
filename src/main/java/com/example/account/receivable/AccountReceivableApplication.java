package com.example.account.receivable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class AccountReceivableApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountReceivableApplication.class, args);
	}

}
