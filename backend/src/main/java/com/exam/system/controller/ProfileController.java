package com.exam.system.controller;

import com.exam.system.common.Result;
import com.exam.system.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    private String getCurrentUsername(Authentication authentication) {
        return authentication.getName();
    }

    @GetMapping("/me")
    public Result<?> getProfile(Authentication authentication) {
        return Result.success(profileService.getProfile(getCurrentUsername(authentication)));
    }

    @GetMapping("/terms")
    public Result<?> getTermOptions() {
        return Result.success(profileService.getTermOptions());
    }

    @PutMapping("/me")
    public Result<?> updateProfile(Authentication authentication, @RequestBody Map<String, Object> payload) {
        return Result.success(profileService.updateProfile(getCurrentUsername(authentication), payload));
    }

    @PutMapping("/password")
    public Result<?> changePassword(Authentication authentication, @RequestBody Map<String, Object> payload) {
        profileService.changePassword(
            getCurrentUsername(authentication),
            String.valueOf(payload.getOrDefault("oldPassword", "")),
            String.valueOf(payload.getOrDefault("newPassword", ""))
        );
        return Result.success("密码修改成功");
    }
}
