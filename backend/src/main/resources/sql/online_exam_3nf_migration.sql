-- ============================================================
-- 在线考试系统 3NF 目标库数据迁移脚本（审阅版）
-- 说明：
-- 1. 假设现有库为 online_exam，目标库为 online_exam_v2。
-- 2. 本脚本仅用于数据迁移，不创建目标库结构。
-- 3. 执行前请先运行 online_exam_3nf_schema.sql。
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 1. 迁移主数据：院系 / 专业 / 班级
-- =========================

INSERT INTO online_exam_v2.department (department_name, status, create_time)
SELECT DISTINCT department, 1, MIN(create_time)
FROM online_exam.sys_class
GROUP BY department;

INSERT INTO online_exam_v2.major (major_name, department_id, status, create_time)
SELECT DISTINCT c.major, d.department_id, 1, MIN(c.create_time)
FROM online_exam.sys_class c
JOIN online_exam_v2.department d ON d.department_name = c.department
GROUP BY c.major, d.department_id;

INSERT INTO online_exam_v2.class_info (class_id, class_name, major_id, grade_year, status, create_time)
SELECT
  c.class_id,
  c.class_name,
  m.major_id,
  YEAR(c.create_time),
  1,
  c.create_time
FROM online_exam.sys_class c
JOIN online_exam_v2.department d ON d.department_name = c.department
JOIN online_exam_v2.major m ON m.major_name = c.major AND m.department_id = d.department_id;

-- =========================
-- 2. 迁移用户与档案
-- =========================

INSERT INTO online_exam_v2.sys_user (user_id, username, password, real_name, role, phone, status, create_time)
SELECT user_id, username, password, real_name, role, phone, status, create_time
FROM online_exam.sys_user;

INSERT INTO online_exam_v2.student_profile (user_id, class_id, student_no)
SELECT user_id, class_id, username
FROM online_exam.sys_user
WHERE role = 'student' AND class_id IS NOT NULL;

INSERT INTO online_exam_v2.teacher_profile (user_id, department_id, teacher_no, subject_id)
SELECT
  u.user_id,
  NULL,
  u.username,
  NULL
FROM online_exam.sys_user u
WHERE u.role = 'teacher';

-- =========================
-- 3. 迁移学科
-- 说明：旧库没有独立学科表，这里先按题库中的 subject 去重建立。
-- department_id 采用“创建教师所属院系”推断，若推断失败则取第一个院系兜底。
-- =========================

INSERT INTO online_exam_v2.subject (subject_name, department_id, status, create_time)
SELECT DISTINCT
  q.subject,
  COALESCE(
    (
      SELECT d.department_id
      FROM online_exam.sys_user su
      JOIN online_exam.sys_class sc ON sc.class_id = su.class_id
      JOIN online_exam_v2.department d ON d.department_name = sc.department
      WHERE su.user_id = q.create_by
      LIMIT 1
    ),
    (
      SELECT department_id FROM online_exam_v2.department ORDER BY department_id LIMIT 1
    )
  ) AS department_id,
  1,
  NOW()
FROM online_exam.question_bank q;

-- =========================
-- 4. 迁移题库 / 试卷 / 试卷题目关系
-- =========================

INSERT INTO online_exam_v2.question_bank (question_id, subject_id, type, title, options, answer, difficulty, create_by, create_time)
SELECT
  q.question_id,
  s.subject_id,
  q.type,
  q.title,
  q.options,
  q.answer,
  q.difficulty,
  q.create_by,
  q.create_time
FROM online_exam.question_bank q
JOIN online_exam_v2.subject s ON s.subject_name = q.subject;

INSERT INTO online_exam_v2.test_paper (paper_id, paper_name, subject_id, total_score, pass_score, duration, create_by, create_time)
SELECT
  p.paper_id,
  p.paper_name,
  s.subject_id,
  p.total_score,
  p.pass_score,
  p.duration,
  p.create_by,
  p.create_time
FROM online_exam.test_paper p
JOIN online_exam_v2.subject s ON s.subject_name = p.subject;

INSERT INTO online_exam_v2.paper_question_rel (id, paper_id, question_id, score, sort_order)
SELECT id, paper_id, question_id, score, sort_order
FROM online_exam.paper_question_rel;

-- =========================
-- 5. 迁移考试安排与目标班级
-- =========================

INSERT INTO online_exam_v2.exam_arrangement (exam_id, title, paper_id, start_time, end_time, status, create_by, create_time)
SELECT exam_id, title, paper_id, start_time, end_time, status, create_by, create_time
FROM online_exam.exam_arrangement;

INSERT INTO online_exam_v2.exam_target_class (exam_id, class_id)
SELECT
  e.exam_id,
  CAST(j.class_id AS UNSIGNED)
FROM online_exam.exam_arrangement e,
JSON_TABLE(
  CONCAT('["', REPLACE(e.target_classes, ',', '","'), '"]'),
  '$[*]' COLUMNS (class_id VARCHAR(20) PATH '$')
) j
WHERE e.target_classes IS NOT NULL AND e.target_classes <> '';

-- =========================
-- 6. 迁移考试记录与答题明细
-- =========================

INSERT INTO online_exam_v2.student_exam_record (record_id, exam_id, student_id, total_score, objective_score, subjective_score, status, create_time)
SELECT
  record_id,
  exam_id,
  student_id,
  total_score,
  objective_score,
  subjective_score,
  status,
  CURRENT_TIMESTAMP
FROM online_exam.student_exam_record;

INSERT INTO online_exam_v2.student_answer_detail (detail_id, record_id, question_id, answer_content, is_correct, score)
SELECT detail_id, record_id, question_id, answer_content, is_correct, score
FROM online_exam.student_answer_detail;

INSERT INTO online_exam_v2.wrong_question_book (id, student_id, question_id, source_exam_id, error_count, last_error_time)
SELECT id, student_id, question_id, source_exam_id, error_count, last_error_time
FROM online_exam.wrong_question_book;

-- =========================
-- 7. 迁移公告
-- =========================

INSERT INTO online_exam_v2.sys_announcement (id, title, content, is_top, create_by, create_time)
SELECT id, title, content, is_top, create_by, create_time
FROM online_exam.sys_announcement;

SET FOREIGN_KEY_CHECKS = 1;
