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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Path ORG_STRUCTURE_FILE = Paths.get("data", "org-structure.json");
    private static final Path TERM_SETTINGS_FILE = Paths.get("data", "term-settings.json");

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysClassMapper classMapper;
    @Autowired
    private SysAnnouncementMapper announcementMapper;
    @Autowired
    private ExamArrangementMapper examMapper;
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
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalExams", examMapper.selectCount(null));
        stats.put("totalQuestions", questionMapper.selectCount(null));
        return stats;
    }

    @Override
    public List<SysUser> getUsers(String role, String keyword) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        if (role != null && !role.isEmpty()) wrapper.eq("role", role);
        if (keyword != null && !keyword.isEmpty()) wrapper.like("real_name", keyword);
        return userMapper.selectList(wrapper);
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
                        try {
                            SysClass sysClass = classMapper.selectById(Integer.parseInt(firstClassId));
                            if (sysClass != null) {
                                department = sysClass.getDepartment();
                            }
                        } catch (NumberFormatException ignored) {
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
                try {
                    SysClass sysClass = classMapper.selectById(Integer.parseInt(firstClassId));
                    if (sysClass != null) {
                        college = sysClass.getDepartment();
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            item.put("college", college);

            String status = "未开始";
            java.util.Date now = new java.util.Date();
            if (exam.getEndTime() != null && !now.before(exam.getEndTime())) {
                status = "已结束";
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

        for (Map<String, Object> department : structure) {
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
    }

    @Override
    public void updateUser(SysUser user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // prevent overwriting with null
        }
        userMapper.updateById(user);
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
