-- ============================================================
-- 在线考试系统 3NF 目标库建库脚本（审阅版）
-- 说明：
-- 1. 本脚本仅用于创建新的目标数据库结构，不操作现有 online_exam 库。
-- 2. 建议新库名称：online_exam_v2
-- 3. 审阅通过后，再执行本脚本与迁移脚本。
-- ============================================================

CREATE DATABASE IF NOT EXISTS `online_exam_v2`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `online_exam_v2`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 1. 主数据：院系 / 专业 / 班级 / 学科
-- =========================

DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `department_id` INT NOT NULL AUTO_INCREMENT,
  `department_name` VARCHAR(100) NOT NULL COMMENT '院系名称',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`department_id`),
  UNIQUE KEY `uk_department_name` (`department_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='院系表';

DROP TABLE IF EXISTS `major`;
CREATE TABLE `major` (
  `major_id` INT NOT NULL AUTO_INCREMENT,
  `major_name` VARCHAR(100) NOT NULL COMMENT '专业名称',
  `department_id` INT NOT NULL COMMENT '所属院系',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`major_id`),
  UNIQUE KEY `uk_major_dept` (`major_name`, `department_id`),
  CONSTRAINT `fk_major_department_v2` FOREIGN KEY (`department_id`) REFERENCES `department` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业表';

DROP TABLE IF EXISTS `class_info`;
CREATE TABLE `class_info` (
  `class_id` INT NOT NULL AUTO_INCREMENT,
  `class_name` VARCHAR(100) NOT NULL COMMENT '班级名称',
  `major_id` INT NOT NULL COMMENT '所属专业',
  `grade_year` INT NOT NULL COMMENT '年级，例如 2023',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`class_id`),
  UNIQUE KEY `uk_class_major_grade` (`class_name`, `major_id`, `grade_year`),
  CONSTRAINT `fk_class_info_major_v2` FOREIGN KEY (`major_id`) REFERENCES `major` (`major_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

DROP TABLE IF EXISTS `subject`;
CREATE TABLE `subject` (
  `subject_id` INT NOT NULL AUTO_INCREMENT,
  `subject_name` VARCHAR(100) NOT NULL COMMENT '学科名称',
  `department_id` INT NOT NULL COMMENT '所属院系',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`subject_id`),
  UNIQUE KEY `uk_subject_dept` (`subject_name`, `department_id`),
  CONSTRAINT `fk_subject_department_v2` FOREIGN KEY (`department_id`) REFERENCES `department` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学科表';

-- =========================
-- 2. 用户与档案
-- =========================

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号(学号/工号)',
  `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `role` VARCHAR(20) NOT NULL COMMENT 'admin/teacher/student',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系方式',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

DROP TABLE IF EXISTS `student_profile`;
CREATE TABLE `student_profile` (
  `user_id` INT NOT NULL COMMENT '对应 sys_user.user_id',
  `class_id` INT NOT NULL COMMENT '所属班级',
  `student_no` VARCHAR(50) NOT NULL COMMENT '学号',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  CONSTRAINT `fk_student_profile_user_v2` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`),
  CONSTRAINT `fk_student_profile_class_v2` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生档案表';

DROP TABLE IF EXISTS `teacher_profile`;
CREATE TABLE `teacher_profile` (
  `user_id` INT NOT NULL COMMENT '对应 sys_user.user_id',
  `department_id` INT DEFAULT NULL COMMENT '所属院系',
  `teacher_no` VARCHAR(50) DEFAULT NULL COMMENT '工号',
  `subject_id` INT DEFAULT NULL COMMENT '负责学科',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_teacher_no` (`teacher_no`),
  CONSTRAINT `fk_teacher_profile_user_v2` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`),
  CONSTRAINT `fk_teacher_profile_department_v2` FOREIGN KEY (`department_id`) REFERENCES `department` (`department_id`),
  CONSTRAINT `fk_teacher_profile_subject_v2` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师档案表';

-- =========================
-- 3. 题库与试卷
-- =========================

