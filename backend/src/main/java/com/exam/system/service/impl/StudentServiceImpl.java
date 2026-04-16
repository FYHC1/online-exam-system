package com.exam.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.exam.system.entity.*;
import com.exam.system.mapper.*;
import com.exam.system.service.StudentService;
import com.exam.system.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class StudentServiceImpl implements StudentService {

    private static final int REQUIRED_CORRECT_STREAK = 2;

    @Autowired
    private StudentExamRecordMapper recordMapper;

    @Autowired
    private ExamArrangementMapper examMapper;
    
    @Autowired
    private TestPaperMapper paperMapper;
    
    @Autowired   
    private StudentAnswerDetailMapper answerDetailMapper;
    
    @Autowired
    private QuestionBankMapper questionMapper;
    
    @Autowired
    private PaperQuestionRelMapper paperQuestionRelMapper;
    
    @Autowired
    private WrongQuestionBookMapper wrongQuestionBookMapper;

    @Autowired
    private RedisUtils redisUtils;

    private String getWrongQuestionStreakKey(Integer studentId, Integer questionId) {
        return String.format("wrong-question:streak:%d:%d", studentId, questionId);
    }

    private int getWrongQuestionStreak(Integer studentId, Integer questionId) {
        String value = redisUtils.get(getWrongQuestionStreakKey(studentId, questionId));
        return value == null ? 0 : Integer.parseInt(value);
    }

    @Override
    public Map<String, Object> getDashboardStats(Integer studentId) {
        QueryWrapper<StudentExamRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId);
        List<StudentExamRecord> records = recordMapper.selectList(wrapper);
        
        int totalExams = records.size();
        double avgScore = records.stream().mapToInt(StudentExamRecord::getTotalScore).average().orElse(0.0);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExams", totalExams);
        stats.put("avgScore", avgScore);
        return stats;
    }

    @Override
    public List<Map<String, Object>> getAvailableExams(Integer studentId, Integer classId) {
        QueryWrapper<ExamArrangement> wrapper = new QueryWrapper<>();
        wrapper.like("target_classes", String.valueOf(classId)).orderByDesc("create_time");
        List<ExamArrangement> list = examMapper.selectList(wrapper);
        
        List<Map<String, Object>> res = new ArrayList<>();
        for (ExamArrangement exam : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("examId", exam.getExamId());
            map.put("title", exam.getTitle());
            map.put("startTime", exam.getStartTime());
            map.put("endTime", exam.getEndTime());
            map.put("status", exam.getStatus());
            
            // Get subject from associated paper
            TestPaper paper = paperMapper.selectById(exam.getPaperId());
            map.put("subject", paper != null ? paper.getSubject() : "通用");
            res.add(map);
        }
        return res;
    }

    @Override
    public List<Map<String, Object>> getExamRecords(Integer studentId) {
        QueryWrapper<StudentExamRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId).orderByDesc("record_id");
        List<StudentExamRecord> records = recordMapper.selectList(wrapper);
        
        List<Map<String, Object>> res = new ArrayList<>();
        for (StudentExamRecord record : records) {
            ExamArrangement exam = examMapper.selectById(record.getExamId());
            TestPaper paper = exam != null ? paperMapper.selectById(exam.getPaperId()) : null;
            Map<String, Object> map = new HashMap<>();
            map.put("record", record);
            map.put("examTitle", exam != null ? exam.getTitle() : "Unknown Exam");
            map.put("passScore", paper != null ? paper.getPassScore() : 60);
            res.add(map);
        }
        return res;
    }

    @Override
    public Map<String, Object> getExamPaper(Integer examId) {
        ExamArrangement exam = examMapper.selectById(examId);
        TestPaper paper = paperMapper.selectById(exam.getPaperId());
        
        QueryWrapper<PaperQuestionRel> w = new QueryWrapper<>();
        w.eq("paper_id", paper.getPaperId()).orderByAsc("sort_order");
        List<PaperQuestionRel> rels = paperQuestionRelMapper.selectList(w);
        
        List<Map<String, Object>> questions = new ArrayList<>();
        for (PaperQuestionRel rel : rels) {
            QuestionBank q = questionMapper.selectById(rel.getQuestionId());
            Map<String, Object> qmap = new HashMap<>();
            qmap.put("id", q.getQuestionId());
            qmap.put("type", q.getType());
            qmap.put("score", rel.getScore());
            qmap.put("title", q.getTitle());
            qmap.put("rawOptions", q.getOptions());
            questions.add(qmap);
        }
        
        Map<String, Object> res = new HashMap<>();
        res.put("exam", exam);
        res.put("paper", paper);
        res.put("questions", questions);
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitExam(Integer studentId, Integer examId, List<Map<String, Object>> answers) {
        submitExam(studentId, examId, answers, false, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitExam(Integer studentId, Integer examId, List<Map<String, Object>> answers, boolean abnormalEnd, Integer cheatCount) {
        ExamArrangement exam = examMapper.selectById(examId);
        TestPaper paper = paperMapper.selectById(exam.getPaperId());

        QueryWrapper<PaperQuestionRel> paperRelWrapper = new QueryWrapper<>();
        paperRelWrapper.eq("paper_id", exam.getPaperId());
        List<PaperQuestionRel> paperQuestionRels = paperQuestionRelMapper.selectList(paperRelWrapper);
        Map<Integer, Integer> scoreByQuestionId = new HashMap<>();
        boolean requiresManualGrading = false;
        for (PaperQuestionRel rel : paperQuestionRels) {
            scoreByQuestionId.put(rel.getQuestionId(), rel.getScore());
            QuestionBank paperQuestion = questionMapper.selectById(rel.getQuestionId());
            if (paperQuestion != null && ("简答题".equals(paperQuestion.getType()) || "填空题".equals(paperQuestion.getType()))) {
                requiresManualGrading = true;
            }
        }

        // 1. Create a record
        StudentExamRecord record = new StudentExamRecord();
        record.setExamId(examId);
        record.setStudentId(studentId);
        record.setStatus(abnormalEnd ? "abnormal" : (requiresManualGrading ? "grading" : "finished"));
        recordMapper.insert(record);
        
        int totalObjectiveScore = 0;
        
        // 2. Parse answers and auto-grade objective questions
        for (Map<String, Object> ans : answers) {
            Integer questionId = Integer.parseInt(ans.get("questionId").toString());
            String text = ans.get("answer").toString();
            
            QuestionBank q = questionMapper.selectById(questionId);
            
            StudentAnswerDetail detail = new StudentAnswerDetail();
            detail.setRecordId(record.getRecordId());
            detail.setQuestionId(questionId);
            detail.setAnswerContent(text);
            
            // Auto grade objective questions
            if ("单选题".equals(q.getType()) || "多选题".equals(q.getType()) || "判断题".equals(q.getType())) {
                if (q.getAnswer().equals(text)) {
                    detail.setIsCorrect(1);
                    int score = scoreByQuestionId.getOrDefault(questionId, 5);
                    detail.setScore(score);
                    totalObjectiveScore += score;
                } else {
                    detail.setIsCorrect(0);
                    detail.setScore(0);
                    // Add to wrong question book
                    saveWrongQuestion(studentId, questionId, examId);
                }
            } else {
                detail.setIsCorrect(null); // Waiting for teacher to grade
                detail.setScore(0);
            }
            answerDetailMapper.insert(detail);
        }
        
        record.setObjectiveScore(totalObjectiveScore);
        record.setSubjectiveScore(0);
        record.setTotalScore(totalObjectiveScore);
        recordMapper.updateById(record);
    }
    
    private void saveWrongQuestion(Integer studentId, Integer questionId, Integer examId) {
        QueryWrapper<WrongQuestionBook> wqb = new QueryWrapper<>();
        wqb.eq("student_id", studentId).eq("question_id", questionId);
        WrongQuestionBook book = wrongQuestionBookMapper.selectOne(wqb);
        if (book == null) {
            book = new WrongQuestionBook();
            book.setStudentId(studentId);
            book.setQuestionId(questionId);
            book.setSourceExamId(examId);
            book.setErrorCount(1);
            book.setLastErrorTime(new Date());
            wrongQuestionBookMapper.insert(book);
        } else {
            book.setErrorCount(book.getErrorCount() + 1);
            book.setLastErrorTime(new Date());
            wrongQuestionBookMapper.updateById(book);
        }

        redisUtils.delete(getWrongQuestionStreakKey(studentId, questionId));
    }
    
    @Override
    public List<Map<String, Object>> getWrongQuestions(Integer studentId) {
        QueryWrapper<WrongQuestionBook> wqb = new QueryWrapper<>();
        wqb.eq("student_id", studentId).orderByDesc("last_error_time");
        List<WrongQuestionBook> books = wrongQuestionBookMapper.selectList(wqb);
        
        List<Map<String, Object>> res = new ArrayList<>();
        for (WrongQuestionBook wq : books) {
            QuestionBank question = questionMapper.selectById(wq.getQuestionId());
            ExamArrangement sourceExam = wq.getSourceExamId() != null ? examMapper.selectById(wq.getSourceExamId()) : null;
            
            Map<String, Object> map = new HashMap<>();
            map.put("id", wq.getId());
            map.put("questionId", wq.getQuestionId());
            map.put("questionTitle", question != null ? question.getTitle() : "题目已删除");
            map.put("questionType", question != null ? question.getType() : "未知");
            map.put("questionDifficulty", question != null ? question.getDifficulty() : 3);
            map.put("questionOptions", question != null ? question.getOptions() : null);
            map.put("referenceAnswer", question != null ? question.getAnswer() : null);
            map.put("sourceExamTitle", sourceExam != null ? sourceExam.getTitle() : "历史考试");
            map.put("errorCount", wq.getErrorCount());
            map.put("correctStreak", getWrongQuestionStreak(studentId, wq.getQuestionId()));
            res.add(map);
        }
        return res;
    }

    @Override
    public Map<String, Object> submitWrongQuestionChallenge(Integer studentId, Integer questionId, boolean correct) {
        QueryWrapper<WrongQuestionBook> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId).eq("question_id", questionId).last("LIMIT 1");
        WrongQuestionBook book = wrongQuestionBookMapper.selectOne(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("removed", false);
        result.put("requiredCorrectStreak", REQUIRED_CORRECT_STREAK);

        if (book == null) {
            result.put("correctStreak", 0);
            return result;
        }

        if (correct) {
            int nextStreak = getWrongQuestionStreak(studentId, questionId) + 1;
            if (nextStreak >= REQUIRED_CORRECT_STREAK) {
                wrongQuestionBookMapper.deleteById(book.getId());
                redisUtils.delete(getWrongQuestionStreakKey(studentId, questionId));
                result.put("removed", true);
                result.put("correctStreak", REQUIRED_CORRECT_STREAK);
                return result;
            }

            redisUtils.set(getWrongQuestionStreakKey(studentId, questionId), String.valueOf(nextStreak), 30, TimeUnit.DAYS);
            result.put("correctStreak", nextStreak);
            return result;
        }

        book.setErrorCount((book.getErrorCount() != null ? book.getErrorCount() : 0) + 1);
        book.setLastErrorTime(new Date());
        wrongQuestionBookMapper.updateById(book);
        redisUtils.delete(getWrongQuestionStreakKey(studentId, questionId));
        result.put("correctStreak", 0);
        return result;
    }
}
