package com.exam.system.service;

import com.exam.system.entity.SysAnnouncement;
import com.exam.system.entity.SysClass;
import com.exam.system.entity.SysUser;

import java.util.List;
import java.util.Map;

public interface AdminService {

    /**
     * Dashboard stats
     */
    Map<String, Object> getDashboardStats();

    /**
     * User Management
     */
    List<SysUser> getUsers(String role, String keyword);
    void addUser(SysUser user);
    void updateUser(SysUser user);
    void deleteUser(Integer userId);

    /**
     * Class Management
     */
    List<SysClass> getClasses(String department);
    void addClass(SysClass sysClass);
    void updateClass(SysClass sysClass);
    void deleteClass(Integer classId);

    /**
     * Announcements
     */
    List<SysAnnouncement> getAnnouncements();
    void addAnnouncement(SysAnnouncement announcement);
    void updateAnnouncement(SysAnnouncement announcement);
    void deleteAnnouncement(Integer id);
}
