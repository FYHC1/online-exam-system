USE `online_exam`;
SET FOREIGN_KEY_CHECKS = 0;

-- ========== 1. 清空原有业务测试数据 (保留系统用户与班级) ==========
TRUNCATE TABLE `question_bank`;
TRUNCATE TABLE `test_paper`;
TRUNCATE TABLE `paper_question_rel`;
TRUNCATE TABLE `exam_arrangement`;
TRUNCATE TABLE `student_exam_record`;
TRUNCATE TABLE `student_answer_detail`;
TRUNCATE TABLE `wrong_question_book`;
TRUNCATE TABLE `sys_announcement`;

-- ========== 2. 插入测试数据 ==========

-- 公告数据
INSERT INTO `sys_announcement` (`id`, `title`, `content`, `is_top`, `create_by`, `create_time`) VALUES
(1, '2023-2024学年第一学期考试纪律最新通知', '各位同学请注意，所有期中考试与期末考试均引入防作弊机制（切屏监控及面部识别检测），请务必遵守考试纪律，诚信考试！', 1, 1, '2023-11-01 10:00:00'),
(2, '系统停机升级维护通知', '本系统将于本周五晚22:00进行性能扩容升级，期间预计停机2个小时，部分同学可能无法登录，请知悉。', 0, 1, '2023-11-05 14:00:00');

-- 题库数据 (JavaWeb, 高等数学, 大学英语)
INSERT INTO `question_bank` (`question_id`, `subject`, `type`, `title`, `options`, `answer`, `difficulty`, `create_by`) VALUES
(1, 'JavaWeb', '单选题', '在Servlet的生命周期中，用于系统初始化的方法是？', '["init()", "service()", "destroy()", "doGet()"]', 'A', 2, 2),
(2, 'JavaWeb', '单选题', '在下面选项中，哪个不属于JSP页面的内建对象？', '["request", "response", "session", "window"]', 'D', 3, 2),
(3, 'JavaWeb', '多选题', '以下属于Spring框架核心设计特性的有：', '["IoC 控制反转", "AOP 面向切面编程", "MVC 分层架构", "Hibernate 对象映射"]', 'A,B', 4, 2),
(4, '高等数学', '判断题', '函数 f(x)=|x| 在 x=0 处是可导的（对或错）。', '["正确", "错误"]', '错误', 2, 2),
(5, '高等数学', '单选题', '求极限 lim(x->0) (sinx / x) 的计算结果为？', '["0", "1", "无穷大", "不存在"]', 'B', 3, 2),
(6, '大学英语', '单选题', 'It is raining cats and ___, stay at home today.', '["dogs", "mice", "birds", "pigs"]', 'A', 1, 2);

-- 试卷数据
INSERT INTO `test_paper` (`paper_id`, `paper_name`, `subject`, `total_score`, `pass_score`, `duration`, `create_by`) VALUES
(1, '2023-2024学年第一学期《JavaWeb》期中测验卷', 'JavaWeb', 100, 60, 120, 2),
(2, '2023-2024学年第一学期《高等数学》历史摸底测试', '高等数学', 100, 60, 60, 2);

-- 试卷题目关联
INSERT INTO `paper_question_rel` (`id`, `paper_id`, `question_id`, `score`, `sort_order`) VALUES
(1, 1, 1, 30, 1),
(2, 1, 2, 30, 2),
(3, 1, 3, 40, 3),
(4, 2, 4, 50, 1),
(5, 2, 5, 50, 2);

-- 考试安排任务 (派发给属于班级ID 1的所有学生，如student1)
INSERT INTO `exam_arrangement` (`exam_id`, `title`, `paper_id`, `target_classes`, `start_time`, `end_time`, `status`, `create_by`) VALUES
(101, '2023-2024 第一学期《JavaWeb》期中考试 (正在进行)', 1, '1,2', '1990-01-01 00:00:00', '2099-12-31 23:59:59', 'running', 2),
(102, '2023-2024 第一学期《高等数学》历史测试 (已结束)', 2, '1', '2022-10-01 09:00:00', '2022-10-01 11:00:00', 'finished', 2);

-- 模拟历史学生考试记录 (student1 历史参考高数)
-- 假设高数卷满分100，答对第1题(50分)，答错第2题(0分)，总分50分不及格
INSERT INTO `student_exam_record` (`record_id`, `exam_id`, `student_id`, `total_score`, `objective_score`, `subjective_score`, `status`) VALUES
(1, 102, 3, 50, 50, 0, 'finished');

-- 模拟历史学生答卷详细追踪点
INSERT INTO `student_answer_detail` (`detail_id`, `record_id`, `question_id`, `answer_content`, `is_correct`, `score`) VALUES
(1, 1, 4, '错误', 1, 50),
(2, 1, 5, 'D', 0, 0);

-- 触发了错题本归档联动（第5题答错了）
INSERT INTO `wrong_question_book` (`id`, `student_id`, `question_id`, `source_exam_id`, `error_count`) VALUES
(1, 3, 5, 102, 1);

SET FOREIGN_KEY_CHECKS = 1;
