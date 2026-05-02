package com.exam.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.exam.system.entity.*;
import com.exam.system.mapper.*;
import com.exam.system.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherServiceImpl implements TeacherService {

    private static final Path PROFILE_META_FILE = Paths.get("data", "user-profile-meta.json");
    private static final Path TERM_SETTINGS_FILE = Paths.get("data", "term-settings.json");

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
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getDashboardStats(Integer teacherId) {
        List<SysClass> managedClasses = getManagedClasses(teacherId);
        Set<Integer> managedClassIds = managedClasses.stream().map(SysClass::getClassId).collect(Collectors.toCollection(LinkedHashSet::new));
        List<SysUser> students = getStudentsInClasses(managedClassIds);
        Set<Integer> studentIds = students.stream().map(SysUser::getUserId).collect(Collectors.toSet());
        List<Map<String, Object>> recentExams = buildExamSummaries(teacherId, managedClasses, studentIds);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", students.size());
        stats.put("totalExams", recentExams.size());
        stats.put("pendingGrading", countPendingGrading(teacherId, studentIds));
        stats.put("totalQuestions", questionMapper.selectCount(new QueryWrapper<QuestionBank>().eq("create_by", teacherId)));
        stats.put("classes", managedClasses.stream().map(this::classToMap).collect(Collectors.toList()));
        stats.put("gradeOptions", managedClasses.stream().map(this::getGradeFromClass).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
        stats.put("subjectOptions", getTeacherSubjects(teacherId));
        stats.put("recentExams", recentExams);
        stats.put("distribution", buildScoreDistribution(recentExams));
        return stats;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> loadProfileMeta() {
        try {
            if (Files.notExists(PROFILE_META_FILE)) {
                return new HashMap<>();
            }
            return objectMapper.readValue(PROFILE_META_FILE.toFile(), new TypeReference<Map<String, Map<String, Object>>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private List<Integer> toIntegerList(Object raw) {
        if (!(raw instanceof List<?> rawList)) {
            return new ArrayList<>();
        }
        List<Integer> result = new ArrayList<>();
        for (Object item : rawList) {
            try {
                result.add(Integer.parseInt(String.valueOf(item)));
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    private List<String> toStringList(Object raw) {
        if (!(raw instanceof List<?> rawList)) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (Object item : rawList) {
            String value = String.valueOf(item).trim();
            if (!value.isEmpty()) {
                result.add(value);
            }
        }
        return result;
    }

    private String getCurrentTerm() {
        try {
            if (Files.notExists(TERM_SETTINGS_FILE)) {
                return "";
            }
            Map<String, Object> settings = objectMapper.readValue(TERM_SETTINGS_FILE.toFile(), new TypeReference<Map<String, Object>>() {});
            return String.valueOf(settings.getOrDefault("currentTerm", "")).trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String withCurrentTermPrefix(String title) {
        String normalizedTitle = title == null ? "" : title.trim();
        String currentTerm = getCurrentTerm();
        if (currentTerm.isEmpty() || normalizedTitle.startsWith(currentTerm)) {
            return normalizedTitle;
        }

        String titleWithoutTerm = normalizedTitle.replaceFirst("^20\\d{2}-20\\d{2}\\s*学年(?:第[一二两]学期|第一学期|第二学期)\\s*", "").trim();
        return currentTerm + " " + titleWithoutTerm;
    }

    private List<SysClass> getManagedClasses(Integer teacherId) {
        Map<String, Object> teacherMeta = loadProfileMeta().getOrDefault(String.valueOf(teacherId), new HashMap<>());
        LinkedHashSet<Integer> classIds = new LinkedHashSet<>(toIntegerList(teacherMeta.get("managedClassIds")));

        if (classIds.isEmpty()) {
            SysUser teacher = sysUserMapper.selectById(teacherId);
            if (teacher != null && teacher.getClassId() != null) {
                SysClass teacherClass = sysClassMapper.selectById(teacher.getClassId());
                if (teacherClass != null && teacherClass.getDepartment() != null) {
                    QueryWrapper<SysClass> wrapper = new QueryWrapper<>();
                    wrapper.eq("department", teacherClass.getDepartment());
                    for (SysClass cls : sysClassMapper.selectList(wrapper)) {
                        classIds.add(cls.getClassId());
                    }
                }
            }
        }

        if (classIds.isEmpty()) {
            QueryWrapper<ExamArrangement> wrapper = new QueryWrapper<>();
            wrapper.eq("create_by", teacherId);
            for (ExamArrangement exam : examMapper.selectList(wrapper)) {
                classIds.addAll(parseTargetClassIds(exam.getTargetClasses()));
            }
        }

        if (classIds.isEmpty()) {
            return sysClassMapper.selectList(new QueryWrapper<SysClass>().orderByAsc("class_id"));
        }

        return classIds.stream()
                .map(sysClassMapper::selectById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<Integer> parseTargetClassIds(String targetClasses) {
        List<Integer> result = new ArrayList<>();
        if (targetClasses == null || targetClasses.isBlank()) {
            return result;
        }
        for (String token : targetClasses.split(",")) {
            String value = token.trim();
            if (value.isEmpty()) continue;
            try {
                result.add(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                QueryWrapper<SysClass> wrapper = new QueryWrapper<>();
                wrapper.eq("class_name", value).last("LIMIT 1");
                SysClass cls = sysClassMapper.selectOne(wrapper);
                if (cls != null) {
                    result.add(cls.getClassId());
                }
            }
        }
        return result;
    }

    private List<SysUser> getStudentsInClasses(Set<Integer> classIds) {
        if (classIds.isEmpty()) {
            return new ArrayList<>();
        }
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("role", "student").in("class_id", classIds).eq("status", 1);
        return sysUserMapper.selectList(wrapper);
    }

    private Long countPendingGrading(Integer teacherId, Set<Integer> studentIds) {
        if (studentIds.isEmpty()) {
            return 0L;
        }
        List<Integer> examIds = examMapper.selectList(new QueryWrapper<ExamArrangement>().eq("create_by", teacherId))
                .stream().map(ExamArrangement::getExamId).collect(Collectors.toList());
        if (examIds.isEmpty()) {
            return 0L;
        }
        QueryWrapper<StudentExamRecord> wrapper = new QueryWrapper<>();
        wrapper.in("exam_id", examIds).in("student_id", studentIds).eq("status", "grading");
        return recordMapper.selectCount(wrapper);
    }

    private List<Map<String, Object>> buildExamSummaries(Integer teacherId, List<SysClass> managedClasses, Set<Integer> studentIds) {
        Set<Integer> managedClassIds = managedClasses.stream().map(SysClass::getClassId).collect(Collectors.toSet());
        QueryWrapper<ExamArrangement> wrapper = new QueryWrapper<>();
        wrapper.eq("create_by", teacherId).orderByDesc("start_time");
        List<Map<String, Object>> result = new ArrayList<>();

        for (ExamArrangement exam : examMapper.selectList(wrapper)) {
            List<Integer> targetClassIds = parseTargetClassIds(exam.getTargetClasses());
            List<SysClass> visibleClasses = targetClassIds.stream()
                    .filter(managedClassIds::contains)
                    .map(sysClassMapper::selectById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (visibleClasses.isEmpty()) {
                continue;
            }

            Set<Integer> targetStudentIds = getStudentsInClasses(targetClassIds.stream().filter(managedClassIds::contains).collect(Collectors.toSet()))
                    .stream().map(SysUser::getUserId).filter(studentIds::contains).collect(Collectors.toSet());
            List<StudentExamRecord> records = recordMapper.selectList(new QueryWrapper<StudentExamRecord>().eq("exam_id", exam.getExamId()));
            List<StudentExamRecord> visibleRecords = records.stream()
                    .filter(record -> targetStudentIds.contains(record.getStudentId()))
                    .collect(Collectors.toList());

            TestPaper paper = paperMapper.selectById(exam.getPaperId());
            int passScore = paper != null && paper.getPassScore() != null ? paper.getPassScore() : 60;
            long finishedCount = visibleRecords.stream().filter(record -> "finished".equals(record.getStatus())).count();
            long passedCount = visibleRecords.stream().filter(record -> "finished".equals(record.getStatus()) && record.getTotalScore() != null && record.getTotalScore() >= passScore).count();
            double avgScore = visibleRecords.stream()
                    .filter(record -> "finished".equals(record.getStatus()) && record.getTotalScore() != null)
                    .mapToInt(StudentExamRecord::getTotalScore)
                    .average()
                    .orElse(0D);
            int[] scoreBuckets = countScoreBuckets(visibleRecords);

            Map<String, Object> item = new HashMap<>();
            item.put("examId", exam.getExamId());
            item.put("title", exam.getTitle());
            item.put("term", resolveTermName(exam.getStartTime(), exam.getTitle()));
            item.put("grade", visibleClasses.stream().map(this::getGradeFromClass).filter(Objects::nonNull).distinct().collect(Collectors.joining("、")));
            item.put("subject", paper != null ? paper.getSubject() : "未分类");
            item.put("class", visibleClasses.stream().map(SysClass::getClassName).collect(Collectors.joining("、")));
            item.put("classIds", visibleClasses.stream().map(SysClass::getClassId).collect(Collectors.toList()));
            item.put("attendRate", targetStudentIds.isEmpty() ? 0 : Math.round(visibleRecords.size() * 100.0 / targetStudentIds.size()));
            item.put("passRate", finishedCount == 0 ? 0 : Math.round(passedCount * 100.0 / finishedCount));
            item.put("avgScore", String.format(Locale.ROOT, "%.1f", avgScore));
            item.put("finishedCount", finishedCount);
            item.put("submittedCount", visibleRecords.size());
            item.put("targetStudentCount", targetStudentIds.size());
            item.put("scoreBuckets", Arrays.stream(scoreBuckets).boxed().collect(Collectors.toList()));
            result.add(item);
        }
        return result;
    }

    private int[] countScoreBuckets(List<StudentExamRecord> records) {
        int[] counts = new int[5];
        for (StudentExamRecord record : records) {
            if (!"finished".equals(record.getStatus())) continue;
            int score = record.getTotalScore() != null ? record.getTotalScore() : 0;
            if (score >= 90) counts[0]++;
            else if (score >= 80) counts[1]++;
            else if (score >= 70) counts[2]++;
            else if (score >= 60) counts[3]++;
            else counts[4]++;
        }
        return counts;
    }

    private String resolveTermName(Date date, String title) {
        if (title != null) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("^(.*?学期)").matcher(title);
            if (matcher.find()) return matcher.group(1);
        }
        if (date == null) return "未划分学期";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int startYear = month >= 9 ? year : year - 1;
        String term = (month >= 9 || month == 1) ? "第一学期" : "第二学期";
        return startYear + "-" + (startYear + 1) + " 学年" + term;
    }

    private Map<String, Object> classToMap(SysClass cls) {
        Map<String, Object> item = new HashMap<>();
        item.put("classId", cls.getClassId());
        item.put("className", cls.getClassName());
        item.put("major", cls.getMajor());
        item.put("department", cls.getDepartment());
        item.put("grade", getGradeFromClass(cls));
        return item;
    }

    private String getGradeFromClass(SysClass cls) {
        if (cls == null || cls.getCreateTime() == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(cls.getCreateTime());
        return calendar.get(Calendar.YEAR) + "级";
    }

    private List<String> getTeacherSubjects(Integer teacherId) {
        List<String> managedSubjects = toStringList(loadProfileMeta().getOrDefault(String.valueOf(teacherId), new HashMap<>()).get("managedSubjects"));
        if (!managedSubjects.isEmpty()) {
            return managedSubjects;
        }

        LinkedHashSet<String> subjects = new LinkedHashSet<>();
        for (QuestionBank question : questionMapper.selectList(new QueryWrapper<QuestionBank>().eq("create_by", teacherId))) {
            if (question.getSubject() != null) subjects.add(question.getSubject());
        }
        for (TestPaper paper : paperMapper.selectList(new QueryWrapper<TestPaper>().eq("create_by", teacherId))) {
            if (paper.getSubject() != null) subjects.add(paper.getSubject());
        }
        return new ArrayList<>(subjects);
    }

    private List<Map<String, Object>> buildScoreDistribution(List<Map<String, Object>> recentExams) {
        int[] counts = new int[5];
        for (Map<String, Object> exam : recentExams) {
            Object buckets = exam.get("scoreBuckets");
            if (buckets instanceof List<?> list) {
                for (int i = 0; i < Math.min(counts.length, list.size()); i++) {
                    counts[i] += Integer.parseInt(String.valueOf(list.get(i)));
                }
            }
        }
        int total = Arrays.stream(counts).sum();
        String[] ranges = {"90-100", "80-89", "70-79", "60-69", "不及格"};
        String[] colors = {"var(--primary-color)", "var(--success-color)", "var(--warning-color)", "#f97316", "var(--danger-color)"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < ranges.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("range", ranges[i]);
            item.put("count", counts[i]);
            item.put("percent", total == 0 ? 0 : Math.round(counts[i] * 1000.0 / total) / 10.0);
            item.put("color", colors[i]);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<QuestionBank> getQuestions(Integer teacherId, String subject, Integer difficulty, String type, String keyword) {
        List<String> managedSubjects = getTeacherSubjects(teacherId);
        if (managedSubjects.isEmpty()) {
            return new ArrayList<>();
        }
        if (subject != null && !subject.isEmpty() && !managedSubjects.contains(subject)) {
            return new ArrayList<>();
        }

        QueryWrapper<QuestionBank> wrapper = new QueryWrapper<>();
        if (subject != null && !subject.isEmpty()) {
            wrapper.eq("subject", subject);
        } else {
            wrapper.in("subject", managedSubjects);
        }
        if (difficulty != null) wrapper.eq("difficulty", difficulty);
        if (type != null && !type.isEmpty()) wrapper.eq("type", type);
        if (keyword != null && !keyword.isEmpty()) wrapper.like("title", keyword);
        return questionMapper.selectList(wrapper);
    }

    @Override
    public void addQuestion(QuestionBank question) {
        if (question.getCreateBy() != null && !getTeacherSubjects(question.getCreateBy()).contains(question.getSubject())) {
            throw new IllegalArgumentException("只能维护本人负责学科的题库");
        }
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
        String examTitle = withCurrentTermPrefix(title);
        if (!getTeacherSubjects(createBy).contains(subject)) {
            throw new IllegalArgumentException("只能发布本人负责学科的考试");
        }
        if (!"manual".equals(paperMode)) {
            validateAutoConfig(subject, autoConfig);
        }

        // 1. Create Paper
        TestPaper paper = new TestPaper();
        paper.setPaperName(examTitle + " 试卷");
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
        exam.setTitle(examTitle);
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
        List<String> managedSubjects = getTeacherSubjects(teacherId);
        if (managedSubjects.isEmpty()) {
            return new ArrayList<>();
        }
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
            if (paper == null || !managedSubjects.contains(paper.getSubject())) {
                continue;
            }
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
