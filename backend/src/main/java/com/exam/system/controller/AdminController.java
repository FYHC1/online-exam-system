package com.exam.system.controller;

import com.exam.system.common.Result;
import com.exam.system.entity.SysAnnouncement;
import com.exam.system.entity.SysClass;
import com.exam.system.entity.QuestionBank;
import com.exam.system.entity.SysUser;
import com.exam.system.mapper.SysUserMapper;
import com.exam.system.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SysUserMapper userMapper;

    private Integer getCurrentUserId() {
        return 1;
    }

    private String getActor() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "管理员";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void audit(HttpServletRequest request, String action, boolean risk) {
        adminService.recordAuditLog(getActor(), action, getClientIp(request), risk);
    }

    private String userAuditLabel(SysUser user) {
        if (user == null) {
            return "未知用户";
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        if (user.getUserId() != null) {
            SysUser existing = userMapper.selectById(user.getUserId());
            if (existing != null && existing.getUsername() != null && !existing.getUsername().isBlank()) {
                return existing.getUsername();
            }
            return "用户ID " + user.getUserId();
        }
        return "未知用户";
    }

    // --- Dashboard ---
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(adminService.getDashboardStats());
    }

    @GetMapping("/analytics")
    public Result<Map<String, Object>> getAnalytics() {
        return Result.success(adminService.getAnalyticsStats());
    }

    @GetMapping("/audit-logs")
    public Result<List<Map<String, Object>>> getAuditLogs(@RequestParam(required = false) String keyword) {
        return Result.success(adminService.getAuditLogs(keyword));
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

    @GetMapping("/resources/subjects")
    public Result<List<String>> getSubjectCategories() {
        return Result.success(adminService.getSubjectCategories());
    }

    @PostMapping("/resources/subjects")
    public Result<?> addSubjectCategory(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Object subject = payload.get("subject");
        adminService.addSubjectCategory(subject == null ? "" : String.valueOf(subject));
        audit(request, "新增学科分类：" + subject, false);
        return Result.success("学科新增成功");
    }

    @GetMapping("/resources/question-banks")
    public Result<List<Map<String, Object>>> getQuestionBanks() {
        return Result.success(adminService.getQuestionBanks());
    }

    @PostMapping("/resources/question-banks")
    public Result<?> addQuestionBank(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        adminService.addQuestionBank(payload);
        audit(request, "新增题库：" + payload.getOrDefault("name", ""), false);
        return Result.success("题库新增成功");
    }

    @PutMapping("/resources/question-banks/{id}")
    public Result<?> updateQuestionBank(@PathVariable String id, @RequestBody Map<String, Object> payload, HttpServletRequest request) {
        adminService.updateQuestionBank(id, payload);
        audit(request, "更新题库配置：" + id, false);
        return Result.success("题库更新成功");
    }

    @DeleteMapping("/resources/question-banks/{id}")
    public Result<?> deleteQuestionBank(@PathVariable String id, HttpServletRequest request) {
        adminService.deleteQuestionBank(id);
        audit(request, "删除题库：" + id, true);
        return Result.success("题库删除成功");
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
    public Result<?> addTerm(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        adminService.addTerm(payload);
        audit(request, "新增学期", false);
        return Result.success("学期新增成功");
    }

    @PutMapping("/terms/current")
    public Result<?> updateCurrentTerm(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        adminService.updateCurrentTerm(payload);
        audit(request, "切换当前学期为：" + payload.getOrDefault("name", ""), true);
        return Result.success("当前学期已更新");
    }

    @GetMapping("/login-carousels")
    public Result<List<Map<String, Object>>> getLoginCarousels() {
        return Result.success(adminService.getLoginCarousels(false));
    }

    @PostMapping("/login-carousels")
    public Result<?> addLoginCarousel(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        adminService.addLoginCarousel(payload);
        audit(request, "新增登录页轮播图：" + payload.getOrDefault("title", ""), false);
        return Result.success("轮播图新增成功");
    }

    @PutMapping("/login-carousels/{id}")
    public Result<?> updateLoginCarousel(@PathVariable String id, @RequestBody Map<String, Object> payload, HttpServletRequest request) {
        adminService.updateLoginCarousel(id, payload);
        audit(request, "更新登录页轮播图：" + id, false);
        return Result.success("轮播图更新成功");
    }

    @DeleteMapping("/login-carousels/{id}")
    public Result<?> deleteLoginCarousel(@PathVariable String id, HttpServletRequest request) {
        adminService.deleteLoginCarousel(id);
        audit(request, "删除登录页轮播图：" + id, true);
        return Result.success("轮播图删除成功");
    }

    @PostMapping("/org-structure/nodes")
    public Result<?> addOrgNode(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        adminService.addOrgNode(payload);
        audit(request, "新增组织分类：" + payload.getOrDefault("name", ""), false);
        return Result.success("组织节点新增成功");
    }

    @PutMapping("/org-structure/nodes")
    public Result<?> updateOrgNode(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        adminService.updateOrgNode(payload);
        audit(request, "更新组织分类：" + payload.getOrDefault("name", ""), false);
        return Result.success("组织节点更新成功");
    }

    @DeleteMapping("/org-structure/nodes")
    public Result<?> deleteOrgNode(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        adminService.deleteOrgNode(payload);
        audit(request, "删除组织分类：" + payload.getOrDefault("type", ""), true);
        return Result.success("组织节点删除成功");
    }

    @GetMapping("/resources/questions/{id}")
    public Result<Map<String, Object>> getGlobalQuestionDetail(@PathVariable Integer id) {
        return Result.success(adminService.getGlobalQuestionDetail(id));
    }

    @PostMapping("/resources/questions")
    public Result<?> addGlobalQuestion(@RequestBody QuestionBank question, HttpServletRequest request) {
        adminService.addGlobalQuestion(question, getCurrentUserId());
        audit(request, "新增题目：" + question.getTitle(), false);
        return Result.success("题目添加成功");
    }

    @DeleteMapping("/resources/questions/{id}")
    public Result<?> deleteGlobalQuestion(@PathVariable Integer id, HttpServletRequest request) {
        adminService.deleteGlobalQuestion(id);
        audit(request, "删除题目：" + id, true);
        return Result.success("题目已移出资源池");
    }

    @GetMapping("/resources/exams/{id}")
    public Result<Map<String, Object>> getGlobalExamDetail(@PathVariable Integer id) {
        return Result.success(adminService.getGlobalExamDetail(id));
    }

    @PutMapping("/resources/exams/{id}/status")
    public Result<?> updateGlobalExamStatus(@PathVariable Integer id, @RequestParam String status, HttpServletRequest request) {
        adminService.updateGlobalExamStatus(id, status);
        audit(request, "更新考试状态：" + id + " -> " + status, true);
        return Result.success("试卷状态已更新");
    }

    @DeleteMapping("/resources/exams/{id}")
    public Result<?> deleteGlobalExam(@PathVariable Integer id, HttpServletRequest request) {
        adminService.deleteGlobalExam(id);
        audit(request, "删除考试资源：" + id, true);
        return Result.success("试卷已移出资源池");
    }

    @PostMapping("/users")
    public Result<?> addUser(@RequestBody SysUser user, HttpServletRequest request) {
        adminService.addUser(user);
        audit(request, "新增用户：" + user.getUsername(), true);
        return Result.success("用户添加成功");
    }

    @PutMapping("/users")
    public Result<?> updateUser(@RequestBody SysUser user, HttpServletRequest request) {
        String label = userAuditLabel(user);
        adminService.updateUser(user);
        audit(request, "更新用户：" + label, true);
        return Result.success("用户修改成功");
    }

    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Integer id, HttpServletRequest request) {
        adminService.deleteUser(id);
        audit(request, "删除用户：" + id, true);
        return Result.success("用户删除成功");
    }

    // --- Class Management ---
    @GetMapping("/classes")
    public Result<List<SysClass>> getClasses(@RequestParam(required = false) String department) {
        return Result.success(adminService.getClasses(department));
    }

    @PostMapping("/classes")
    public Result<?> addClass(@RequestBody SysClass sysClass, HttpServletRequest request) {
        adminService.addClass(sysClass);
        audit(request, "新增班级：" + sysClass.getClassName(), false);
        return Result.success("班级添加成功");
    }

    @PutMapping("/classes")
    public Result<?> updateClass(@RequestBody SysClass sysClass, HttpServletRequest request) {
        adminService.updateClass(sysClass);
        audit(request, "更新班级：" + sysClass.getClassName(), false);
        return Result.success("班级更新成功");
    }

    @DeleteMapping("/classes/{id}")
    public Result<?> deleteClass(@PathVariable Integer id, HttpServletRequest request) {
        adminService.deleteClass(id);
        audit(request, "删除班级：" + id, true);
        return Result.success("班级删除成功");
    }

    // --- Announcements ---
    @GetMapping("/announcements")
    public Result<List<SysAnnouncement>> getAnnouncements() {
        return Result.success(adminService.getAnnouncements());
    }

    @PostMapping("/announcements")
    public Result<?> addAnnouncement(@RequestBody SysAnnouncement announcement, HttpServletRequest request) {
        adminService.addAnnouncement(announcement);
        audit(request, "发布公告：" + announcement.getTitle(), false);
        return Result.success("公告发布成功");
    }

    @PutMapping("/announcements")
    public Result<?> updateAnnouncement(@RequestBody SysAnnouncement announcement, HttpServletRequest request) {
        adminService.updateAnnouncement(announcement);
        audit(request, "更新公告：" + announcement.getTitle(), false);
        return Result.success("公告更新成功");
    }

    @DeleteMapping("/announcements/{id}")
    public Result<?> deleteAnnouncement(@PathVariable Integer id, HttpServletRequest request) {
        adminService.deleteAnnouncement(id);
        audit(request, "删除公告：" + id, true);
        return Result.success("公告删除成功");
    }
}
