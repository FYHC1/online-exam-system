package com.exam.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.exam.system.entity.*;
import com.exam.system.mapper.*;
import com.exam.system.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Path ORG_STRUCTURE_FILE = Paths.get("data", "org-structure.json");
    private static final Path TERM_SETTINGS_FILE = Paths.get("data", "term-settings.json");
    private static final Path PROFILE_META_FILE = Paths.get("data", "user-profile-meta.json");
    private static final Path QUESTION_BANKS_FILE = Paths.get("data", "question-bank-subjects.json");
    private static final Path SUBJECT_CATEGORIES_FILE = Paths.get("data", "subject-categories.json");
    private static final Path LOGIN_CAROUSEL_FILE = Paths.get("data", "login-carousel.json");

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysClassMapper classMapper;
    @Autowired
    private SysAnnouncementMapper announcementMapper;
    @Autowired
    private ExamArrangementMapper examMapper;
    @Autowired
    private StudentExamRecordMapper studentExamRecordMapper;
    @Autowired
    private QuestionBankMapper questionMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TestPaperMapper testPaperMapper;
    @Autowired
    private PaperQuestionRelMapper paperQuestionRelMapper;
    @Autowired
    private ObjectMapper objectMapper;

    private SysClass resolveClass(String classToken) {
        if (classToken == null || classToken.isBlank()) {
            return null;
        }

        String normalizedToken = classToken.trim();
        List<String> candidateNames = new ArrayList<>();
        candidateNames.add(normalizedToken);
        if (normalizedToken.startsWith("软件") && !normalizedToken.contains("工程")) {
            candidateNames.add(normalizedToken.replaceFirst("软件", "软件工程"));
        }

        try {
            return classMapper.selectById(Integer.parseInt(normalizedToken));
        } catch (NumberFormatException ignored) {
            for (String candidate : candidateNames) {
                QueryWrapper<SysClass> wrapper = new QueryWrapper<>();
                wrapper.eq("class_name", candidate).last("LIMIT 1");
                SysClass exact = classMapper.selectOne(wrapper);
                if (exact != null) {
                    return exact;
                }
            }

            for (String candidate : candidateNames) {
                QueryWrapper<SysClass> fuzzyWrapper = new QueryWrapper<>();
                fuzzyWrapper.like("class_name", candidate).last("LIMIT 1");
                SysClass fuzzy = classMapper.selectOne(fuzzyWrapper);
                if (fuzzy != null) {
                    return fuzzy;
                }
            }

            return null;
        }
    }

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

    private void saveProfileMeta(Map<String, Map<String, Object>> meta) {
        try {
            if (PROFILE_META_FILE.getParent() != null) {
                Files.createDirectories(PROFILE_META_FILE.getParent());
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(PROFILE_META_FILE.toFile(), meta);
        } catch (IOException e) {
            throw new RuntimeException("保存用户扩展信息失败", e);
        }
    }

    private List<String> loadSubjectCategories() {
        try {
            if (Files.notExists(SUBJECT_CATEGORIES_FILE)) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(SUBJECT_CATEGORIES_FILE.toFile(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void saveSubjectCategories(List<String> subjects) {
        try {
            Files.createDirectories(SUBJECT_CATEGORIES_FILE.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(SUBJECT_CATEGORIES_FILE.toFile(), subjects);
        } catch (IOException e) {
            throw new RuntimeException("保存学科分类失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadQuestionBanks() {
        try {
            if (Files.notExists(QUESTION_BANKS_FILE)) {
                return new ArrayList<>();
            }
            List<?> raw = objectMapper.readValue(QUESTION_BANKS_FILE.toFile(), new TypeReference<List<?>>() {});
            List<Map<String, Object>> banks = new ArrayList<>();
            for (Object item : raw) {
                if (item instanceof Map<?, ?> map) {
                    banks.add(new HashMap<>((Map<String, Object>) map));
                    continue;
                }
                String subject = String.valueOf(item).trim();
                if (!subject.isEmpty()) {
                    banks.add(new HashMap<>(Map.of(
                            "id", UUID.randomUUID().toString(),
                            "name", subject + "题库",
                            "subject", subject,
                            "enabled", true
                    )));
                }
            }
            return banks;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void saveQuestionBanks(List<Map<String, Object>> banks) {
        try {
            Files.createDirectories(QUESTION_BANKS_FILE.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(QUESTION_BANKS_FILE.toFile(), banks);
        } catch (IOException e) {
            throw new RuntimeException("保存题库失败", e);
        }
    }

    private List<Map<String, Object>> defaultLoginCarousels() {
        List<Map<String, Object>> defaults = new ArrayList<>();
        defaults.add(new HashMap<>(Map.of(
                "id", UUID.randomUUID().toString(),
                "title", "在线考试，轻松管理",
                "subtitle", "支持题库、考试、成绩与错题全流程管理",
                "imageUrl", "",
                "enabled", true,
                "sort", 1
        )));
        defaults.add(new HashMap<>(Map.of(
                "id", UUID.randomUUID().toString(),
                "title", "诚信考试，从容作答",
                "subtitle", "为教师和学生提供稳定、安全的线上考试体验",
                "imageUrl", "",
                "enabled", true,
                "sort", 2
        )));
        defaults.add(new HashMap<>(Map.of(
                "id", UUID.randomUUID().toString(),
                "title", "数据清晰，管理高效",
                "subtitle", "管理员可统一维护组织架构、学期、公告和考试资源",
                "imageUrl", "",
                "enabled", true,
                "sort", 3
        )));
        return defaults;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadLoginCarousels() {
        try {
            if (Files.notExists(LOGIN_CAROUSEL_FILE)) {
                List<Map<String, Object>> defaults = defaultLoginCarousels();
                saveLoginCarousels(defaults);
                return defaults;
            }
            return objectMapper.readValue(LOGIN_CAROUSEL_FILE.toFile(), new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return defaultLoginCarousels();
        }
    }

    private void saveLoginCarousels(List<Map<String, Object>> items) {
        try {
            Files.createDirectories(LOGIN_CAROUSEL_FILE.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(LOGIN_CAROUSEL_FILE.toFile(), items);
        } catch (IOException e) {
            throw new RuntimeException("保存登录轮播图失败", e);
        }
    }

    private int toInt(Object raw, int fallback) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (Exception e) {
            return fallback;
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

    private void saveUserMeta(SysUser user) {
        if (user.getUserId() == null) return;
        if (!"teacher".equals(user.getRole()) || (user.getManagedClassIds() == null && user.getManagedSubjects() == null)) return;

        Map<String, Map<String, Object>> meta = loadProfileMeta();
        Map<String, Object> userMeta = meta.getOrDefault(String.valueOf(user.getUserId()), new HashMap<>());
        if (user.getManagedClassIds() != null) {
            userMeta.put("managedClassIds", user.getManagedClassIds());
        }
        if (user.getManagedSubjects() != null) {
            userMeta.put("managedSubjects", user.getManagedSubjects());
        }
        meta.put(String.valueOf(user.getUserId()), userMeta);
        saveProfileMeta(meta);
    }

    private List<Map<String, Object>> defaultOrgStructure() {
        List<Map<String, Object>> departments = new ArrayList<>();

        Map<String, Object> csDept = new HashMap<>();
        csDept.put("name", "计算机与信息学院");
        csDept.put("archived", false);
        List<Map<String, Object>> csMajors = new ArrayList<>();
        Map<String, Object> softwareMajor = new HashMap<>();
        softwareMajor.put("name", "软件工程");
        List<Map<String, Object>> softwareClasses = new ArrayList<>();
        softwareClasses.add(new HashMap<>(Map.of("name", "软件工程 1 班", "count", 45)));
        softwareClasses.add(new HashMap<>(Map.of("name", "软件工程 2 班", "count", 47)));
        softwareMajor.put("classes", softwareClasses);
        csMajors.add(softwareMajor);
        csDept.put("majors", csMajors);
        departments.add(csDept);

        Map<String, Object> scienceDept = new HashMap<>();
        scienceDept.put("name", "理学院");
        scienceDept.put("archived", false);
        List<Map<String, Object>> scienceMajors = new ArrayList<>();
        Map<String, Object> mathMajor = new HashMap<>();
        mathMajor.put("name", "数学与应用数学");
        List<Map<String, Object>> mathClasses = new ArrayList<>();
        mathClasses.add(new HashMap<>(Map.of("name", "数学 1 班", "count", 39)));
        mathMajor.put("classes", mathClasses);
        scienceMajors.add(mathMajor);
        scienceDept.put("majors", scienceMajors);
        departments.add(scienceDept);

        Map<String, Object> archivedDept = new HashMap<>();
        archivedDept.put("name", "历史归档院系");
        archivedDept.put("archived", true);
        archivedDept.put("majors", new ArrayList<>());
        departments.add(archivedDept);

        return departments;
    }

    private Map<String, Object> defaultTermSettings() {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> terms = new ArrayList<>();
        for (int year = 2020; year <= 2027; year++) {
            terms.add(new HashMap<>(Map.of("name", year + "-" + (year + 1) + " 学年第一学期", "range", new ArrayList<>())));
            terms.add(new HashMap<>(Map.of("name", year + "-" + (year + 1) + " 学年第二学期", "range", new ArrayList<>())));
        }
        data.put("terms", terms);
        data.put("currentTerm", "2023-2024 学年第一学期");
        data.put("currentRange", new ArrayList<>());
        return data;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadOrgStructure() {
        try {
            if (Files.notExists(ORG_STRUCTURE_FILE)) {
                Files.createDirectories(ORG_STRUCTURE_FILE.getParent());
                List<Map<String, Object>> defaults = defaultOrgStructure();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(ORG_STRUCTURE_FILE.toFile(), defaults);
                return defaults;
            }
            return objectMapper.readValue(ORG_STRUCTURE_FILE.toFile(), new TypeReference<List<Map<String, Object>>>() {});
        } catch (IOException e) {
            throw new RuntimeException("读取组织架构失败", e);
        }
    }

    private void saveOrgStructure(List<Map<String, Object>> structure) {
        try {
            Files.createDirectories(ORG_STRUCTURE_FILE.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(ORG_STRUCTURE_FILE.toFile(), structure);
        } catch (IOException e) {
            throw new RuntimeException("保存组织架构失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadTermSettings() {
        try {
            if (Files.notExists(TERM_SETTINGS_FILE)) {
                Files.createDirectories(TERM_SETTINGS_FILE.getParent());
                Map<String, Object> defaults = defaultTermSettings();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(TERM_SETTINGS_FILE.toFile(), defaults);
                return defaults;
            }
            return objectMapper.readValue(TERM_SETTINGS_FILE.toFile(), new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new RuntimeException("读取学期设置失败", e);
        }
    }

    private void saveTermSettings(Map<String, Object> settings) {
        try {
            Files.createDirectories(TERM_SETTINGS_FILE.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(TERM_SETTINGS_FILE.toFile(), settings);
        } catch (IOException e) {
            throw new RuntimeException("保存学期设置失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> findDepartment(List<Map<String, Object>> structure, String departmentName) {
        return structure.stream().filter(item -> departmentName.equals(item.get("name"))).findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> findMajor(Map<String, Object> department, String majorName) {
        if (department == null) return null;
        List<Map<String, Object>> majors = (List<Map<String, Object>>) department.get("majors");
        return majors.stream().filter(item -> majorName.equals(item.get("name"))).findFirst().orElse(null);
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        Long totalUsers = userMapper.selectCount(null);
        Long studentCount = userMapper.selectCount(new QueryWrapper<SysUser>().eq("role", "student"));
        Long teacherCount = userMapper.selectCount(new QueryWrapper<SysUser>().eq("role", "teacher"));
        Long adminCount = userMapper.selectCount(new QueryWrapper<SysUser>().eq("role", "admin"));
        Date now = new Date();

        QueryWrapper<ExamArrangement> activeExamWrapper = new QueryWrapper<>();
        activeExamWrapper.and(wrapper -> wrapper.eq("status", "running")
                .or(inner -> inner.le("start_time", now).ge("end_time", now).notIn("status", Arrays.asList("cancelled", "finished", "abnormal"))));

        stats.put("totalUsers", totalUsers);
        stats.put("totalExams", examMapper.selectCount(null));
        stats.put("activeExams", examMapper.selectCount(activeExamWrapper));
        stats.put("totalQuestions", questionMapper.selectCount(null));
        stats.put("totalRecords", studentExamRecordMapper.selectCount(null));
        stats.put("roleDistribution", Arrays.asList(
                Map.of("role", "学生", "count", studentCount, "percent", totalUsers == 0 ? 0 : Math.round(studentCount * 1000.0 / totalUsers) / 10.0),
                Map.of("role", "教师", "count", teacherCount, "percent", totalUsers == 0 ? 0 : Math.round(teacherCount * 1000.0 / totalUsers) / 10.0),
                Map.of("role", "管理员", "count", adminCount, "percent", totalUsers == 0 ? 0 : Math.round(adminCount * 1000.0 / totalUsers) / 10.0)
        ));
        return stats;
    }

    @Override
    public Map<String, Object> getAnalyticsStats() {
        Map<String, Object> result = new HashMap<>();
        List<StudentExamRecord> records = studentExamRecordMapper.selectList(null);
        List<ExamArrangement> exams = examMapper.selectList(null);
        List<SysClass> classes = classMapper.selectList(null);
        Map<Integer, ExamArrangement> examMap = new HashMap<>();
        Map<Integer, SysClass> classMap = new HashMap<>();
        Map<Integer, TestPaper> paperMap = new HashMap<>();

        for (ExamArrangement exam : exams) {
            examMap.put(exam.getExamId(), exam);
            if (exam.getPaperId() != null && !paperMap.containsKey(exam.getPaperId())) {
                TestPaper paper = testPaperMapper.selectById(exam.getPaperId());
                if (paper != null) {
                    paperMap.put(exam.getPaperId(), paper);
                }
            }
        }
        for (SysClass sysClass : classes) {
            classMap.put(sysClass.getClassId(), sysClass);
        }

        long finishedRecords = records.stream().filter(item -> "finished".equals(item.getStatus())).count();
        long gradingRecords = records.stream().filter(item -> "grading".equals(item.getStatus()) || "pending".equals(item.getStatus())).count();
        long abnormalRecords = records.stream().filter(item -> "abnormal".equals(item.getStatus())).count();
        int scoreSum = 0;
        int scoredCount = 0;
        int passCount = 0;
        for (StudentExamRecord record : records) {
            if (record.getTotalScore() != null) {
                scoreSum += record.getTotalScore();
                scoredCount++;
                if (record.getTotalScore() >= 60) {
                    passCount++;
                }
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("studentCount", userMapper.selectCount(new QueryWrapper<SysUser>().eq("role", "student")));
        summary.put("teacherCount", userMapper.selectCount(new QueryWrapper<SysUser>().eq("role", "teacher")));
        summary.put("examCount", exams.size());
        summary.put("questionCount", questionMapper.selectCount(null));
        summary.put("submittedCount", records.size());
        summary.put("finishedCount", finishedRecords);
        summary.put("gradingCount", gradingRecords);
        summary.put("abnormalCount", abnormalRecords);
        summary.put("avgScore", scoredCount == 0 ? 0 : Math.round(scoreSum * 10.0 / scoredCount) / 10.0);
        summary.put("passRate", scoredCount == 0 ? 0 : Math.round(passCount * 1000.0 / scoredCount) / 10.0);
        result.put("summary", summary);

        Map<String, Map<String, Object>> subjectStats = new LinkedHashMap<>();
        for (ExamArrangement exam : exams) {
            TestPaper paper = exam.getPaperId() == null ? null : paperMap.get(exam.getPaperId());
            String subject = paper == null || paper.getSubject() == null || paper.getSubject().isBlank() ? "未分类" : paper.getSubject();
            Map<String, Object> stat = subjectStats.computeIfAbsent(subject, key -> new HashMap<>(Map.of(
                    "subject", key,
                    "examCount", 0,
                    "recordCount", 0,
                    "scoreSum", 0,
                    "scoredCount", 0,
                    "passCount", 0
            )));
            stat.put("examCount", (Integer) stat.get("examCount") + 1);
        }
        for (StudentExamRecord record : records) {
            ExamArrangement exam = examMap.get(record.getExamId());
            TestPaper paper = exam == null || exam.getPaperId() == null ? null : paperMap.get(exam.getPaperId());
            String subject = paper == null || paper.getSubject() == null || paper.getSubject().isBlank() ? "未分类" : paper.getSubject();
            Map<String, Object> stat = subjectStats.computeIfAbsent(subject, key -> new HashMap<>(Map.of(
                    "subject", key,
                    "examCount", 0,
                    "recordCount", 0,
                    "scoreSum", 0,
                    "scoredCount", 0,
                    "passCount", 0
            )));
            stat.put("recordCount", (Integer) stat.get("recordCount") + 1);
            if (record.getTotalScore() != null) {
                stat.put("scoreSum", (Integer) stat.get("scoreSum") + record.getTotalScore());
                stat.put("scoredCount", (Integer) stat.get("scoredCount") + 1);
                if (record.getTotalScore() >= 60) {
                    stat.put("passCount", (Integer) stat.get("passCount") + 1);
                }
            }
        }
        List<Map<String, Object>> subjectList = new ArrayList<>();
        for (Map<String, Object> stat : subjectStats.values()) {
            int subjectScoredCount = (Integer) stat.get("scoredCount");
            int subjectScoreSum = (Integer) stat.get("scoreSum");
            int subjectPassCount = (Integer) stat.get("passCount");
            stat.put("avgScore", subjectScoredCount == 0 ? 0 : Math.round(subjectScoreSum * 10.0 / subjectScoredCount) / 10.0);
            stat.put("passRate", subjectScoredCount == 0 ? 0 : Math.round(subjectPassCount * 1000.0 / subjectScoredCount) / 10.0);
            stat.remove("scoreSum");
            stat.remove("scoredCount");
            stat.remove("passCount");
            subjectList.add(stat);
        }
        subjectList.sort((a, b) -> Double.compare(Double.parseDouble(String.valueOf(b.get("passRate"))), Double.parseDouble(String.valueOf(a.get("passRate")))));
        result.put("subjects", subjectList);

        Map<String, Map<String, Object>> departmentStats = new LinkedHashMap<>();
        for (ExamArrangement exam : exams) {
            Set<String> departments = new LinkedHashSet<>();
            if (exam.getTargetClasses() != null) {
                for (String token : exam.getTargetClasses().split(",")) {
                    try {
                        SysClass sysClass = classMap.get(Integer.parseInt(token.trim()));
                        if (sysClass != null && sysClass.getDepartment() != null) {
                            departments.add(sysClass.getDepartment());
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            if (departments.isEmpty()) {
                departments.add("未指定学院");
            }
            for (String department : departments) {
                Map<String, Object> stat = departmentStats.computeIfAbsent(department, key -> new HashMap<>(Map.of(
                        "department", key,
                        "examCount", 0,
                        "recordCount", 0
                )));
                stat.put("examCount", (Integer) stat.get("examCount") + 1);
            }
        }
        for (StudentExamRecord record : records) {
            ExamArrangement exam = examMap.get(record.getExamId());
            if (exam == null || exam.getTargetClasses() == null) continue;
            Set<String> departments = new LinkedHashSet<>();
            for (String token : exam.getTargetClasses().split(",")) {
                try {
                    SysClass sysClass = classMap.get(Integer.parseInt(token.trim()));
                    if (sysClass != null && sysClass.getDepartment() != null) {
                        departments.add(sysClass.getDepartment());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            for (String department : departments) {
                Map<String, Object> stat = departmentStats.get(department);
                if (stat != null) {
                    stat.put("recordCount", (Integer) stat.get("recordCount") + 1);
                }
            }
        }
        List<Map<String, Object>> departmentList = new ArrayList<>(departmentStats.values());
        departmentList.sort((a, b) -> Integer.compare((Integer) b.get("examCount"), (Integer) a.get("examCount")));
        result.put("departments", departmentList);

        List<Map<String, Object>> attentionList = new ArrayList<>();
        for (StudentExamRecord record : records) {
            boolean abnormal = "abnormal".equals(record.getStatus());
            boolean lowScore = record.getTotalScore() != null && record.getTotalScore() < 60;
            if (!abnormal && !lowScore) continue;
            ExamArrangement exam = examMap.get(record.getExamId());
            SysUser student = record.getStudentId() == null ? null : userMapper.selectById(record.getStudentId());
            Map<String, Object> item = new HashMap<>();
            item.put("recordId", record.getRecordId());
            item.put("exam", exam == null ? "未知考试" : exam.getTitle());
            item.put("student", student == null ? "未知学生" : student.getRealName());
            item.put("score", record.getTotalScore());
            item.put("status", abnormal ? "异常结束" : "低于及格线");
            item.put("reason", abnormal ? "考试过程异常，需要人工复核" : "成绩低于60分，可关注教学或补考安排");
            attentionList.add(item);
        }
        result.put("attentionRecords", attentionList.stream().limit(20).toList());
        return result;
    }

    @Override
    public List<SysUser> getUsers(String role, String keyword) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        if (role != null && !role.isEmpty()) wrapper.eq("role", role);
        if (keyword != null && !keyword.isEmpty()) wrapper.like("real_name", keyword);
        List<SysUser> users = userMapper.selectList(wrapper);
        Map<String, Map<String, Object>> meta = loadProfileMeta();
        for (SysUser user : users) {
            Map<String, Object> userMeta = meta.getOrDefault(String.valueOf(user.getUserId()), new HashMap<>());
            user.setManagedClassIds(toIntegerList(userMeta.get("managedClassIds")));
            user.setManagedSubjects(toStringList(userMeta.get("managedSubjects")));
        }
        return users;
    }

    @Override
    public List<Map<String, Object>> getGlobalQuestions() {
        List<QuestionBank> questions = questionMapper.selectList(new QueryWrapper<QuestionBank>().orderByDesc("question_id"));
        Map<Integer, String> userNames = new HashMap<>();
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (QuestionBank question : questions) {
            Map<String, Object> item = new HashMap<>();
            item.put("qcId", "Q-" + question.getQuestionId());
            item.put("questionId", question.getQuestionId());
            item.put("content", question.getTitle());
            item.put("type", question.getType());
            item.put("subject", question.getSubject());

            String creator = "未知教师";
            if (question.getCreateBy() != null) {
                creator = userNames.computeIfAbsent(question.getCreateBy(), userId -> {
                    SysUser user = userMapper.selectById(userId);
                    return user != null ? user.getRealName() : "未知教师";
                });
            }
            item.put("creator", creator);

            String department = "未指定院系";
            if (question.getCreateBy() != null) {
                QueryWrapper<TestPaper> paperWrapper = new QueryWrapper<>();
                paperWrapper.eq("subject", question.getSubject()).eq("create_by", question.getCreateBy()).last("LIMIT 1");
                TestPaper relatedPaper = testPaperMapper.selectOne(paperWrapper);
                if (relatedPaper != null) {
                    QueryWrapper<ExamArrangement> examWrapper = new QueryWrapper<>();
                    examWrapper.eq("paper_id", relatedPaper.getPaperId()).last("LIMIT 1");
                    ExamArrangement relatedExam = examMapper.selectOne(examWrapper);
                    if (relatedExam != null && relatedExam.getTargetClasses() != null) {
                        String firstClassId = relatedExam.getTargetClasses().split(",")[0].trim();
                        SysClass sysClass = resolveClass(firstClassId);
                        if (sysClass != null) {
                            department = sysClass.getDepartment();
                        }
                    }
                }
            }
            item.put("department", department);
            result.add(item);
        }

        return result;
    }

    @Override
    public List<String> getSubjectCategories() {
        LinkedHashSet<String> subjects = new LinkedHashSet<>();
        subjects.addAll(loadSubjectCategories());
        for (Map<String, Object> bank : loadQuestionBanks()) {
            String subject = String.valueOf(bank.getOrDefault("subject", "")).trim();
            if (!subject.isEmpty()) {
                subjects.add(subject);
            }
        }

        QueryWrapper<QuestionBank> wrapper = new QueryWrapper<>();
        wrapper.select("subject").isNotNull("subject");
        for (QuestionBank question : questionMapper.selectList(wrapper)) {
            if (question.getSubject() != null && !question.getSubject().isBlank()) {
                subjects.add(question.getSubject().trim());
            }
        }

        return new ArrayList<>(subjects);
    }

    @Override
    public void addSubjectCategory(String subject) {
        String normalized = subject == null ? "" : subject.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("学科名称不能为空");
        }

        List<String> subjects = getSubjectCategories();
        if (!subjects.contains(normalized)) {
            subjects.add(normalized);
            saveSubjectCategories(subjects);
        }
    }

    @Override
    public List<Map<String, Object>> getQuestionBanks() {
        List<Map<String, Object>> banks = loadQuestionBanks();
        for (String subject : getSubjectCategories()) {
            boolean exists = banks.stream().anyMatch(bank -> subject.equals(bank.get("subject")));
            if (!exists) {
                banks.add(new HashMap<>(Map.of(
                        "id", UUID.randomUUID().toString(),
                        "name", subject + "题库",
                        "subject", subject,
                        "enabled", true
                )));
            }
        }
        saveQuestionBanks(banks);
        return banks;
    }

    @Override
    public void addQuestionBank(Map<String, Object> payload) {
        String name = String.valueOf(payload.getOrDefault("name", "")).trim();
        String subject = String.valueOf(payload.getOrDefault("subject", "")).trim();
        if (name.isEmpty() || subject.isEmpty()) {
            throw new IllegalArgumentException("题库名称和所属学科不能为空");
        }

        addSubjectCategory(subject);
        List<Map<String, Object>> banks = loadQuestionBanks();
        banks.add(new HashMap<>(Map.of(
                "id", UUID.randomUUID().toString(),
                "name", name,
                "subject", subject,
                "enabled", Boolean.parseBoolean(String.valueOf(payload.getOrDefault("enabled", true)))
        )));
        saveQuestionBanks(banks);
    }

    @Override
    public void updateQuestionBank(String id, Map<String, Object> payload) {
        List<Map<String, Object>> banks = loadQuestionBanks();
        Map<String, Object> bank = banks.stream().filter(item -> id.equals(String.valueOf(item.get("id")))).findFirst().orElse(null);
        if (bank == null) {
            throw new IllegalArgumentException("题库不存在");
        }
        if (payload.containsKey("name")) {
            String name = String.valueOf(payload.get("name")).trim();
            if (!name.isEmpty()) {
                bank.put("name", name);
            }
        }
        if (payload.containsKey("subject")) {
            String subject = String.valueOf(payload.get("subject")).trim();
            if (!subject.isEmpty()) {
                addSubjectCategory(subject);
                bank.put("subject", subject);
            }
        }
        if (payload.containsKey("enabled")) {
            bank.put("enabled", Boolean.parseBoolean(String.valueOf(payload.get("enabled"))));
        }
        saveQuestionBanks(banks);
    }

    @Override
    public void deleteQuestionBank(String id) {
        List<Map<String, Object>> banks = loadQuestionBanks();
        boolean removed = banks.removeIf(item -> id.equals(String.valueOf(item.get("id"))));
        if (!removed) {
            throw new IllegalArgumentException("题库不存在");
        }
        saveQuestionBanks(banks);
    }

    @Override
    public List<Map<String, Object>> getGlobalExams() {
        List<ExamArrangement> exams = examMapper.selectList(new QueryWrapper<ExamArrangement>().orderByDesc("exam_id"));
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (ExamArrangement exam : exams) {
            Map<String, Object> item = new HashMap<>();
            item.put("roomId", "EXM-" + exam.getExamId());
            item.put("examId", exam.getExamId());
            item.put("title", exam.getTitle());

            String college = "未指定院系";
            if (exam.getTargetClasses() != null && !exam.getTargetClasses().isBlank()) {
                String firstClassId = exam.getTargetClasses().split(",")[0].trim();
                SysClass sysClass = resolveClass(firstClassId);
                if (sysClass != null) {
                    college = sysClass.getDepartment();
                }
            }
            item.put("college", college);

            String status = "未开始";
            java.util.Date now = new java.util.Date();
            if (Arrays.asList("cancelled", "finished", "grading", "abnormal").contains(exam.getStatus())) {
                status = "已结束";
            } else if (exam.getEndTime() != null && !now.before(exam.getEndTime())) {
                status = "已结束";
            } else if ("pending".equals(exam.getStatus()) && exam.getStartTime() != null && now.before(exam.getStartTime())) {
                status = "未开始";
            } else if (exam.getStartTime() != null && !now.before(exam.getStartTime())) {
                status = "进行中";
            }
            item.put("status", status);

            QueryWrapper<PaperQuestionRel> relWrapper = new QueryWrapper<>();
            relWrapper.eq("paper_id", exam.getPaperId());
            item.put("questionCount", paperQuestionRelMapper.selectCount(relWrapper));
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getOrgStructure() {
        return loadOrgStructure();
    }

    @Override
    public Map<String, Object> getTermSettings() {
        return loadTermSettings();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addTerm(Map<String, Object> payload) {
        Map<String, Object> settings = loadTermSettings();
        List<Map<String, Object>> terms = (List<Map<String, Object>>) settings.get("terms");
        String startYear = payload.get("startYear").toString();
        String semester = payload.get("semester").toString();
        String termName = startYear + "-" + (Integer.parseInt(startYear) + 1) + " 学年" + semester;

        boolean exists = terms.stream().anyMatch(item -> termName.equals(item.get("name")));
        if (exists) {
            throw new IllegalArgumentException("该学期已存在");
        }

        Map<String, Object> term = new HashMap<>();
        term.put("name", termName);
        term.put("range", payload.getOrDefault("range", new ArrayList<>()));
        terms.add(term);
        settings.put("currentTerm", termName);
        settings.put("currentRange", payload.getOrDefault("range", new ArrayList<>()));
        saveTermSettings(settings);
    }

    @Override
    public void updateCurrentTerm(Map<String, Object> payload) {
        Map<String, Object> settings = loadTermSettings();
        settings.put("currentTerm", payload.get("name"));
        settings.put("currentRange", payload.getOrDefault("range", new ArrayList<>()));
        saveTermSettings(settings);
    }

    @Override
    public List<Map<String, Object>> getLoginCarousels(boolean enabledOnly) {
        List<Map<String, Object>> items = loadLoginCarousels();
        return items.stream()
                .filter(item -> !enabledOnly || Boolean.parseBoolean(String.valueOf(item.getOrDefault("enabled", true))))
                .sorted(Comparator.comparingInt(item -> toInt(item.get("sort"), 0)))
                .collect(Collectors.toList());
    }

    @Override
    public void addLoginCarousel(Map<String, Object> payload) {
        String title = String.valueOf(payload.getOrDefault("title", "")).trim();
        if (title.isEmpty()) {
            throw new IllegalArgumentException("轮播标题不能为空");
        }
        List<Map<String, Object>> items = loadLoginCarousels();
        Map<String, Object> item = new HashMap<>();
        item.put("id", UUID.randomUUID().toString());
        item.put("title", title);
        item.put("subtitle", String.valueOf(payload.getOrDefault("subtitle", "")).trim());
        item.put("imageUrl", String.valueOf(payload.getOrDefault("imageUrl", "")).trim());
        item.put("enabled", Boolean.parseBoolean(String.valueOf(payload.getOrDefault("enabled", true))));
        item.put("sort", toInt(payload.get("sort"), items.size() + 1));
        items.add(item);
        saveLoginCarousels(items);
    }

    @Override
    public void updateLoginCarousel(String id, Map<String, Object> payload) {
        List<Map<String, Object>> items = loadLoginCarousels();
        Map<String, Object> item = items.stream().filter(row -> id.equals(String.valueOf(row.get("id")))).findFirst().orElse(null);
        if (item == null) throw new IllegalArgumentException("轮播图不存在");
        if (payload.containsKey("title")) item.put("title", String.valueOf(payload.get("title")).trim());
        if (payload.containsKey("subtitle")) item.put("subtitle", String.valueOf(payload.get("subtitle")).trim());
        if (payload.containsKey("imageUrl")) item.put("imageUrl", String.valueOf(payload.get("imageUrl")).trim());
        if (payload.containsKey("enabled")) item.put("enabled", Boolean.parseBoolean(String.valueOf(payload.get("enabled"))));
        if (payload.containsKey("sort")) item.put("sort", toInt(payload.get("sort"), 0));
        saveLoginCarousels(items);
    }

    @Override
    public void deleteLoginCarousel(String id) {
        List<Map<String, Object>> items = loadLoginCarousels();
        boolean removed = items.removeIf(row -> id.equals(String.valueOf(row.get("id"))));
        if (!removed) throw new IllegalArgumentException("轮播图不存在");
        saveLoginCarousels(items);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addOrgNode(Map<String, Object> payload) {
        List<Map<String, Object>> structure = loadOrgStructure();
        String type = payload.get("type").toString();
        String name = payload.get("name").toString();

        if ("院系".equals(type)) {
            Map<String, Object> department = new HashMap<>();
            department.put("name", name);
            department.put("archived", !(Boolean) payload.getOrDefault("enabled", true));
            department.put("majors", new ArrayList<>());
            structure.add(department);
            saveOrgStructure(structure);
            return;
        }

        String parent = payload.get("parent").toString();
        if ("专业".equals(type)) {
            Map<String, Object> department = findDepartment(structure, parent);
            if (department == null) throw new IllegalArgumentException("所属院系不存在");
            List<Map<String, Object>> majors = (List<Map<String, Object>>) department.get("majors");
            Map<String, Object> major = new HashMap<>();
            major.put("name", name);
            major.put("classes", new ArrayList<>());
            majors.add(major);
            saveOrgStructure(structure);
            return;
        }

        String parentDepartment = payload.get("parentDepartment") == null ? null : payload.get("parentDepartment").toString();
        for (Map<String, Object> department : structure) {
            if (parentDepartment != null && !parentDepartment.isBlank() && !parentDepartment.equals(department.get("name"))) {
                continue;
            }
            Map<String, Object> major = findMajor(department, parent);
            if (major != null) {
                List<Map<String, Object>> classes = (List<Map<String, Object>>) major.get("classes");
                Map<String, Object> classItem = new HashMap<>();
                classItem.put("name", name);
                classItem.put("count", Integer.parseInt(payload.getOrDefault("count", 0).toString()));
                classes.add(classItem);
                saveOrgStructure(structure);
                return;
            }
        }

        throw new IllegalArgumentException("所属专业不存在");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateOrgNode(Map<String, Object> payload) {
        List<Map<String, Object>> structure = loadOrgStructure();
        String type = payload.get("type").toString();
        String currentDepartment = payload.get("currentDepartment") != null ? payload.get("currentDepartment").toString() : null;
        String currentMajor = payload.get("currentMajor") != null ? payload.get("currentMajor").toString() : null;
        String currentClass = payload.get("currentClass") != null ? payload.get("currentClass").toString() : null;

        if ("院系".equals(type)) {
            Map<String, Object> department = findDepartment(structure, currentDepartment);
            if (department == null) throw new IllegalArgumentException("院系不存在");
            department.put("name", payload.get("name"));
            department.put("archived", !(Boolean) payload.getOrDefault("enabled", true));
            saveOrgStructure(structure);
            return;
        }

        Map<String, Object> department = findDepartment(structure, currentDepartment);
        if (department == null) throw new IllegalArgumentException("所属院系不存在");

        if ("专业".equals(type)) {
            Map<String, Object> major = findMajor(department, currentMajor);
            if (major == null) throw new IllegalArgumentException("专业不存在");
            major.put("name", payload.get("name"));
            saveOrgStructure(structure);
            return;
        }

        Map<String, Object> major = findMajor(department, currentMajor);
        if (major == null) throw new IllegalArgumentException("专业不存在");
        List<Map<String, Object>> classes = (List<Map<String, Object>>) major.get("classes");
        Map<String, Object> classItem = classes.stream().filter(item -> currentClass.equals(item.get("name"))).findFirst().orElse(null);
        if (classItem == null) throw new IllegalArgumentException("班级不存在");
        classItem.put("name", payload.get("name"));
        saveOrgStructure(structure);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deleteOrgNode(Map<String, Object> payload) {
        List<Map<String, Object>> structure = loadOrgStructure();
        String type = payload.get("type").toString();
        String currentDepartment = payload.get("currentDepartment") != null ? payload.get("currentDepartment").toString() : null;
        String currentMajor = payload.get("currentMajor") != null ? payload.get("currentMajor").toString() : null;
        String currentClass = payload.get("currentClass") != null ? payload.get("currentClass").toString() : null;

        if ("院系".equals(type)) {
            boolean removed = structure.removeIf(item -> currentDepartment.equals(item.get("name")));
            if (!removed) throw new IllegalArgumentException("学院不存在");
            saveOrgStructure(structure);
            return;
        }

        Map<String, Object> department = findDepartment(structure, currentDepartment);
        if (department == null) throw new IllegalArgumentException("所属学院不存在");
        List<Map<String, Object>> majors = (List<Map<String, Object>>) department.get("majors");

        if ("专业".equals(type)) {
            boolean removed = majors.removeIf(item -> currentMajor.equals(item.get("name")));
            if (!removed) throw new IllegalArgumentException("专业不存在");
            saveOrgStructure(structure);
            return;
        }

        Map<String, Object> major = findMajor(department, currentMajor);
        if (major == null) throw new IllegalArgumentException("专业不存在");
        List<Map<String, Object>> classes = (List<Map<String, Object>>) major.get("classes");
        boolean removed = classes.removeIf(item -> currentClass.equals(item.get("name")));
        if (!removed) throw new IllegalArgumentException("班级不存在");
        saveOrgStructure(structure);
    }

    @Override
    public Map<String, Object> getGlobalQuestionDetail(Integer questionId) {
        QuestionBank question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new IllegalArgumentException("题目不存在");
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("questionId", question.getQuestionId());
        detail.put("subject", question.getSubject());
        detail.put("type", question.getType());
        detail.put("title", question.getTitle());
        detail.put("options", question.getOptions());
        detail.put("answer", question.getAnswer());
        detail.put("difficulty", question.getDifficulty());
        detail.put("createBy", question.getCreateBy());
        return detail;
    }

    @Override
    public void addGlobalQuestion(QuestionBank question, Integer createBy) {
        question.setCreateBy(createBy);
        questionMapper.insert(question);
    }

    @Override
    public void deleteGlobalQuestion(Integer questionId) {
        QueryWrapper<PaperQuestionRel> relWrapper = new QueryWrapper<>();
        relWrapper.eq("question_id", questionId);
        paperQuestionRelMapper.delete(relWrapper);
        questionMapper.deleteById(questionId);
    }

    @Override
    public Map<String, Object> getGlobalExamDetail(Integer examId) {
        ExamArrangement exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("试卷不存在");
        }

        TestPaper paper = testPaperMapper.selectById(exam.getPaperId());
        QueryWrapper<PaperQuestionRel> relWrapper = new QueryWrapper<>();
        relWrapper.eq("paper_id", exam.getPaperId()).orderByAsc("sort_order");
        List<PaperQuestionRel> relations = paperQuestionRelMapper.selectList(relWrapper);

        List<Map<String, Object>> questions = new ArrayList<>();
        for (PaperQuestionRel relation : relations) {
            QuestionBank question = questionMapper.selectById(relation.getQuestionId());
            if (question == null) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("questionId", question.getQuestionId());
            item.put("title", question.getTitle());
            item.put("type", question.getType());
            item.put("score", relation.getScore());
            questions.add(item);
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("examId", exam.getExamId());
        detail.put("title", exam.getTitle());
        detail.put("subject", paper != null ? paper.getSubject() : "未指定学科");
        detail.put("startTime", exam.getStartTime());
        detail.put("endTime", exam.getEndTime());
        detail.put("targetClasses", exam.getTargetClasses());
        detail.put("questions", questions);
        return detail;
    }

    @Override
    public void updateGlobalExamStatus(Integer examId, String status) {
        ExamArrangement exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("试卷不存在");
        }

        Date now = new Date();
        if ("未开始".equals(status)) {
            exam.setStartTime(new Date(now.getTime() + 60L * 60 * 1000));
            exam.setEndTime(new Date(now.getTime() + 2L * 60 * 60 * 1000));
            exam.setStatus("pending");
        } else if ("进行中".equals(status)) {
            exam.setStartTime(new Date(now.getTime() - 60L * 60 * 1000));
            exam.setEndTime(new Date(now.getTime() + 60L * 60 * 1000));
            exam.setStatus("running");
        } else {
            exam.setStartTime(new Date(now.getTime() - 2L * 60 * 60 * 1000));
            exam.setEndTime(new Date(now.getTime() - 60L * 1000));
            exam.setStatus("finished");
        }
        examMapper.updateById(exam);
    }

    @Override
    public void deleteGlobalExam(Integer examId) {
        ExamArrangement exam = examMapper.selectById(examId);
        if (exam == null) {
            return;
        }

        QueryWrapper<PaperQuestionRel> relWrapper = new QueryWrapper<>();
        relWrapper.eq("paper_id", exam.getPaperId());
        paperQuestionRelMapper.delete(relWrapper);
        testPaperMapper.deleteById(exam.getPaperId());
        examMapper.deleteById(examId);
    }

    @Override
    public void addUser(SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        saveUserMeta(user);
    }

    @Override
    public void updateUser(SysUser user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // prevent overwriting with null
        }
        userMapper.updateById(user);
        SysUser current = userMapper.selectById(user.getUserId());
        if (current != null) {
            current.setManagedClassIds(user.getManagedClassIds());
            current.setManagedSubjects(user.getManagedSubjects());
            saveUserMeta(current);
        }
    }

    @Override
    public void deleteUser(Integer userId) {
        userMapper.deleteById(userId);
    }

    @Override
    public List<SysClass> getClasses(String department) {
        QueryWrapper<SysClass> wrapper = new QueryWrapper<>();
        if (department != null && !department.isEmpty()) wrapper.eq("department", department);
        return classMapper.selectList(wrapper);
    }

    @Override
    public void addClass(SysClass sysClass) {
        classMapper.insert(sysClass);
    }

    @Override
    public void updateClass(SysClass sysClass) {
        classMapper.updateById(sysClass);
    }

    @Override
    public void deleteClass(Integer classId) {
        classMapper.deleteById(classId);
    }

    @Override
    public List<SysAnnouncement> getAnnouncements() {
        QueryWrapper<SysAnnouncement> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("is_top", "create_time");
        return announcementMapper.selectList(wrapper);
    }

    @Override
    public void addAnnouncement(SysAnnouncement announcement) {
        announcementMapper.insert(announcement);
    }

    @Override
    public void updateAnnouncement(SysAnnouncement announcement) {
        announcementMapper.updateById(announcement);
    }

    @Override
    public void deleteAnnouncement(Integer id) {
        announcementMapper.deleteById(id);
    }
}
