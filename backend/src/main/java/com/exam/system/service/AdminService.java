package com.exam.system.service;

import com.exam.system.entity.SysAnnouncement;
import com.exam.system.entity.SysClass;
import com.exam.system.entity.QuestionBank;
import com.exam.system.entity.SysUser;

import java.util.List;
import java.util.Map;

public interface AdminService {

    /**
     * Dashboard stats
     */
    Map<String, Object> getDashboardStats();
    Map<String, Object> getAnalyticsStats();
    List<Map<String, Object>> getAuditLogs(String keyword);
    void recordAuditLog(String actor, String action, String ip, boolean risk);

    /**
     * User Management
     */
    List<SysUser> getUsers(String role, String keyword);
    List<Map<String, Object>> getGlobalQuestions();
    List<String> getSubjectCategories();
    void addSubjectCategory(String subject);
    List<Map<String, Object>> getQuestionBanks();
    void addQuestionBank(Map<String, Object> payload);
    void updateQuestionBank(String id, Map<String, Object> payload);
    void deleteQuestionBank(String id);
    List<Map<String, Object>> getGlobalExams();
    List<Map<String, Object>> getOrgStructure();
    Map<String, Object> getTermSettings();
    void addTerm(Map<String, Object> payload);
    void updateCurrentTerm(Map<String, Object> payload);
    List<Map<String, Object>> getLoginCarousels(boolean enabledOnly);
    void addLoginCarousel(Map<String, Object> payload);
    void updateLoginCarousel(String id, Map<String, Object> payload);
    void deleteLoginCarousel(String id);
    void addOrgNode(Map<String, Object> payload);
    void updateOrgNode(Map<String, Object> payload);
    void deleteOrgNode(Map<String, Object> payload);
    Map<String, Object> getGlobalQuestionDetail(Integer questionId);
    void addGlobalQuestion(QuestionBank question, Integer createBy);
    void deleteGlobalQuestion(Integer questionId);
    Map<String, Object> getGlobalExamDetail(Integer examId);
    void updateGlobalExamStatus(Integer examId, String status);
    void deleteGlobalExam(Integer examId);
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
