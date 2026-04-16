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
(6, '大学英语', '单选题', 'It is raining cats and ___, stay at home today.', '["dogs", "mice", "birds", "pigs"]', 'A', 1, 2),
(7, '高等数学', '单选题', '函数y=x^2在x=1处的导数值是？', '["1", "2", "3", "4"]', 'B', 2, 2),
(8, '高等数学', '单选题', '不定积分∫2x dx的结果是？', '["x^2+C", "2x+C", "x+C", "4x+C"]', 'A', 2, 2),
(9, '高等数学', '单选题', '定积分表示曲边梯形面积时，积分上下限对应的是？', '["函数值范围", "自变量区间", "导数区间", "极值点个数"]', 'B', 3, 2),
(10, '高等数学', '单选题', '级数1+1/2+1/4+...的和收敛于？', '["1", "2", "3", "发散"]', 'B', 3, 2),
(11, '高等数学', '单选题', '矩阵乘法满足下列哪一性质？', '["交换律总成立", "结合律成立", "任意可逆", "结果维度不变"]', 'B', 4, 2),
(12, '高等数学', '多选题', '下列哪些函数是奇函数？', '["x^3", "sinx", "cosx", "tanx"]', 'A,B,D', 3, 2),
(13, '高等数学', '多选题', '下列哪些方法可用于求极限？', '["洛必达法则", "等价无穷小替换", "配方法", "夹逼准则"]', 'A,B,D', 4, 2),
(14, '高等数学', '判断题', '若函数在某点可导，则该点一定连续。', '["正确", "错误"]', '正确', 1, 2),
(15, '高等数学', '判断题', '定积分的值一定是正数。', '["正确", "错误"]', '错误', 2, 2),
(16, '高等数学', '判断题', '向量数量积结果是一个标量。', '["正确", "错误"]', '正确', 1, 2),
(17, '高等数学', '简答题', '说明导数的几何意义。', NULL, '导数表示函数图像在该点切线的斜率。', 3, 2),
(18, '大学英语', '单选题', 'Choose the correct word: She ___ to school by bus every day.', '["go", "goes", "gone", "going"]', 'B', 1, 2),
(19, '大学英语', '单选题', 'Which word is closest in meaning to "rapid"?', '["slow", "fast", "quiet", "late"]', 'B', 2, 2),
(20, '大学英语', '单选题', 'We have lived here ___ 2018.', '["for", "since", "from", "at"]', 'B', 2, 2),
(21, '大学英语', '单选题', 'The meeting was canceled ___ the heavy rain.', '["because of", "instead of", "according to", "such as"]', 'A', 2, 2),
(22, '大学英语', '单选题', 'If I ___ enough time, I will finish the report.', '["have", "had", "will have", "having"]', 'A', 3, 2),
(23, '大学英语', '多选题', 'Which of the following are countable nouns?', '["book", "water", "idea", "apple"]', 'A,C,D', 2, 2),
(24, '大学英语', '多选题', 'Which sentences are in the present perfect tense?', '["I have finished my homework.", "She is reading now.", "They have gone home.", "We played football yesterday."]', 'A,C', 3, 2),
(25, '大学英语', '判断题', 'The word "information" is an uncountable noun.', '["正确", "错误"]', '正确', 1, 2),
(26, '大学英语', '判断题', 'In English, every sentence must contain a verb.', '["正确", "错误"]', '正确', 1, 2),
(27, '大学英语', '判断题', 'The comparative form of "good" is "gooder".', '["正确", "错误"]', '错误', 1, 2),
(28, '大学英语', '简答题', 'Write two sentences to describe your college life.', NULL, '答案开放，语法正确且表达完整即可。', 3, 2),
(29, 'JavaWeb', '单选题', 'HTTP协议默认使用的端口号是？', '["21", "80", "3306", "8080"]', 'B', 1, 2),
(30, 'JavaWeb', '单选题', '在Spring MVC中，负责接收请求并分发的核心组件是？', '["DispatcherServlet", "Filter", "Listener", "ViewResolver"]', 'A', 3, 2),
(31, 'JavaWeb', '单选题', 'Session通常保存在？', '["客户端浏览器", "服务器端", "数据库中固定表", "DNS服务器"]', 'B', 2, 2),
(32, 'JavaWeb', '单选题', 'RESTful风格中，用于删除资源的HTTP方法通常是？', '["GET", "POST", "PUT", "DELETE"]', 'D', 2, 2),
(33, 'JavaWeb', '单选题', 'MyBatis中用于防止SQL注入并进行预编译的占位符是？', '["${}", "#{}", "@{}", "%{}"]', 'B', 3, 2),
(34, 'JavaWeb', '多选题', '下列哪些属于常见的会话跟踪技术？', '["Cookie", "Session", "URL重写", "DNS缓存"]', 'A,B,C', 3, 2),
(35, 'JavaWeb', '判断题', 'GET请求适合传输大量敏感数据。', '["正确", "错误"]', '错误', 1, 2),
(36, 'JavaWeb', '判断题', 'Filter可以在请求到达Servlet前进行处理。', '["正确", "错误"]', '正确', 2, 2),
(37, 'JavaWeb', '判断题', '前后端分离项目中，后端通常返回JSON数据。', '["正确", "错误"]', '正确', 1, 2),
(38, 'JavaWeb', '简答题', '简述Cookie与Session的区别。', NULL, 'Cookie保存在客户端，Session保存在服务端，二者常配合实现会话管理。', 3, 2),
(39, '数据结构', '单选题', '下列结构中，先进后出的是？', '["队列", "栈", "数组", "链表"]', 'B', 1, 2),
(40, '数据结构', '单选题', '二叉树的前序遍历顺序是？', '["根左右", "左根右", "左右根", "右左根"]', 'A', 2, 2),
(41, '数据结构', '多选题', '下列哪些属于线性结构？', '["数组", "链表", "栈", "二叉树"]', 'A,B,C', 2, 2),
(42, '数据结构', '判断题', '快速排序在最坏情况下时间复杂度为O(n^2)。', '["正确", "错误"]', '正确', 3, 2),
(43, '数据结构', '简答题', '简述队列和栈的主要区别。', NULL, '队列先进先出，栈后进先出。', 2, 2);

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
