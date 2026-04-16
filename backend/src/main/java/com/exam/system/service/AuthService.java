package com.exam.system.service;

import com.exam.system.entity.SysUser;
import java.util.Map;

public interface AuthService {

    /**
     * Get graphical captcha
     */
    Map<String, String> getCaptcha();

    /**
     * User login
     */
    Map<String, Object> login(String username, String password, String role, String code, String uuid);

    /**
     * User registration (students only)
     */
    void register(SysUser sysUser);
}
