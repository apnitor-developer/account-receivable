package com.example.account.receivable.User.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.User.entity.SystemSetting;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
}
