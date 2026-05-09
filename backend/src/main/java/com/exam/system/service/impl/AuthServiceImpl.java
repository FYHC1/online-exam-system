package com.exam.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.exam.system.entity.SysClass;
import com.exam.system.entity.SysUser;
import com.exam.system.mapper.SysClassMapper;
import com.exam.system.mapper.SysUserMapper;
import com.exam.system.service.AuthService;
import com.exam.system.utils.JwtUtils;
import com.exam.system.utils.RedisUtils;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.FastByteArrayOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Map<String, CaptchaCacheEntry> LOCAL_CAPTCHA_CACHE = new ConcurrentHashMap<>();
    private static final Path TERM_SETTINGS_FILE = Paths.get("data", "term-settings.json");
    private static final Path ORG_STRUCTURE_FILE = Paths.get("data", "org-structure.json");
    private static final String LEGACY_DEFAULT_PASSWORD_HASH = "$2a$10$7Q/.0Qj7YQQ0K2i3HnZ4e.2N.L/h0U/ZzD/x6xY25a58U8KFVpPoy";

    private static class CaptchaCacheEntry {
        private final String code;
        private final long expireAt;

        private CaptchaCacheEntry(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }

    @Autowired
    private DefaultKaptcha kaptcha;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysClassMapper sysClassMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    private int getEarliestGradeYear() {
        try {
            if (Files.notExists(TERM_SETTINGS_FILE)) {
                return 2020;
            }
            Map<String, Object> settings = objectMapper.readValue(TERM_SETTINGS_FILE.toFile(), new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> terms = (List<Map<String, Object>>) settings.getOrDefault("terms", new ArrayList<>());
            return terms.stream()
                .map(item -> String.valueOf(item.get("name")))
                .map(name -> name.replaceAll("^([0-9]{4}).*", "$1"))
                .mapToInt(Integer::parseInt)
                .min()
                .orElse(2020);
        } catch (Exception e) {
            return 2020;
        }
    }

    private String normalizeClassName(String className) {
        return className == null ? "" : className.replaceAll("\\s+", "").trim();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadOrgStructure() {
        try {
            if (Files.notExists(ORG_STRUCTURE_FILE)) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(ORG_STRUCTURE_FILE.toFile(), new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, String> getCaptcha() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String text = kaptcha.createText();

        // Save text in Redis when available and always keep a local fallback.
        try {
            redisUtils.set("captcha:" + uuid, text, 2, TimeUnit.MINUTES);
        } catch (Exception e) {
            // Fall back to in-memory cache when Redis is unavailable.
        }
        LOCAL_CAPTCHA_CACHE.put(uuid, new CaptchaCacheEntry(text, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2)));

        BufferedImage image = kaptcha.createImage(text);
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码失败");
        }
        
        String base64Img = Base64.getEncoder().encodeToString(os.toByteArray());
        
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("uuid", uuid);
        resultMap.put("img", "data:image/jpeg;base64," + base64Img);
        
        return resultMap;
    }

    @Override
    public Map<String, Object> login(String username, String password, String role, String code, String uuid) {
        // Allow dummy captcha when fallback testing is needed.
        boolean bypassCaptcha = "dummy".equalsIgnoreCase(code) && "dummy".equalsIgnoreCase(uuid);

        if (!bypassCaptcha) {
            String captchaKey = "captcha:" + uuid;
            String cachedCode = null;
            try {
                cachedCode = redisUtils.get(captchaKey);
            } catch (Exception e) {
                // Ignore and try local fallback cache below.
            }

            CaptchaCacheEntry localEntry = LOCAL_CAPTCHA_CACHE.get(uuid);
            if (cachedCode == null && localEntry != null && localEntry.expireAt > System.currentTimeMillis()) {
                cachedCode = localEntry.code;
            }

            if (cachedCode == null || !cachedCode.equalsIgnoreCase(code)) {
                throw new RuntimeException("验证码错误或已失效");
            }

            try {
                redisUtils.delete(captchaKey);
            } catch (Exception e) {
                // Ignore Redis cleanup failures.
            }
            LOCAL_CAPTCHA_CACHE.remove(uuid);
        }

        // Get user details
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username).eq("role", role);
        SysUser user = sysUserMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        boolean passwordMatched = passwordEncoder.matches(password, user.getPassword());
        boolean legacyDefaultPasswordMatched = LEGACY_DEFAULT_PASSWORD_HASH.equals(user.getPassword()) && "123456".equals(password);
        if (!passwordMatched && !legacyDefaultPasswordMatched) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (legacyDefaultPasswordMatched) {
            user.setPassword(passwordEncoder.encode(password));
            sysUserMapper.updateById(user);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("您的账户已被禁用");
        }

        // Generate JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("role", user.getRole());
        String token = jwtUtils.generateToken(claims, user.getUsername());

        // Prepare return object
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("classId", user.getClassId());

        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        res.put("userInfo", userInfo);
        return res;
    }

    @Override
    public void register(SysUser sysUser) {
        if (sysUser.getUsername() == null || sysUser.getPassword() == null || sysUser.getRealName() == null) {
            throw new RuntimeException("请完整填写注册信息");
        }

        // Check if username exists
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", sysUser.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该账号已被注册");
        }

        if (sysUser.getRole() != null && !"student".equals(sysUser.getRole())) {
            throw new RuntimeException("仅支持学生自助注册");
        }

        SysClass sysClass = null;
        if (sysUser.getClassId() != null && sysUser.getClassId() > 0) {
            sysClass = sysClassMapper.selectById(sysUser.getClassId());
        }

        if (sysClass == null) {
            if (sysUser.getDepartment() == null || sysUser.getMajor() == null || sysUser.getClassName() == null || sysUser.getGrade() == null) {
                throw new RuntimeException("请选择完整的学院、专业、班级和年级信息");
            }

            QueryWrapper<SysClass> classWrapper = new QueryWrapper<>();
            classWrapper.eq("department", sysUser.getDepartment())
                .eq("major", sysUser.getMajor())
                .last("LIMIT 1");
            List<SysClass> matchedClasses = sysClassMapper.selectList(classWrapper);
            sysClass = matchedClasses.stream()
                .filter(item -> normalizeClassName(item.getClassName()).equals(normalizeClassName(sysUser.getClassName())))
                .findFirst()
                .orElse(null);

            if (sysClass == null) {
                sysClass = new SysClass();
                sysClass.setDepartment(sysUser.getDepartment());
                sysClass.setMajor(sysUser.getMajor());
                sysClass.setClassName(normalizeClassName(sysUser.getClassName()));
                int gradeYear = Integer.parseInt(sysUser.getGrade().replace("级", ""));
                sysClass.setCreateTime(java.sql.Timestamp.valueOf(gradeYear + "-09-01 00:00:00"));
                sysClassMapper.insert(sysClass);
            }
        }

        if (sysClass == null) {
            throw new RuntimeException("所选班级不存在");
        }

        if (sysUser.getDepartment() != null && !sysUser.getDepartment().isBlank() && !sysUser.getDepartment().equals(sysClass.getDepartment())) {
            throw new RuntimeException("所选学院与班级信息不匹配");
        }
        if (sysUser.getMajor() != null && !sysUser.getMajor().isBlank() && !sysUser.getMajor().equals(sysClass.getMajor())) {
            throw new RuntimeException("所选专业与班级信息不匹配");
        }

        String inferredGrade = null;
        if (sysUser.getUsername().matches("^20\\d{2}.*")) {
            inferredGrade = sysUser.getUsername().substring(0, 4) + "级";
        }
        if (inferredGrade != null && sysUser.getGrade() != null && !sysUser.getGrade().isBlank() && !inferredGrade.equals(sysUser.getGrade())) {
            throw new RuntimeException("学号与所选年级不匹配");
        }

        int currentYear = java.time.Year.now().getValue();
        int earliestYear = getEarliestGradeYear();
        if (sysUser.getGrade() != null && !sysUser.getGrade().isBlank()) {
            int selectedYear = Integer.parseInt(sysUser.getGrade().replace("级", ""));
            if (selectedYear < earliestYear || selectedYear > currentYear) {
                throw new RuntimeException("所选年级不在允许范围内");
            }
        }

        // Set basic info
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        sysUser.setRole("student");
        // New self-registered students stay disabled until an admin approves them.
        sysUser.setStatus(0);
        sysUser.setClassId(sysClass.getClassId());
        sysUserMapper.insert(sysUser);
    }

    @Override
    public Map<String, Object> getRegisterOptions() {
        List<SysClass> classes = sysClassMapper.selectList(new QueryWrapper<SysClass>().orderByAsc("department", "major", "class_name"));
        Map<String, Map<String, Object>> mergedClasses = new HashMap<>();

        for (SysClass cls : classes) {
            Map<String, Object> item = new HashMap<>();
            item.put("classId", cls.getClassId());
            item.put("className", normalizeClassName(cls.getClassName()));
            item.put("major", cls.getMajor());
            item.put("department", cls.getDepartment());
            item.put("createTime", cls.getCreateTime());
            mergedClasses.put(cls.getDepartment() + "|" + cls.getMajor() + "|" + normalizeClassName(cls.getClassName()), item);
        }

        int syntheticId = -1;
        for (Map<String, Object> department : loadOrgStructure()) {
            String departmentName = String.valueOf(department.get("name"));
            List<Map<String, Object>> majors = (List<Map<String, Object>>) department.getOrDefault("majors", new ArrayList<>());
            for (Map<String, Object> major : majors) {
                String majorName = String.valueOf(major.get("name"));
                List<Map<String, Object>> classItems = (List<Map<String, Object>>) major.getOrDefault("classes", new ArrayList<>());
                for (Map<String, Object> classItem : classItems) {
                    String className = normalizeClassName(String.valueOf(classItem.get("name")));
                    String key = departmentName + "|" + majorName + "|" + className;
                    if (!mergedClasses.containsKey(key)) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("classId", syntheticId--);
                        item.put("className", className);
                        item.put("major", majorName);
                        item.put("department", departmentName);
                        item.put("createTime", null);
                        mergedClasses.put(key, item);
                    }
                }
            }
        }

        int currentYear = java.time.Year.now().getValue();
        int earliestYear = getEarliestGradeYear();
        List<String> grades = new ArrayList<>();
        for (int year = earliestYear; year <= currentYear; year++) {
            grades.add(year + "级");
        }

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> classOptions = new ArrayList<>(mergedClasses.values());
        classOptions.sort(Comparator.comparing(item -> String.valueOf(item.get("department")) + String.valueOf(item.get("major")) + String.valueOf(item.get("className"))));
        result.put("classes", classOptions);
        result.put("grades", grades);
        return result;
    }
}
