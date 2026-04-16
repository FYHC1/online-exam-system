package com.exam.system.controller;

import com.exam.system.common.Result;
import com.exam.system.entity.ExamArrangement;
import com.exam.system.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
@PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
public class StudentController {

    @Autowired
    private StudentService studentService;

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Since we stored username in Principal, we need to map to ID, but for PoC we can return a mocked token ID or rely on passed ID
        // Assuming client passes `studentId` explicitly for now to keep it simple, or we can parse from token.
        return 3; // Harcoded to student1 id=3 as defined in sql for now.
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> stats = studentService.getDashboardStats(getCurrentUserId());
        return Result.success(stats);
    }

    @GetMapping("/exams")
    public Result<List<Map<String, Object>>> getAvailableExams(@RequestParam(required = false, defaultValue = "1") Integer classId) {
        List<Map<String, Object>> exams = studentService.getAvailableExams(getCurrentUserId(), classId);
        return Result.success(exams);
    }

    @GetMapping("/records")
    public Result<List<Map<String, Object>>> getRecords() {
        List<Map<String, Object>> records = studentService.getExamRecords(getCurrentUserId());
        return Result.success(records);
    }

    @GetMapping("/exam/{id}/paper")
    public Result<Map<String, Object>> getExamPaper(@PathVariable Integer id) {
        return Result.success(studentService.getExamPaper(id));
    }

    @GetMapping("/wrong-questions")
    public Result<?> getWrongQuestions() {
        return Result.success(studentService.getWrongQuestions(getCurrentUserId()));
    }

    @PostMapping("/wrong-questions/challenge")
    public Result<?> submitWrongQuestionChallenge(@RequestBody Map<String, Object> payload) {
        try {
            Integer questionId = Integer.parseInt(payload.get("questionId").toString());
            boolean correct = Boolean.parseBoolean(payload.get("correct").toString());
            return Result.success(studentService.submitWrongQuestionChallenge(getCurrentUserId(), questionId, correct));
        } catch (Exception e) {
            return Result.error("提交重练结果失败: " + e.getMessage());
        }
    }

    @PostMapping("/exam/submit")
    public Result<?> submitExam(@RequestBody Map<String, Object> payload) {
        try {
            Integer examId = Integer.parseInt(payload.get("examId").toString());
            List<Map<String, Object>> answers = (List<Map<String, Object>>) payload.get("answers");
            boolean abnormalEnd = payload.get("abnormalEnd") != null && Boolean.parseBoolean(payload.get("abnormalEnd").toString());
            Integer cheatCount = payload.get("cheatCount") != null ? Integer.parseInt(payload.get("cheatCount").toString()) : 0;

            studentService.submitExam(getCurrentUserId(), examId, answers, abnormalEnd, cheatCount);
            return Result.success("交卷成功！");
        } catch (Exception e) {
            return Result.error("交卷失败: " + e.getMessage());
        }
    }
}
