package com.exam.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.exam.system.mapper")
public class ExamSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamSystemApplication.class, args);
        System.out.println("===============================================");
        System.out.println("|| Online Exam System Backend Started Config ||");
        System.out.println("|| - Spring Boot 3 + Java 17                 ||");
        System.out.println("|| - Database: MySQL 8.0                     ||");
        System.out.println("|| - Cache: Redis 3.0                        ||");
        System.out.println("===============================================");
    }
}
