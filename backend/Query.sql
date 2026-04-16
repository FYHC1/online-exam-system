-- Create Database
CREATE DATABASE IF NOT EXISTS `online_exam` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `online_exam`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_class
-- ----------------------------
DROP TABLE IF EXISTS `sys_class`;
CREATE TABLE `sys_class`  (
  `class_id` int NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '班级名称',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属专业',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属院系',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`class_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '班级信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_class
-- ----------------------------
INSERT INTO `sys_class` VALUES (1, '软件工程1班', '软件工程', '计算机与信息学院', '2023-09-01 00:00:00');
INSERT INTO `sys_class` VALUES (2, '软件工程2班', '软件工程', '计算机与信息学院', '2023-09-01 00:00:00');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号(学号/工号)',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(BCrypt加密)',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色: admin, teacher, student',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系方式',
  `class_id` int NULL DEFAULT NULL COMMENT '所属班级ID(外键)',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态: 1正常 0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  INDEX `fk_user_class`(`class_id`) USING BTREE,
  CONSTRAINT `fk_user_class` FOREIGN KEY (`class_id`) REFERENCES `sys_class` (`class_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user (Passwords are BCrypt of '123456')
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$7Q/.0Qj7YQQ0K2i3HnZ4e.2N.L/h0U/ZzD/x6xY25a58U8KFVpPoy', '超级管', 'admin', '13800138000', NULL, 1, '2023-09-01 00:00:00');
INSERT INTO `sys_user` VALUES (2, 'teacher1', '$2a$10$7Q/.0Qj7YQQ0K2i3HnZ4e.2N.L/h0U/ZzD/x6xY25a58U8KFVpPoy', '李老师', 'teacher', '13900139000', NULL, 1, '2023-09-01 00:00:00');
INSERT INTO `sys_user` VALUES (3, 'student1', '$2a$10$7Q/.0Qj7YQQ0K2i3HnZ4e.2N.L/h0U/ZzD/x6xY25a58U8KFVpPoy', '张同学', 'student', '13700137000', 1, 1, '2023-09-01 00:00:00');

-- ----------------------------
-- Table structure for question_bank
-- ----------------------------
DROP TABLE IF EXISTS `question_bank`;
CREATE TABLE `question_bank`  (
  `question_id` int NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学科/科目',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题型: 单选题/多选题/判断题/填空题/简答题',
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题干',
  `options` json NULL COMMENT '选项(JSON格式)',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标准答案',
  `difficulty` tinyint NULL DEFAULT 3 COMMENT '难度系数1-5',
  `create_by` int NULL DEFAULT NULL COMMENT '创建教师ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for test_paper
-- ----------------------------
DROP TABLE IF EXISTS `test_paper`;
CREATE TABLE `test_paper`  (
  `paper_id` int NOT NULL AUTO_INCREMENT,
  `paper_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '试卷名称',
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对应科目',
  `total_score` int NOT NULL DEFAULT 100 COMMENT '总分',
  `pass_score` int NOT NULL DEFAULT 60 COMMENT '及格线',
  `duration` int NOT NULL DEFAULT 120 COMMENT '考试时长(分钟)',
  `create_by` int NULL DEFAULT NULL COMMENT '创建教师ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`paper_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '试卷模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for paper_question_rel
-- ----------------------------
DROP TABLE IF EXISTS `paper_question_rel`;
CREATE TABLE `paper_question_rel`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `paper_id` int NOT NULL,
  `question_id` int NOT NULL,
  `score` int NOT NULL COMMENT '该题在一卷中的分值',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_rel_paper`(`paper_id`) USING BTREE,
  INDEX `fk_rel_question`(`question_id`) USING BTREE,
  CONSTRAINT `fk_rel_paper` FOREIGN KEY (`paper_id`) REFERENCES `test_paper` (`paper_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_rel_question` FOREIGN KEY (`question_id`) REFERENCES `question_bank` (`question_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '试卷-题目关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for exam_arrangement
-- ----------------------------
DROP TABLE IF EXISTS `exam_arrangement`;
CREATE TABLE `exam_arrangement`  (
  `exam_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '考试标题',
  `paper_id` int NOT NULL COMMENT '关联试卷ID',
  `target_classes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标班级ID集合(逗号分隔)',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态: pending/running/finished',
  `create_by` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`exam_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '考试安排任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_announcement
-- ----------------------------
DROP TABLE IF EXISTS `sys_announcement`;
CREATE TABLE `sys_announcement` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_top` tinyint(1) DEFAULT 0,
  `create_by` int DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ----------------------------
-- Table structure for student_exam_record
-- ----------------------------
DROP TABLE IF EXISTS `student_exam_record`;
CREATE TABLE `student_exam_record` (
  `record_id` int NOT NULL AUTO_INCREMENT,
  `exam_id` int NOT NULL,
  `student_id` int NOT NULL,
  `total_score` int DEFAULT NULL,
  `objective_score` int DEFAULT NULL,
  `subjective_score` int DEFAULT NULL,
  `status` varchar(20) DEFAULT 'pending',
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生考试记录表';

-- ----------------------------
-- Table structure for student_answer_detail
-- ----------------------------
DROP TABLE IF EXISTS `student_answer_detail`;
CREATE TABLE `student_answer_detail` (
  `detail_id` int NOT NULL AUTO_INCREMENT,
  `record_id` int NOT NULL,
  `question_id` int NOT NULL,
  `answer_content` text,
  `is_correct` tinyint(1) DEFAULT NULL,
  `score` int DEFAULT NULL,
  PRIMARY KEY (`detail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生答题明细表';

-- ----------------------------
-- Table structure for wrong_question_book
-- ----------------------------
DROP TABLE IF EXISTS `wrong_question_book`;
CREATE TABLE `wrong_question_book` (
  `id` int NOT NULL AUTO_INCREMENT,
  `student_id` int NOT NULL,
  `question_id` int NOT NULL,
  `source_exam_id` int DEFAULT NULL,
  `error_count` int DEFAULT 1,
  `last_error_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='错题本';

SET FOREIGN_KEY_CHECKS = 1;
