package com.exam.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.exam.system.common.Result;
import com.exam.system.entity.ExamArrangement;
import com.exam.system.entity.QuestionBank;
import com.exam.system.entity.SysUser;
import com.exam.system.mapper.SysUserMapper;
import com.exam.system.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher")
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private SysUserMapper userMapper;

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("未获取到当前登录用户");
        }

        SysUser user = userMapper.selectOne(new QueryWrapper<SysUser>()
                .eq("username", authentication.getName())
                .last("LIMIT 1"));
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("当前登录用户不存在");
        }
        return user.getUserId();
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(teacherService.getDashboardStats(getCurrentUserId()));
    }

    @GetMapping("/questions")
    public Result<List<QuestionBank>> getQuestions(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        return Result.success(teacherService.getQuestions(getCurrentUserId(), subject, difficulty, type, keyword));
    }

    @PostMapping("/questions")
    public Result<?> addQuestion(@RequestBody QuestionBank question) {
        question.setCreateBy(getCurrentUserId());
        teacherService.addQuestion(question);
        return Result.success("题目添加成功");
    }

    @PutMapping("/questions")
    public Result<?> updateQuestion(@RequestBody QuestionBank question) {
        teacherService.updateQuestion(question);
        return Result.success("题目更新成功");
    }

    @DeleteMapping("/questions/{id}")
    public Result<?> deleteQuestion(@PathVariable Integer id) {
        teacherService.deleteQuestion(id);
        return Result.success("题目删除成功");
    }

    @PostMapping("/exams")
    public Result<?> publishExam(@RequestBody Map<String, Object> payload) {
        String title = (String) payload.get("title");
        String subject = (String) payload.get("subject");
        String targetClasses = (String) payload.get("targetClasses");
        Integer duration = Integer.parseInt(payload.get("duration").toString());
        Map<String, Integer> autoConfig = (Map<String, Integer>) payload.get("autoConfig");
        
        String paperMode = (String) payload.getOrDefault("paperMode", "auto");
        List<Map<String, Object>> manualQuestions = (List<Map<String, Object>>) payload.get("manualQuestions");
        
        // Parse optional start/end times from frontend
        String startTimeStr = payload.get("startTime") != null ? payload.get("startTime").toString() : null;
        String endTimeStr = payload.get("endTime") != null ? payload.get("endTime").toString() : null;
        
        teacherService.publishExam(title, subject, targetClasses, duration, getCurrentUserId(), autoConfig, paperMode, manualQuestions, startTimeStr, endTimeStr);
        return Result.success("发布成功");
    }

    @GetMapping("/exams")
    public Result<List<Map<String, Object>>> getExams() {
        return Result.success(teacherService.getExamsByTeacher(getCurrentUserId()));
    }

    @PutMapping("/exams/{id}")
    public Result<?> updateExam(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        String title = (String) payload.get("title");
        String targetClasses = (String) payload.get("targetClasses");
        Integer duration = Integer.parseInt(payload.get("duration").toString());
        String startTimeStr = payload.get("startTime") != null ? payload.get("startTime").toString() : null;
        String endTimeStr = payload.get("endTime") != null ? payload.get("endTime").toString() : null;

        teacherService.updateExam(id, title, targetClasses, duration, startTimeStr, endTimeStr, getCurrentUserId());
        return Result.success("修改成功");
    }

    @PutMapping("/exams/{id}/status")
    public Result<?> updateExamStatus(@PathVariable Integer id, @RequestParam String status) {
        teacherService.updateExamStatus(id, status);
        return Result.success("状态更新成功");
    }

    @GetMapping("/grading/list")
    public Result<List<Map<String, Object>>> getGradingList(@RequestParam Integer examId, @RequestParam(required = false) Integer classId) {
        return Result.success(teacherService.getPendingGradingList(examId, classId));
    }

    @GetMapping("/grading/answers")
    public Result<?> getGradingAnswers(@RequestParam Integer recordId) {
        return Result.success(teacherService.getGradingAnswers(recordId));
    }

    @PostMapping("/grading/submit")
    public Result<?> submitGrading(@RequestBody Map<String, Object> payload) {
        Integer recordId = Integer.parseInt(payload.get("recordId").toString());
        List<Map<String, Integer>> scores = (List<Map<String, Integer>>) payload.get("scores");
        teacherService.submitGrading(recordId, scores);
        return Result.success("批阅完成");
    }
}
