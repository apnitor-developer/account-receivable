package com.example.account.receivable.Auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Auth.dto.LoginDto;
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.User.repository.UsersRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UsersRepository usersRepository;


    public Users login(LoginDto dto) {

        Users user = usersRepository.findByEmailAndDeletedFalse(dto.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "User not found"
                                        ));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();                              

        if(!encoder.matches(dto.getPassword(), user.getPassword())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        return user;
    }

}
