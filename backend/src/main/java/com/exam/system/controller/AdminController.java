package com.exam.system.controller;

import com.exam.system.common.Result;
import com.exam.system.entity.SysAnnouncement;
import com.exam.system.entity.SysClass;
import com.exam.system.entity.SysUser;
import com.exam.system.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // --- Dashboard ---
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(adminService.getDashboardStats());
    }

    // --- User Management ---
    @GetMapping("/users")
    public Result<List<SysUser>> getUsers(@RequestParam(required = false) String role, @RequestParam(required = false) String keyword) {
        return Result.success(adminService.getUsers(role, keyword));
    }

    @PostMapping("/users")
    public Result<?> addUser(@RequestBody SysUser user) {
        adminService.addUser(user);
        return Result.success("用户添加成功");
    }

    @PutMapping("/users")
    public Result<?> updateUser(@RequestBody SysUser user) {
        adminService.updateUser(user);
        return Result.success("用户修改成功");
    }

    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Integer id) {
        adminService.deleteUser(id);
        return Result.success("用户删除成功");
    }

    // --- Class Management ---
    @GetMapping("/classes")
    public Result<List<SysClass>> getClasses(@RequestParam(required = false) String department) {
        return Result.success(adminService.getClasses(department));
    }

    @PostMapping("/classes")
    public Result<?> addClass(@RequestBody SysClass sysClass) {
        adminService.addClass(sysClass);
        return Result.success("班级添加成功");
    }

    @PutMapping("/classes")
    public Result<?> updateClass(@RequestBody SysClass sysClass) {
        adminService.updateClass(sysClass);
        return Result.success("班级更新成功");
    }

    @DeleteMapping("/classes/{id}")
    public Result<?> deleteClass(@PathVariable Integer id) {
        adminService.deleteClass(id);
        return Result.success("班级删除成功");
    }

    // --- Announcements ---
    @GetMapping("/announcements")
    public Result<List<SysAnnouncement>> getAnnouncements() {
        return Result.success(adminService.getAnnouncements());
    }

    @PostMapping("/announcements")
    public Result<?> addAnnouncement(@RequestBody SysAnnouncement announcement) {
        adminService.addAnnouncement(announcement);
        return Result.success("公告发布成功");
    }

    @PutMapping("/announcements")
    public Result<?> updateAnnouncement(@RequestBody SysAnnouncement announcement) {
        adminService.updateAnnouncement(announcement);
        return Result.success("公告更新成功");
    }

    @DeleteMapping("/announcements/{id}")
    public Result<?> deleteAnnouncement(@PathVariable Integer id) {
        adminService.deleteAnnouncement(id);
        return Result.success("公告删除成功");
    }
}
