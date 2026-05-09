package com.exam.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.exam.system.entity.ExamArrangement;
import com.exam.system.entity.QuestionBank;
import com.exam.system.entity.SysClass;
import com.exam.system.entity.SysUser;
import com.exam.system.entity.TestPaper;
import com.exam.system.mapper.ExamArrangementMapper;
import com.exam.system.mapper.QuestionBankMapper;
import com.exam.system.mapper.SysClassMapper;
import com.exam.system.mapper.SysUserMapper;
import com.exam.system.mapper.TestPaperMapper;
import com.exam.system.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Path PROFILE_META_FILE = Paths.get("data", "user-profile-meta.json");
    private static final Path TERM_SETTINGS_FILE = Paths.get("data", "term-settings.json");
    private static final String LEGACY_DEFAULT_PASSWORD_HASH = "$2a$10$7Q/.0Qj7YQQ0K2i3HnZ4e.2N.L/h0U/ZzD/x6xY25a58U8KFVpPoy";

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysClassMapper sysClassMapper;

    @Autowired
    private QuestionBankMapper questionBankMapper;

    @Autowired
    private ExamArrangementMapper examArrangementMapper;

    @Autowired
    private TestPaperMapper testPaperMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private SysUser getUserByUsername(String username) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username).last("LIMIT 1");
        SysUser user = sysUserMapper.selectOne(wrapper);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private Map<String, Map<String, Object>> loadProfileMeta() {
        try {
            if (Files.notExists(PROFILE_META_FILE)) {
                Files.createDirectories(PROFILE_META_FILE.getParent());
                return new HashMap<>();
            }
            return objectMapper.readValue(PROFILE_META_FILE.toFile(), new TypeReference<Map<String, Map<String, Object>>>() {});
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    private void saveProfileMeta(Map<String, Map<String, Object>> meta) {
        try {
            Files.createDirectories(PROFILE_META_FILE.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(PROFILE_META_FILE.toFile(), meta);
        } catch (IOException e) {
            throw new RuntimeException("保存头像信息失败", e);
        }
    }

    private Map<String, Object> loadTermSettings() {
        try {
            if (Files.notExists(TERM_SETTINGS_FILE)) {
                Map<String, Object> defaults = new HashMap<>();
                defaults.put("terms", new java.util.ArrayList<>());
                defaults.put("currentTerm", "");
                defaults.put("currentRange", new java.util.ArrayList<>());
                return defaults;
            }
            return objectMapper.readValue(TERM_SETTINGS_FILE.toFile(), new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            Map<String, Object> defaults = new HashMap<>();
            defaults.put("terms", new java.util.ArrayList<>());
            defaults.put("currentTerm", "");
            defaults.put("currentRange", new java.util.ArrayList<>());
            return defaults;
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

    private Map<String, Object> classToMap(SysClass cls) {
        Map<String, Object> item = new HashMap<>();
        item.put("classId", cls.getClassId());
        item.put("className", cls.getClassName());
        item.put("major", cls.getMajor());
        item.put("department", cls.getDepartment());
        if (cls.getCreateTime() != null) {
            int gradeYear = cls.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).getYear();
            item.put("grade", gradeYear + "级");
        }
        return item;
    }

    private List<SysClass> getManagedClasses(SysUser teacher, Map<String, Object> userMeta) {
        LinkedHashSet<Integer> classIds = new LinkedHashSet<>(toIntegerList(userMeta.get("managedClassIds")));
        if (classIds.isEmpty() && teacher.getClassId() != null) {
            SysClass teacherClass = sysClassMapper.selectById(teacher.getClassId());
            if (teacherClass != null && teacherClass.getDepartment() != null) {
                QueryWrapper<SysClass> wrapper = new QueryWrapper<>();
                wrapper.eq("department", teacherClass.getDepartment());
                for (SysClass cls : sysClassMapper.selectList(wrapper)) {
                    classIds.add(cls.getClassId());
                }
            }
        }
        if (classIds.isEmpty()) {
            QueryWrapper<ExamArrangement> wrapper = new QueryWrapper<>();
            wrapper.eq("create_by", teacher.getUserId());
            for (ExamArrangement exam : examArrangementMapper.selectList(wrapper)) {
                classIds.addAll(parseTargetClassIds(exam.getTargetClasses()));
            }
        }
        return classIds.stream()
                .map(sysClassMapper::selectById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> getTeacherSubjects(Integer teacherId, Map<String, Object> userMeta) {
        List<String> managedSubjects = toStringList(userMeta.get("managedSubjects"));
        if (!managedSubjects.isEmpty()) {
            return managedSubjects;
        }

        LinkedHashSet<String> subjects = new LinkedHashSet<>();
        QueryWrapper<QuestionBank> questionWrapper = new QueryWrapper<>();
        questionWrapper.eq("create_by", teacherId);
        for (QuestionBank question : questionBankMapper.selectList(questionWrapper)) {
            if (question.getSubject() != null && !question.getSubject().isBlank()) {
                subjects.add(question.getSubject());
            }
        }

        QueryWrapper<TestPaper> paperWrapper = new QueryWrapper<>();
        paperWrapper.eq("create_by", teacherId);
        for (TestPaper paper : testPaperMapper.selectList(paperWrapper)) {
            if (paper.getSubject() != null && !paper.getSubject().isBlank()) {
                subjects.add(paper.getSubject());
            }
        }
        return new ArrayList<>(subjects);
    }

    @Override
    public Map<String, Object> getProfile(String username) {
        SysUser user = getUserByUsername(username);
        Map<String, Map<String, Object>> meta = loadProfileMeta();
        Map<String, Object> userMeta = meta.getOrDefault(String.valueOf(user.getUserId()), new HashMap<>());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole());
        result.put("phone", user.getPhone());
        result.put("avatar", userMeta.getOrDefault("avatar", ""));

        if (user.getClassId() != null) {
            SysClass sysClass = sysClassMapper.selectById(user.getClassId());
            if (sysClass != null) {
                int gradeYear = sysClass.getCreateTime() != null ? sysClass.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).getYear() : 2023;
                result.put("grade", gradeYear + "级");
                result.put("enrollmentYear", String.valueOf(gradeYear));
                result.put("department", sysClass.getDepartment());
                result.put("major", sysClass.getMajor());
                result.put("className", sysClass.getClassName());
            }
        }

        if ("teacher".equals(user.getRole())) {
            List<SysClass> managedClasses = getManagedClasses(user, userMeta);
            result.put("managedClassIds", managedClasses.stream().map(SysClass::getClassId).collect(Collectors.toList()));
            result.put("managedClasses", managedClasses.stream().map(this::classToMap).collect(Collectors.toList()));
            result.put("subjects", getTeacherSubjects(user.getUserId(), userMeta));
        }

        return result;
    }

    @Override
    public Map<String, Object> getTermOptions() {
        return loadTermSettings();
    }

    @Override
    public Map<String, Object> updateProfile(String username, Map<String, Object> payload) {
        SysUser user = getUserByUsername(username);
        if (payload.get("phone") != null) {
            user.setPhone(payload.get("phone").toString());
        }
        sysUserMapper.updateById(user);

        if (payload.get("avatar") != null) {
            Map<String, Map<String, Object>> meta = loadProfileMeta();
            Map<String, Object> userMeta = meta.getOrDefault(String.valueOf(user.getUserId()), new HashMap<>());
            userMeta.put("avatar", payload.get("avatar").toString());
            meta.put(String.valueOf(user.getUserId()), userMeta);
            saveProfileMeta(meta);
        }

        return getProfile(username);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        SysUser user = getUserByUsername(username);
        boolean passwordMatched = passwordEncoder.matches(oldPassword, user.getPassword());
        boolean legacyDefaultPasswordMatched = LEGACY_DEFAULT_PASSWORD_HASH.equals(user.getPassword()) && "123456".equals(oldPassword);
        if (!passwordMatched && !legacyDefaultPasswordMatched) {
            throw new IllegalArgumentException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
    }
}
