package com.example.account.receivable.User.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.account.receivable.User.repository.SystemSettingRepository;

@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingRepository repository;

    public int getInt(String key, int defaultValue) {
        return repository.findById(key)
                .map(s -> Integer.parseInt(s.getValue()))
                .orElse(defaultValue);
    }
}
