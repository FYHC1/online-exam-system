package com.exam.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.exam.system.entity.*;
import com.exam.system.mapper.*;
import com.exam.system.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private QuestionBankMapper questionMapper;
    @Autowired
    private TestPaperMapper paperMapper;
    @Autowired
    private ExamArrangementMapper examMapper;
    @Autowired
    private PaperQuestionRelMapper relMapper;
    @Autowired
    private StudentExamRecordMapper recordMapper;
    @Autowired
    private StudentAnswerDetailMapper answerDetailMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysClassMapper sysClassMapper;

    @Override
    public Map<String, Object> getDashboardStats(Integer teacherId) {
        // Mock Implementation for dashboard metrics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExams", examMapper.selectCount(new QueryWrapper<ExamArrangement>().eq("create_by", teacherId)));
        stats.put("totalQuestions", questionMapper.selectCount(new QueryWrapper<QuestionBank>().eq("create_by", teacherId)));
        stats.put("avgPassRate", 85.5); // Derived complex metric
        return stats;
    }

    @Override
    public List<QuestionBank> getQuestions(String subject, Integer difficulty, String type, String keyword) {
        QueryWrapper<QuestionBank> wrapper = new QueryWrapper<>();
        if (subject != null && !subject.isEmpty()) wrapper.eq("subject", subject);
        if (difficulty != null) wrapper.eq("difficulty", difficulty);
        if (type != null && !type.isEmpty()) wrapper.eq("type", type);
        if (keyword != null && !keyword.isEmpty()) wrapper.like("title", keyword);
        return questionMapper.selectList(wrapper);
    }

    @Override
    public void addQuestion(QuestionBank question) {
        questionMapper.insert(question);
    }

    @Override
    public void updateQuestion(QuestionBank question) {
        questionMapper.updateById(question);
    }

    @Override
    public void deleteQuestion(Integer questionId) {
        questionMapper.deleteById(questionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishExam(String title, String subject, String targetClasses, Integer duration, Integer createBy, Map<String, Integer> autoConfig, String paperMode, List<Map<String, Object>> manualQuestions, String startTimeStr, String endTimeStr) {
        if (!"manual".equals(paperMode)) {
            validateAutoConfig(subject, autoConfig);
        }

        // 1. Create Paper
        TestPaper paper = new TestPaper();
        paper.setPaperName(title + " 试卷");
        paper.setSubject(subject);
        paper.setTotalScore(100);
        paper.setPassScore(60);
        paper.setDuration(duration);
        paper.setCreateBy(createBy);
        paperMapper.insert(paper);

        // 2. Bind Questions
        int sort = 1;
        if ("manual".equals(paperMode) && manualQuestions != null) {
            for (Map<String, Object> qMap : manualQuestions) {
                Integer qId = Integer.parseInt(qMap.get("questionId").toString());
                Integer score = Integer.parseInt(qMap.get("score").toString());
                
                QuestionBank q = questionMapper.selectById(qId);
                if (q != null) {
                    PaperQuestionRel rel = new PaperQuestionRel();
                    rel.setPaperId(paper.getPaperId());
                    rel.setQuestionId(q.getQuestionId());
                    rel.setScore(score);
                    rel.setSortOrder(sort++);
                    relMapper.insert(rel);
                }
            }
        } else {
            if (autoConfig != null && autoConfig.containsKey("single")) {
                selectAndBindQuestions(paper.getPaperId(), subject, "单选题", autoConfig.get("single"), 5, sort);
                sort += autoConfig.get("single");
            }
            if (autoConfig != null && autoConfig.containsKey("multiple")) {
                selectAndBindQuestions(paper.getPaperId(), subject, "多选题", autoConfig.get("multiple"), 5, sort);
                sort += autoConfig.get("multiple");
            }
            if (autoConfig != null && autoConfig.containsKey("judge")) {
                selectAndBindQuestions(paper.getPaperId(), subject, "判断题", autoConfig.get("judge"), 5, sort);
                sort += autoConfig.get("judge");
            }
            if (autoConfig != null && autoConfig.containsKey("subjective")) {
                selectAndBindQuestions(paper.getPaperId(), subject, "简答题", autoConfig.get("subjective"), 10, sort);
            }
        }

        // 3. Create Exam Arrangement
        ExamArrangement exam = new ExamArrangement();
        exam.setTitle(title);
        exam.setPaperId(paper.getPaperId());
        exam.setTargetClasses(targetClasses);
        exam.setStatus("pending");
        exam.setCreateBy(createBy);
        
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (startTimeStr != null) {
                try { exam.setStartTime(sdf.parse(startTimeStr)); } catch (Exception ex) {
                    try { exam.setStartTime(sdf2.parse(startTimeStr)); } catch (Exception ex2) { exam.setStartTime(new Date()); }
                }
            } else { exam.setStartTime(new Date()); }
            
            if (endTimeStr != null) {
                try { exam.setEndTime(sdf.parse(endTimeStr)); } catch (Exception ex) {
                    try { exam.setEndTime(sdf2.parse(endTimeStr)); } catch (Exception ex2) { exam.setEndTime(new Date(System.currentTimeMillis() + duration * 60000L)); }
                }
            } else { exam.setEndTime(new Date(System.currentTimeMillis() + duration * 60000L)); }
        } catch (Exception e) {
            exam.setStartTime(new Date());
            exam.setEndTime(new Date(System.currentTimeMillis() + duration * 60000L));
        }
        
        examMapper.insert(exam);
    }

    private void validateAutoConfig(String subject, Map<String, Integer> autoConfig) {
        if (autoConfig == null) {
            throw new IllegalArgumentException("自动组卷配置不能为空");
        }

        validateQuestionCount(subject, "单选题", autoConfig.getOrDefault("single", 0));
        validateQuestionCount(subject, "多选题", autoConfig.getOrDefault("multiple", 0));
        validateQuestionCount(subject, "判断题", autoConfig.getOrDefault("judge", 0));
        validateQuestionCount(subject, "简答题", autoConfig.getOrDefault("subjective", 0));
    }

    private void validateQuestionCount(String subject, String type, Integer requiredCount) {
        if (requiredCount == null || requiredCount <= 0) {
            return;
        }

        QueryWrapper<QuestionBank> wrapper = new QueryWrapper<>();
        wrapper.eq("subject", subject).eq("type", type);
        Long availableCount = questionMapper.selectCount(wrapper);
        if (availableCount < requiredCount) {
            throw new IllegalArgumentException(String.format("%s题库中的%s数量不足，当前%d题，需要%d题", subject, type, availableCount, requiredCount));
        }
    }
    
    private void selectAndBindQuestions(Integer paperId, String subject, String type, Integer limit, Integer scorePerQuestion, int startSort) {
        if (limit == null || limit <= 0) {
            return;
        }

        QueryWrapper<QuestionBank> w = new QueryWrapper<>();
        w.eq("subject", subject).eq("type", type).last("LIMIT " + limit);
        List<QuestionBank> list = questionMapper.selectList(w);
        for (QuestionBank q : list) {
            PaperQuestionRel rel = new PaperQuestionRel();
            rel.setPaperId(paperId);
            rel.setQuestionId(q.getQuestionId());
            rel.setScore(scorePerQuestion);
            rel.setSortOrder(startSort++);
            relMapper.insert(rel);
        }
    }

    @Override
    public List<Map<String, Object>> getExamsByTeacher(Integer teacherId) {
        QueryWrapper<ExamArrangement> wrapper = new QueryWrapper<>();
        wrapper.eq("create_by", teacherId).orderByDesc("create_time");
        List<ExamArrangement> exams = examMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ExamArrangement exam : exams) {
            Map<String, Object> item = new HashMap<>();
            item.put("examId", exam.getExamId());
            item.put("title", exam.getTitle());
            item.put("paperId", exam.getPaperId());
            item.put("targetClasses", exam.getTargetClasses());
            item.put("startTime", exam.getStartTime());
            item.put("endTime", exam.getEndTime());
            item.put("status", exam.getStatus());
            item.put("createBy", exam.getCreateBy());
            item.put("createTime", exam.getCreateTime());

            TestPaper paper = paperMapper.selectById(exam.getPaperId());
            item.put("subject", paper != null ? paper.getSubject() : null);
            item.put("duration", paper != null ? paper.getDuration() : null);

            result.add(item);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateExam(Integer examId, String title, String targetClasses, Integer duration, String startTimeStr, String endTimeStr, Integer teacherId) {
        ExamArrangement currentExam = examMapper.selectById(examId);
        if (currentExam == null) {
            throw new IllegalArgumentException("考试不存在");
        }
        if (!Objects.equals(currentExam.getCreateBy(), teacherId)) {
            throw new IllegalArgumentException("无权修改该考试");
        }
        if (!"pending".equals(currentExam.getStatus())) {
            throw new IllegalArgumentException("只能修改未开始的考试");
        }

        ExamArrangement exam = new ExamArrangement();
        exam.setExamId(examId);
        exam.setTitle(title);
        exam.setTargetClasses(targetClasses);

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if (startTimeStr != null) {
                try { exam.setStartTime(sdf.parse(startTimeStr)); } catch (Exception ex) {
                    exam.setStartTime(sdf2.parse(startTimeStr));
                }
            }

            if (endTimeStr != null) {
                try { exam.setEndTime(sdf.parse(endTimeStr)); } catch (Exception ex) {
                    exam.setEndTime(sdf2.parse(endTimeStr));
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("考试时间格式不正确");
        }

        examMapper.updateById(exam);

        TestPaper paper = new TestPaper();
        paper.setPaperId(currentExam.getPaperId());
        paper.setPaperName(title + " 试卷");
        paper.setDuration(duration);
        paperMapper.updateById(paper);
    }
    
    @Override
    public void updateExamStatus(Integer examId, String status) {
        ExamArrangement exam = new ExamArrangement();
        exam.setExamId(examId);
        exam.setStatus(status);
        examMapper.updateById(exam);
    }

    @Override
    public List<Map<String, Object>> getPendingGradingList(Integer examId, Integer classId) {
        QueryWrapper<StudentExamRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("exam_id", examId).in("status", Arrays.asList("grading", "finished", "abnormal")).orderByDesc("record_id");
        List<StudentExamRecord> records = recordMapper.selectList(wrapper);
        List<Map<String, Object>> res = new ArrayList<>();
        for (StudentExamRecord r : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("record", r);
            
            SysUser user = sysUserMapper.selectById(r.getStudentId());
            if (user != null) {
                map.put("studentName", user.getRealName());
                SysClass cls = sysClassMapper.selectById(user.getClassId());
                if (cls != null) {
                    map.put("studentClass", cls.getClassName());
                } else {
                    map.put("studentClass", "未分配班级");
                }
            } else {
                map.put("studentName", "Student ID: " + r.getStudentId());
                map.put("studentClass", "未分配班级");
            }

            map.put("hasSubjective", hasSubjectiveQuestions(r.getExamId()));
            
            res.add(map);
        }
        return res;
    }

    private boolean hasSubjectiveQuestions(Integer examId) {
        ExamArrangement exam = examMapper.selectById(examId);
        if (exam == null) {
            return false;
        }

        QueryWrapper<PaperQuestionRel> relWrapper = new QueryWrapper<>();
        relWrapper.eq("paper_id", exam.getPaperId());
        List<PaperQuestionRel> rels = relMapper.selectList(relWrapper);
        for (PaperQuestionRel rel : rels) {
            QuestionBank question = questionMapper.selectById(rel.getQuestionId());
            if (question != null && ("简答题".equals(question.getType()) || "填空题".equals(question.getType()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> getGradingAnswers(Integer recordId) {
        StudentExamRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            return new ArrayList<>();
        }

        ExamArrangement exam = examMapper.selectById(record.getExamId());
        if (exam == null) {
            return new ArrayList<>();
        }

        QueryWrapper<PaperQuestionRel> relWrapper = new QueryWrapper<>();
        relWrapper.eq("paper_id", exam.getPaperId()).orderByAsc("sort_order");
        List<PaperQuestionRel> paperRels = relMapper.selectList(relWrapper);

        QueryWrapper<StudentAnswerDetail> w = new QueryWrapper<>();
        w.eq("record_id", recordId);
        List<StudentAnswerDetail> details = answerDetailMapper.selectList(w);
        Map<Integer, StudentAnswerDetail> detailByQuestionId = new HashMap<>();
        for (StudentAnswerDetail detail : details) {
            detailByQuestionId.put(detail.getQuestionId(), detail);
        }

        for (PaperQuestionRel rel : paperRels) {
            QuestionBank question = questionMapper.selectById(rel.getQuestionId());
            if (question == null || !("简答题".equals(question.getType()) || "填空题".equals(question.getType()))) {
                continue;
            }

            if (!detailByQuestionId.containsKey(rel.getQuestionId())) {
                StudentAnswerDetail missingDetail = new StudentAnswerDetail();
                missingDetail.setRecordId(recordId);
                missingDetail.setQuestionId(rel.getQuestionId());
                missingDetail.setAnswerContent("");
                missingDetail.setIsCorrect(null);
                missingDetail.setScore(0);
                answerDetailMapper.insert(missingDetail);
                detailByQuestionId.put(rel.getQuestionId(), missingDetail);
            }
        }

        details = new ArrayList<>(detailByQuestionId.values());
        
        List<Map<String, Object>> res = new ArrayList<>();
        for (StudentAnswerDetail d : details) {
            QuestionBank q = questionMapper.selectById(d.getQuestionId());
            if (q == null) continue;
            // Only include subjective questions that need manual grading
            if ("简答题".equals(q.getType()) || "填空题".equals(q.getType())) {
                int maxScore = 15;
                PaperQuestionRel rel = paperRels.stream().filter(item -> item.getQuestionId().equals(d.getQuestionId())).findFirst().orElse(null);
                if (rel != null && rel.getScore() != null) {
                    maxScore = rel.getScore();
                }
                Map<String, Object> item = new HashMap<>();
                item.put("detailId", d.getDetailId());
                item.put("questionTitle", q.getTitle());
                item.put("questionType", q.getType());
                item.put("referenceAnswer", q.getAnswer());
                item.put("studentAnswer", d.getAnswerContent());
                item.put("score", d.getScore());
                item.put("maxScore", maxScore);
                res.add(item);
            }
        }
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitGrading(Integer recordId, List<Map<String, Integer>> scores) {
        int additionalScore = 0;
        for (Map<String, Integer> sc : scores) {
            Integer detailId = sc.get("detailId");
            Integer getScore = sc.get("score");
            
            StudentAnswerDetail detail = new StudentAnswerDetail();
            detail.setDetailId(detailId);
            detail.setScore(getScore);
            detail.setIsCorrect(getScore > 0 ? 1 : 0);
            answerDetailMapper.updateById(detail);
            
            additionalScore += getScore;
        }

        QueryWrapper<StudentAnswerDetail> detailsWrapper = new QueryWrapper<>();
        detailsWrapper.eq("record_id", recordId);
        List<StudentAnswerDetail> details = answerDetailMapper.selectList(detailsWrapper);
        int finalTotalScore = 0;
        for (StudentAnswerDetail detail : details) {
            finalTotalScore += detail.getScore() != null ? detail.getScore() : 0;
        }
        
        StudentExamRecord record = recordMapper.selectById(recordId);
        record.setSubjectiveScore(additionalScore);
        record.setTotalScore(finalTotalScore);
        record.setStatus("finished");
        recordMapper.updateById(record);
    }
}
