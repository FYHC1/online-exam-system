package com.exam.system.controller;

import com.exam.system.common.Result;
import com.exam.system.entity.SysAnnouncement;
import com.exam.system.entity.SysClass;
import com.exam.system.entity.QuestionBank;
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

    private Integer getCurrentUserId() {
        return 1;
    }

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

    @GetMapping("/resources/questions")
    public Result<List<Map<String, Object>>> getGlobalQuestions() {
        return Result.success(adminService.getGlobalQuestions());
    }

    @GetMapping("/resources/exams")
    public Result<List<Map<String, Object>>> getGlobalExams() {
        return Result.success(adminService.getGlobalExams());
    }

    @GetMapping("/org-structure")
    public Result<List<Map<String, Object>>> getOrgStructure() {
        return Result.success(adminService.getOrgStructure());
    }

    @GetMapping("/terms")
    public Result<Map<String, Object>> getTermSettings() {
        return Result.success(adminService.getTermSettings());
    }

    @PostMapping("/terms")
    public Result<?> addTerm(@RequestBody Map<String, Object> payload) {
        adminService.addTerm(payload);
        return Result.success("学期新增成功");
    }

    @PutMapping("/terms/current")
    public Result<?> updateCurrentTerm(@RequestBody Map<String, Object> payload) {
        adminService.updateCurrentTerm(payload);
        return Result.success("当前学期已更新");
    }

    @PostMapping("/org-structure/nodes")
    public Result<?> addOrgNode(@RequestBody Map<String, Object> payload) {
        adminService.addOrgNode(payload);
        return Result.success("组织节点新增成功");
    }

    @PutMapping("/org-structure/nodes")
    public Result<?> updateOrgNode(@RequestBody Map<String, Object> payload) {
        adminService.updateOrgNode(payload);
        return Result.success("组织节点更新成功");
    }

    @GetMapping("/resources/questions/{id}")
    public Result<Map<String, Object>> getGlobalQuestionDetail(@PathVariable Integer id) {
        return Result.success(adminService.getGlobalQuestionDetail(id));
    }

    @PostMapping("/resources/questions")
    public Result<?> addGlobalQuestion(@RequestBody QuestionBank question) {
        adminService.addGlobalQuestion(question, getCurrentUserId());
        return Result.success("题目添加成功");
    }

    @DeleteMapping("/resources/questions/{id}")
    public Result<?> deleteGlobalQuestion(@PathVariable Integer id) {
        adminService.deleteGlobalQuestion(id);
        return Result.success("题目已移出资源池");
    }

    @GetMapping("/resources/exams/{id}")
    public Result<Map<String, Object>> getGlobalExamDetail(@PathVariable Integer id) {
        return Result.success(adminService.getGlobalExamDetail(id));
    }

    @PutMapping("/resources/exams/{id}/status")
    public Result<?> updateGlobalExamStatus(@PathVariable Integer id, @RequestParam String status) {
        adminService.updateGlobalExamStatus(id, status);
        return Result.success("试卷状态已更新");
    }

    @DeleteMapping("/resources/exams/{id}")
    public Result<?> deleteGlobalExam(@PathVariable Integer id) {
        adminService.deleteGlobalExam(id);
        return Result.success("试卷已移出资源池");
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
