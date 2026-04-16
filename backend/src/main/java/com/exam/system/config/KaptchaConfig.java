package com.exam.system.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Bean
    public DefaultKaptcha defaultKaptcha() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        // 验证码是否带边框
        properties.setProperty("kaptcha.border", "no");
        // 验证码字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        // 验证码图片宽度
        properties.setProperty("kaptcha.image.width", "120");
        // 验证码图片高度
        properties.setProperty("kaptcha.image.height", "40");
        // 验证码字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "30");
        // 验证码字符长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 验证码字体
        properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");

        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
