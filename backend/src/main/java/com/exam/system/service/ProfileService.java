package com.exam.system.service;

import java.util.Map;

public interface ProfileService {
    Map<String, Object> getProfile(String username);
    Map<String, Object> getTermOptions();
    Map<String, Object> updateProfile(String username, Map<String, Object> payload);
    void changePassword(String username, String oldPassword, String newPassword);
}
