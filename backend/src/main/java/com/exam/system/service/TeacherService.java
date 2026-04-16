package com.exam.system.service;

import com.exam.system.entity.ExamArrangement;
import com.exam.system.entity.QuestionBank;
import com.exam.system.entity.TestPaper;

import java.util.List;
import java.util.Map;

public interface TeacherService {

    /**
     * Get Teacher Dashboard Stats
     */
    Map<String, Object> getDashboardStats(Integer teacherId);

    /**
     * Question Bank Management
     */
    List<QuestionBank> getQuestions(String subject, Integer difficulty, String type, String keyword);
    void addQuestion(QuestionBank question);
    void updateQuestion(QuestionBank question);
    void deleteQuestion(Integer questionId);
    
    /**
     * Auto Generate Paper and Arrange Exam
     */
    void publishExam(String title, String subject, String targetClasses, Integer duration, Integer createBy, Map<String, Integer> autoConfig, String paperMode, List<Map<String, Object>> manualQuestions, String startTime, String endTime);

    /**
     * Exam Management
     */
    List<Map<String, Object>> getExamsByTeacher(Integer teacherId);
    void updateExam(Integer examId, String title, String targetClasses, Integer duration, String startTime, String endTime, Integer teacherId);
    void updateExamStatus(Integer examId, String status);
    
    /**
     * Grading System
     */
    List<Map<String, Object>> getPendingGradingList(Integer examId, Integer classId);
    List<Map<String, Object>> getGradingAnswers(Integer recordId);
    void submitGrading(Integer recordId, List<Map<String, Integer>> scores);
}
