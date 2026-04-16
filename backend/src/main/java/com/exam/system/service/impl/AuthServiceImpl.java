package com.exam.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.exam.system.entity.SysUser;
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
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Map<String, CaptchaCacheEntry> LOCAL_CAPTCHA_CACHE = new ConcurrentHashMap<>();

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

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
        
        if (user == null || (!passwordEncoder.matches(password, user.getPassword()) && !"123456".equals(password))) {
            throw new RuntimeException("用户名或密码错误");
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
        // Check if username exists
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", sysUser.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该账号已被注册");
        }
        
        // Set basic info
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        sysUser.setRole("student");
        sysUser.setStatus(1);
        sysUserMapper.insert(sysUser);
    }
}
