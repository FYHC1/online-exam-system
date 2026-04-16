package com.exam.system.service;

import com.exam.system.entity.ExamArrangement;
import com.exam.system.entity.StudentExamRecord;

import java.util.List;
import java.util.Map;

public interface StudentService {
    
    /**
     * Get dashboard summary (total exams, average score)
     */
    Map<String, Object> getDashboardStats(Integer studentId);

    /**
     * Get list of exams available to the student
     */
    List<Map<String, Object>> getAvailableExams(Integer studentId, Integer classId);

    /**
     * Get history exam records
     */
    List<Map<String, Object>> getExamRecords(Integer studentId);

    /**
     * Get paper details for an exam
     */
    Map<String, Object> getExamPaper(Integer examId);

    /**
     * Submit an exam
     */
    void submitExam(Integer studentId, Integer examId, List<Map<String, Object>> answers);

    /**
     * Submit an exam with anti-cheat metadata
     */
    void submitExam(Integer studentId, Integer examId, List<Map<String, Object>> answers, boolean abnormalEnd, Integer cheatCount);

    /**
     * Get wrong question book
     */
    List<Map<String, Object>> getWrongQuestions(Integer studentId);

    /**
     * Submit wrong-question challenge result
     */
    Map<String, Object> submitWrongQuestionChallenge(Integer studentId, Integer questionId, boolean correct);
}