DROP TABLE IF EXISTS `question_bank`;
CREATE TABLE `question_bank` (
  `question_id` INT NOT NULL AUTO_INCREMENT,
  `subject_id` INT NOT NULL COMMENT '所属学科',
  `type` VARCHAR(20) NOT NULL COMMENT '题型',
  `title` TEXT NOT NULL COMMENT '题干',
  `options` JSON DEFAULT NULL COMMENT '选项(JSON)',
  `answer` TEXT NOT NULL COMMENT '标准答案',
  `difficulty` TINYINT NOT NULL DEFAULT 3 COMMENT '难度 1-5',
  `create_by` INT DEFAULT NULL COMMENT '创建教师ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`),
  KEY `idx_question_subject` (`subject_id`),
  KEY `idx_question_creator` (`create_by`),
  CONSTRAINT `fk_question_bank_subject_v2` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`subject_id`),
  CONSTRAINT `fk_question_bank_creator_v2` FOREIGN KEY (`create_by`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库表';

DROP TABLE IF EXISTS `test_paper`;
CREATE TABLE `test_paper` (
  `paper_id` INT NOT NULL AUTO_INCREMENT,
  `paper_name` VARCHAR(200) NOT NULL COMMENT '试卷名称',
  `subject_id` INT NOT NULL COMMENT '所属学科',
  `total_score` INT NOT NULL DEFAULT 100,
  `pass_score` INT NOT NULL DEFAULT 60,
  `duration` INT NOT NULL DEFAULT 120 COMMENT '时长(分钟)',
  `create_by` INT DEFAULT NULL COMMENT '创建教师ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`paper_id`),
  KEY `idx_paper_subject` (`subject_id`),
  KEY `idx_paper_creator` (`create_by`),
  CONSTRAINT `fk_test_paper_subject_v2` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`subject_id`),
  CONSTRAINT `fk_test_paper_creator_v2` FOREIGN KEY (`create_by`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷表';

DROP TABLE IF EXISTS `paper_question_rel`;
CREATE TABLE `paper_question_rel` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `paper_id` INT NOT NULL,
  `question_id` INT NOT NULL,
  `score` INT NOT NULL COMMENT '题目分值',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_paper_question` (`paper_id`, `question_id`),
  KEY `idx_rel_question` (`question_id`),
  CONSTRAINT `fk_paper_question_rel_paper_v2` FOREIGN KEY (`paper_id`) REFERENCES `test_paper` (`paper_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_paper_question_rel_question_v2` FOREIGN KEY (`question_id`) REFERENCES `question_bank` (`question_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷题目关系表';

-- =========================
-- 4. 考试安排
-- =========================

DROP TABLE IF EXISTS `exam_arrangement`;
CREATE TABLE `exam_arrangement` (
  `exam_id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL COMMENT '考试标题',
  `paper_id` INT NOT NULL COMMENT '关联试卷',
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending',
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`exam_id`),
  KEY `idx_exam_paper` (`paper_id`),
  KEY `idx_exam_creator` (`create_by`),
  CONSTRAINT `fk_exam_arrangement_paper_v2` FOREIGN KEY (`paper_id`) REFERENCES `test_paper` (`paper_id`),
  CONSTRAINT `fk_exam_arrangement_creator_v2` FOREIGN KEY (`create_by`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试安排表';

DROP TABLE IF EXISTS `exam_target_class`;
CREATE TABLE `exam_target_class` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `exam_id` INT NOT NULL,
  `class_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_class` (`exam_id`, `class_id`),
  KEY `idx_target_class` (`class_id`),
  CONSTRAINT `fk_exam_target_class_exam_v2` FOREIGN KEY (`exam_id`) REFERENCES `exam_arrangement` (`exam_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_exam_target_class_class_v2` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试目标班级表';

-- =========================
-- 5. 作答与错题
-- =========================

DROP TABLE IF EXISTS `student_exam_record`;
CREATE TABLE `student_exam_record` (
  `record_id` INT NOT NULL AUTO_INCREMENT,
  `exam_id` INT NOT NULL,
  `student_id` INT NOT NULL COMMENT '对应 sys_user.user_id',
  `total_score` INT DEFAULT NULL,
  `objective_score` INT DEFAULT NULL,
  `subjective_score` INT DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_exam_student` (`exam_id`, `student_id`),
  CONSTRAINT `fk_student_exam_record_exam_v2` FOREIGN KEY (`exam_id`) REFERENCES `exam_arrangement` (`exam_id`),
  CONSTRAINT `fk_student_exam_record_student_v2` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生考试记录表';

DROP TABLE IF EXISTS `student_answer_detail`;
CREATE TABLE `student_answer_detail` (
  `detail_id` INT NOT NULL AUTO_INCREMENT,
  `record_id` INT NOT NULL,
  `question_id` INT NOT NULL,
  `answer_content` TEXT,
  `is_correct` TINYINT(1) DEFAULT NULL,
  `score` INT DEFAULT NULL,
  PRIMARY KEY (`detail_id`),
  UNIQUE KEY `uk_record_question` (`record_id`, `question_id`),
  CONSTRAINT `fk_student_answer_detail_record_v2` FOREIGN KEY (`record_id`) REFERENCES `student_exam_record` (`record_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_student_answer_detail_question_v2` FOREIGN KEY (`question_id`) REFERENCES `question_bank` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生答题明细表';

DROP TABLE IF EXISTS `wrong_question_book`;
CREATE TABLE `wrong_question_book` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `student_id` INT NOT NULL,
  `question_id` INT NOT NULL,
  `source_exam_id` INT DEFAULT NULL,
  `error_count` INT NOT NULL DEFAULT 1,
  `last_error_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wrong_student_question` (`student_id`, `question_id`),
  CONSTRAINT `fk_wrong_question_book_student_v2` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`user_id`),
  CONSTRAINT `fk_wrong_question_book_question_v2` FOREIGN KEY (`question_id`) REFERENCES `question_bank` (`question_id`),
  CONSTRAINT `fk_wrong_question_book_exam_v2` FOREIGN KEY (`source_exam_id`) REFERENCES `exam_arrangement` (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='错题本表';

-- =========================
-- 6. 保留原业务表
-- =========================

DROP TABLE IF EXISTS `sys_announcement`;
CREATE TABLE `sys_announcement` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `is_top` TINYINT(1) DEFAULT 0,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

SET FOREIGN_KEY_CHECKS = 1;
