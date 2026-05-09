-- MySQL dump 10.13  Distrib 8.0.12, for Win64 (x86_64)
--
-- Host: localhost    Database: online_exam
-- ------------------------------------------------------
-- Server version	8.0.12

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `exam_arrangement`
--

DROP TABLE IF EXISTS `exam_arrangement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `exam_arrangement` (
  `exam_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '考试标题',
  `paper_id` int(11) NOT NULL COMMENT '关联试卷ID',
  `target_classes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标班级ID集合(逗号分隔)',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT '状态: pending/running/finished',
  `create_by` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`exam_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='考试安排任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exam_arrangement`
--

LOCK TABLES `exam_arrangement` WRITE;
/*!40000 ALTER TABLE `exam_arrangement` DISABLE KEYS */;
INSERT INTO `exam_arrangement` VALUES (101,'2023-2024 第一学期《JavaWeb》期中考试 (正在进行)',1,'1,2','2026-04-17 20:35:36','2026-04-17 22:34:36','finished',2,'2026-04-15 18:26:30'),(102,'2023-2024 第一学期《高等数学》历史测试 (已结束)',2,'1','2022-10-01 09:00:00','2022-10-01 11:00:00','finished',2,'2026-04-15 18:26:30'),(103,'实例考试',3,'软件1班','2026-04-15 00:00:00','2026-04-15 02:00:00','pending',2,'2026-04-15 18:30:56'),(104,'test exam',4,'软件1班','2026-04-17 20:56:24','2026-04-17 22:55:24','finished',2,'2026-04-16 00:31:07'),(105,'高等数学自动组卷验证-二次验证',5,'软件1班','2026-04-17 21:05:44','2026-04-17 23:04:44','finished',2,'2026-04-16 00:59:20'),(106,'matn test 1',6,'软件1班','2026-04-17 21:05:42','2026-04-17 23:04:42','finished',2,'2026-04-16 15:16:03'),(107,'2025-2026 学年第二学期 高数测试1',7,'4','2026-05-08 14:10:00','2026-05-08 15:00:00','pending',7,'2026-05-08 14:07:12'),(108,'2025-2026 学年第二学期 测试2',8,'1,2,3','2026-05-09 15:25:00','2026-05-09 16:00:00','pending',2,'2026-05-09 15:26:56'),(109,'2025-2026 学年第二学期 测试3',9,'1,2,3','2026-05-09 00:00:00','2026-06-10 00:00:00','pending',2,'2026-05-09 16:07:12'),(110,'2025-2026 学年第二学期 测试4',10,'1,2,3','2026-05-09 00:00:00','2026-06-09 00:00:00','pending',2,'2026-05-09 20:55:05');
/*!40000 ALTER TABLE `exam_arrangement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paper_question_rel`
--

DROP TABLE IF EXISTS `paper_question_rel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `paper_question_rel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `paper_id` int(11) NOT NULL,
  `question_id` int(11) NOT NULL,
  `score` int(11) NOT NULL COMMENT '该题在一卷中的分值',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `fk_rel_paper` (`paper_id`) USING BTREE,
  KEY `fk_rel_question` (`question_id`) USING BTREE,
  CONSTRAINT `fk_rel_paper` FOREIGN KEY (`paper_id`) REFERENCES `test_paper` (`paper_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_rel_question` FOREIGN KEY (`question_id`) REFERENCES `question_bank` (`question_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='试卷-题目关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paper_question_rel`
--

LOCK TABLES `paper_question_rel` WRITE;
/*!40000 ALTER TABLE `paper_question_rel` DISABLE KEYS */;
INSERT INTO `paper_question_rel` VALUES (1,1,1,30,1),(2,1,2,30,2),(3,1,3,40,3),(4,2,4,50,1),(5,2,5,50,2),(6,3,5,5,1),(7,3,4,5,16),(8,4,5,5,1),(9,4,4,5,16),(10,5,5,5,1),(11,5,7,5,2),(12,5,8,5,3),(13,5,9,5,4),(14,5,10,5,5),(15,5,12,5,6),(16,5,13,5,7),(17,5,4,5,8),(18,5,14,5,9),(19,5,15,5,10),(20,5,17,10,11),(21,6,5,5,1),(22,6,7,5,2),(23,6,8,5,3),(24,6,9,5,4),(25,6,10,5,5),(26,6,14,5,6),(27,6,15,5,7),(28,6,13,5,8),(29,6,12,5,9),(30,6,17,10,10),(31,6,16,5,11),(32,7,5,5,1),(33,7,7,5,2),(34,7,8,5,3),(35,7,9,5,4),(36,7,10,5,5),(37,7,12,5,6),(38,7,13,5,7),(39,7,4,5,8),(40,7,14,5,9),(41,7,15,5,10),(42,7,17,10,11),(43,8,1,5,1),(44,8,2,5,2),(45,8,29,5,3),(46,8,30,5,4),(47,8,31,5,5),(48,8,3,5,6),(49,8,34,5,7),(50,8,35,5,8),(51,8,36,5,9),(52,8,37,5,10),(53,8,38,10,11),(54,9,1,8,1),(55,9,2,8,2),(56,9,29,8,3),(57,9,30,8,4),(58,9,31,8,5),(59,9,3,13,6),(60,9,34,12,7),(61,9,35,7,8),(62,9,36,7,9),(63,9,37,6,10),(64,9,38,15,11),(65,10,61,13,1),(66,10,30,9,2),(67,10,3,13,3),(68,10,37,7,4),(69,10,38,17,5),(70,10,58,7,6),(71,10,29,9,7),(72,10,55,9,8),(73,10,32,8,9),(74,10,31,8,10);
/*!40000 ALTER TABLE `paper_question_rel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_bank`
--

DROP TABLE IF EXISTS `question_bank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `question_bank` (
  `question_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学科/科目',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题型: 单选题/多选题/判断题/填空题/简答题',
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题干',
  `options` json DEFAULT NULL COMMENT '选项(JSON格式)',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标准答案',
  `difficulty` tinyint(4) DEFAULT '3' COMMENT '难度系数1-5',
  `create_by` int(11) DEFAULT NULL COMMENT '创建教师ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='题库表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_bank`
--

LOCK TABLES `question_bank` WRITE;
/*!40000 ALTER TABLE `question_bank` DISABLE KEYS */;
INSERT INTO `question_bank` VALUES (1,'JavaWeb','单选题','在Servlet的生命周期中，用于系统初始化的方法是？','[\"init()\", \"service()\", \"destroy()\", \"doGet()\"]','A',2,2,'2026-04-15 18:26:30'),(2,'JavaWeb','单选题','在下面选项中，哪个不属于JSP页面的内建对象？','[\"request\", \"response\", \"session\", \"window\"]','D',3,2,'2026-04-15 18:26:30'),(3,'JavaWeb','多选题','以下属于Spring框架核心设计特性的有：','[\"IoC 控制反转\", \"AOP 面向切面编程\", \"MVC 分层架构\", \"Hibernate 对象映射\"]','A,B',4,2,'2026-04-15 18:26:30'),(4,'高等数学','判断题','函数 f(x)=|x| 在 x=0 处是可导的（对或错）。','[\"正确\", \"错误\"]','错误',2,2,'2026-04-15 18:26:30'),(5,'高等数学','单选题','求极限 lim(x->0) (sinx / x) 的计算结果为？','[\"0\", \"1\", \"无穷大\", \"不存在\"]','B',3,2,'2026-04-15 18:26:30'),(6,'大学英语','单选题','It is raining cats and ___, stay at home today.','[\"dogs\", \"mice\", \"birds\", \"pigs\"]','A',1,2,'2026-04-15 18:26:30'),(7,'高等数学','单选题','函数y=x^2在x=1处的导数值是？','[\"1\", \"2\", \"3\", \"4\"]','B',2,2,'2026-04-16 00:48:31'),(8,'高等数学','单选题','不定积分∫2x dx的结果是？','[\"x^2+C\", \"2x+C\", \"x+C\", \"4x+C\"]','A',2,2,'2026-04-16 00:48:31'),(9,'高等数学','单选题','定积分表示曲边梯形面积时，积分上下限对应的是？','[\"函数值范围\", \"自变量区间\", \"导数区间\", \"极值点个数\"]','B',3,2,'2026-04-16 00:48:31'),(10,'高等数学','单选题','级数1+1/2+1/4+...的和收敛于？','[\"1\", \"2\", \"3\", \"发散\"]','B',3,2,'2026-04-16 00:48:31'),(11,'高等数学','单选题','矩阵乘法满足下列哪一性质？','[\"交换律总成立\", \"结合律成立\", \"任意可逆\", \"结果维度不变\"]','B',4,2,'2026-04-16 00:48:31'),(12,'高等数学','多选题','下列哪些函数是奇函数？','[\"x^3\", \"sinx\", \"cosx\", \"tanx\"]','A,B,D',3,2,'2026-04-16 00:48:31'),(13,'高等数学','多选题','下列哪些方法可用于求极限？','[\"洛必达法则\", \"等价无穷小替换\", \"配方法\", \"夹逼准则\"]','A,B,D',4,2,'2026-04-16 00:48:31'),(14,'高等数学','判断题','若函数在某点可导，则该点一定连续。','[\"正确\", \"错误\"]','正确',1,2,'2026-04-16 00:48:31'),(15,'高等数学','判断题','定积分的值一定是正数。','[\"正确\", \"错误\"]','错误',2,2,'2026-04-16 00:48:31'),(16,'高等数学','判断题','向量数量积结果是一个标量。','[\"正确\", \"错误\"]','正确',1,2,'2026-04-16 00:48:31'),(17,'高等数学','简答题','说明导数的几何意义。',NULL,'导数表示函数图像在该点切线的斜率。',3,2,'2026-04-16 00:48:31'),(18,'大学英语','单选题','Choose the correct word: She ___ to school by bus every day.','[\"go\", \"goes\", \"gone\", \"going\"]','B',1,2,'2026-04-16 00:48:31'),(19,'大学英语','单选题','Which word is closest in meaning to rapid?','[\"slow\", \"fast\", \"quiet\", \"late\"]','B',2,2,'2026-04-16 00:48:31'),(20,'大学英语','单选题','We have lived here ___ 2018.','[\"for\", \"since\", \"from\", \"at\"]','B',2,2,'2026-04-16 00:48:31'),(21,'大学英语','单选题','The meeting was canceled ___ the heavy rain.','[\"because of\", \"instead of\", \"according to\", \"such as\"]','A',2,2,'2026-04-16 00:48:31'),(22,'大学英语','单选题','If I ___ enough time, I will finish the report.','[\"have\", \"had\", \"will have\", \"having\"]','A',3,2,'2026-04-16 00:48:31'),(23,'大学英语','多选题','Which of the following are countable nouns?','[\"book\", \"water\", \"idea\", \"apple\"]','A,C,D',2,2,'2026-04-16 00:48:31'),(24,'大学英语','多选题','Which sentences are in the present perfect tense?','[\"I have finished my homework.\", \"She is reading now.\", \"They have gone home.\", \"We played football yesterday.\"]','A,C',3,2,'2026-04-16 00:48:32'),(25,'大学英语','判断题','The word information is an uncountable noun.','[\"正确\", \"错误\"]','正确',1,2,'2026-04-16 00:48:32'),(26,'大学英语','判断题','In English, every sentence must contain a verb.','[\"正确\", \"错误\"]','正确',1,2,'2026-04-16 00:48:32'),(27,'大学英语','判断题','The comparative form of good is gooder.','[\"正确\", \"错误\"]','错误',1,2,'2026-04-16 00:48:32'),(28,'大学英语','简答题','Write two sentences to describe your college life.',NULL,'答案开放，语法正确且表达完整即可。',3,2,'2026-04-16 00:48:32'),(29,'JavaWeb','单选题','HTTP协议默认使用的端口号是？','[\"21\", \"80\", \"3306\", \"8080\"]','B',1,2,'2026-04-16 00:48:32'),(30,'JavaWeb','单选题','在Spring MVC中，负责接收请求并分发的核心组件是？','[\"DispatcherServlet\", \"Filter\", \"Listener\", \"ViewResolver\"]','A',3,2,'2026-04-16 00:48:32'),(31,'JavaWeb','单选题','Session通常保存在？','[\"客户端浏览器\", \"服务器端\", \"数据库中固定表\", \"DNS服务器\"]','B',2,2,'2026-04-16 00:48:32'),(32,'JavaWeb','单选题','RESTful风格中，用于删除资源的HTTP方法通常是？','[\"GET\", \"POST\", \"PUT\", \"DELETE\"]','D',2,2,'2026-04-16 00:48:32'),(33,'JavaWeb','单选题','MyBatis中用于防止SQL注入并进行预编译的占位符是？','[\"${}\", \"#{}\", \"@{}\", \"%{}\"]','B',3,2,'2026-04-16 00:48:32'),(34,'JavaWeb','多选题','下列哪些属于常见的会话跟踪技术？','[\"Cookie\", \"Session\", \"URL重写\", \"DNS缓存\"]','A,B,C',3,2,'2026-04-16 00:48:32'),(35,'JavaWeb','判断题','GET请求适合传输大量敏感数据。','[\"正确\", \"错误\"]','错误',1,2,'2026-04-16 00:48:32'),(36,'JavaWeb','判断题','Filter可以在请求到达Servlet前进行处理。','[\"正确\", \"错误\"]','正确',2,2,'2026-04-16 00:48:32'),(37,'JavaWeb','判断题','前后端分离项目中，后端通常返回JSON数据。','[\"正确\", \"错误\"]','正确',1,2,'2026-04-16 00:48:32'),(38,'JavaWeb','简答题','简述Cookie与Session的区别。',NULL,'Cookie保存在客户端，Session保存在服务端，二者常配合实现会话管理。',3,2,'2026-04-16 00:48:32'),(39,'数据结构','单选题','下列结构中，先进后出的是？','[\"队列\", \"栈\", \"数组\", \"链表\"]','B',1,2,'2026-04-16 00:48:32'),(40,'数据结构','单选题','二叉树的前序遍历顺序是？','[\"根左右\", \"左根右\", \"左右根\", \"右左根\"]','A',2,2,'2026-04-16 00:48:32'),(41,'数据结构','多选题','下列哪些属于线性结构？','[\"数组\", \"链表\", \"栈\", \"二叉树\"]','A,B,C',2,2,'2026-04-16 00:48:32'),(42,'数据结构','判断题','快速排序在最坏情况下时间复杂度为O(n^2)。','[\"正确\", \"错误\"]','正确',3,2,'2026-04-16 00:48:32'),(43,'数据结构','简答题','简述队列和栈的主要区别。',NULL,'队列先进先出，栈后进先出。',2,2,'2026-04-16 00:48:32'),(44,'高等数学','单选题','1+1=','[\"1\", \"2\", \"3\", \"4\"]','B',3,7,'2026-05-08 14:05:36'),(55,'JavaWeb','单选题','编译Java Application 源程序文件将产生相应的字节码文件，这些字节码文件的扩展名为(  )。','[\"java\", \".class\", \"html\", \".exe\"]','B',1,2,'2026-05-09 15:30:21'),(56,'JavaWeb','单选题','设 x = 1 , y = 2 , z = 3，则表达式 y＋＝z－－/＋＋x 的值是(  )','[\"3\", \"3.5\", \"4\", \"5\"]','A',3,2,'2026-05-09 15:30:21'),(57,'JavaWeb','单选题','不允许作为类及类成员的访问控制符的是( )','[\"public\", \"private\", \"static\", \"protected\"]','C',3,2,'2026-05-09 15:30:21'),(58,'JavaWeb','判断题','java语言中不用区分字母的大写小写','[\"正确\", \"错误\"]','错误',1,2,'2026-05-09 15:30:21'),(59,'JavaWeb','判断题','Java的字符类型采用的是Unicode编码，每个Unicode码占16个比特。','[\"正确\", \"错误\"]','正确',5,2,'2026-05-09 15:30:22'),(60,'JavaWeb','多选题','不能用来修饰interface的有（）','[\"private\", \"public\", \"protected\", \"static\"]','A,C,D',3,2,'2026-05-09 15:30:22'),(61,'JavaWeb','多选题','如下哪些不是java的关键字？（ ）','[\"const\", \"NULL\", \"false\", \"this\", \"native\"]','B,C',1,2,'2026-05-09 15:30:22'),(62,'JavaWeb','填空题','若x = 5，y = 10，则x < y和x >= y的逻辑值分别为___和___。',NULL,'true/false',3,2,'2026-05-09 15:30:22'),(63,'JavaWeb','填空题','设 x = 2 ，则表达式 ( x + + )／3 的值是_______。',NULL,'0',5,2,'2026-05-09 15:30:22'),(64,'JavaWeb','简答题','用Java Application编写一个程序，输出 Hello! I love JAVA.',NULL,'',5,2,'2026-05-09 15:30:22');
/*!40000 ALTER TABLE `question_bank` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_answer_detail`
--

DROP TABLE IF EXISTS `student_answer_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `student_answer_detail` (
  `detail_id` int(11) NOT NULL AUTO_INCREMENT,
  `record_id` int(11) NOT NULL,
  `question_id` int(11) NOT NULL,
  `answer_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `is_correct` tinyint(1) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  PRIMARY KEY (`detail_id`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生答题明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_answer_detail`
--

LOCK TABLES `student_answer_detail` WRITE;
/*!40000 ALTER TABLE `student_answer_detail` DISABLE KEYS */;
INSERT INTO `student_answer_detail` VALUES (1,1,4,'错误',1,50),(2,1,5,'D',0,0),(3,2,5,'',0,0),(4,2,7,'',0,0),(5,2,8,'',0,0),(6,2,10,'',0,0),(7,2,12,'',NULL,0),(8,2,13,'',NULL,0),(9,3,4,'B',0,0),(10,3,5,'C',0,0),(11,3,7,'C',0,0),(12,3,8,'C',0,0),(13,3,9,'B',1,5),(14,3,10,'B',1,5),(15,3,12,'A,B',NULL,0),(16,3,13,'A,B',NULL,0),(17,3,14,'A',0,0),(18,3,15,'B',0,0),(19,5,7,'A',0,0),(20,6,4,'正确',0,0),(21,6,5,'B',1,5),(22,7,1,'A',1,30),(23,7,2,'A',0,0),(24,7,3,'C,B',0,0),(25,9,5,'A',0,0),(26,9,7,'A',0,0),(27,9,8,'A',1,5),(28,9,9,'A',0,0),(29,9,10,'B',1,5),(30,9,12,'C',0,0),(31,9,13,'A,B',0,0),(32,9,14,'错误',0,0),(33,9,15,'错误',1,5),(34,9,17,'',0,0),(35,2,17,'',NULL,0),(36,3,17,'',NULL,0),(37,5,17,'',NULL,0),(38,10,5,'B',1,5),(39,10,7,'A',0,0),(40,10,8,'C',0,0),(41,10,9,'B',1,5),(42,10,10,'B',1,5),(43,10,12,'B,A',0,0),(44,10,13,'A,D',0,0),(45,10,4,'错误',1,5),(46,10,14,'错误',0,0),(47,10,15,'错误',1,5),(48,10,17,'导数的几何意义是。。。。',1,6),(49,11,1,'A',1,5),(50,11,2,'C',0,0),(51,11,29,'B',1,5),(52,11,30,'B',0,0),(53,11,31,'A',0,0),(54,11,3,'B,C',0,0),(55,11,34,'A,B',0,0),(56,11,35,'错误',1,5),(57,11,36,'正确',1,5),(58,11,37,'正确',1,5),(59,11,38,'区别是',1,5),(60,12,1,'A',1,8),(61,12,2,'D',1,8),(62,12,29,'B',1,8),(63,12,30,'A',1,8),(64,12,31,'B',1,8),(65,12,3,'B,C',0,0),(66,12,34,'A,B',0,0),(67,12,35,'错误',1,7),(68,12,36,'正确',1,7),(69,12,37,'正确',1,6),(70,12,38,'区别是',1,10),(71,13,1,'A',1,8),(72,13,2,'D',1,8),(73,13,29,'B',1,8),(74,13,30,'A',1,8),(75,13,31,'B',1,8),(76,13,3,'B,C',0,0),(77,13,34,'A,B',0,0),(78,13,35,'错误',1,7),(79,13,36,'正确',1,7),(80,13,37,'正确',1,6),(81,13,38,'区别是',1,5),(82,14,61,'E',0,0),(83,14,30,'A',1,9),(84,14,3,'B,C',0,0),(85,14,37,'正确',1,7),(86,14,38,'区别是，，，',1,8),(87,14,58,'错误',1,7),(88,14,29,'B',1,9),(89,14,55,'B',1,9),(90,14,32,'D',1,8),(91,14,31,'B',1,8);
/*!40000 ALTER TABLE `student_answer_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_exam_record`
--

DROP TABLE IF EXISTS `student_exam_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `student_exam_record` (
  `record_id` int(11) NOT NULL AUTO_INCREMENT,
  `exam_id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `total_score` int(11) DEFAULT NULL,
  `objective_score` int(11) DEFAULT NULL,
  `subjective_score` int(11) DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生考试记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_exam_record`
--

LOCK TABLES `student_exam_record` WRITE;
/*!40000 ALTER TABLE `student_exam_record` DISABLE KEYS */;
INSERT INTO `student_exam_record` VALUES (1,102,3,50,50,0,'finished'),(2,105,3,0,0,0,'finished'),(3,105,3,10,10,0,'finished'),(5,105,3,0,0,0,'finished'),(6,104,3,5,5,0,'finished'),(7,101,3,30,30,NULL,'grading'),(8,103,3,0,0,0,'abnormal'),(9,106,3,15,15,0,'finished'),(10,107,9,31,25,6,'finished'),(11,108,3,30,25,5,'finished'),(12,109,3,70,60,10,'finished'),(13,109,8,65,60,5,'finished'),(14,110,3,65,57,8,'finished');
/*!40000 ALTER TABLE `student_exam_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_announcement`
--

DROP TABLE IF EXISTS `sys_announcement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `sys_announcement` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_top` tinyint(1) DEFAULT '0',
  `create_by` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_announcement`
--

LOCK TABLES `sys_announcement` WRITE;
/*!40000 ALTER TABLE `sys_announcement` DISABLE KEYS */;
INSERT INTO `sys_announcement` VALUES (1,'2023-2024学年第一学期考试纪律最新通知','各位同学请注意，所有期中考试与期末考试均引入防作弊机制（切屏监控及面部识别检测），请务必遵守考试纪律，诚信考试！',1,1,'2023-11-01 10:00:00'),(2,'系统停机升级维护通知','本系统将于本周五晚22:00进行性能扩容升级，期间预计停机2个小时，部分同学可能无法登录，请知悉。',0,1,'2023-11-05 14:00:00'),(3,'系统测试（请忽略）','正在进行系统测试',1,1,'2026-04-30 17:28:35');
/*!40000 ALTER TABLE `sys_announcement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_class`
--

DROP TABLE IF EXISTS `sys_class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `sys_class` (
  `class_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '班级名称',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属专业',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属院系',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`class_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='班级信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_class`
--

LOCK TABLES `sys_class` WRITE;
/*!40000 ALTER TABLE `sys_class` DISABLE KEYS */;
INSERT INTO `sys_class` VALUES (1,'软件工程1班','软件工程','计算机与信息学院','2023-09-01 00:00:00'),(2,'软件工程2班','软件工程','计算机与信息学院','2023-09-01 00:00:00'),(3,'计科1班','计算机科学与技术','计算机与信息学院','2023-09-01 08:00:00'),(4,'数学1班','数学与应用数学','理学院','2023-09-01 08:00:00');
/*!40000 ALTER TABLE `sys_class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `sys_user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号(学号/工号)',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(BCrypt加密)',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色: admin, teacher, student',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系方式',
  `class_id` int(11) DEFAULT NULL COMMENT '所属班级ID(外键)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态: 1正常 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE,
  KEY `fk_user_class` (`class_id`) USING BTREE,
  CONSTRAINT `fk_user_class` FOREIGN KEY (`class_id`) REFERENCES `sys_class` (`class_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$OyP0.ILkyLVkgqLGf5U3kOLO3ConUoaupXx9qJeQ761QEqhI/lk0W','超级管','admin','13800138000',NULL,1,'2023-09-01 00:00:00'),(2,'teacher1','$2a$10$RF5rcyYM59nrGMVTINd0y.o/.jzR8ku.NoY0IyWeGRVNOtId0/rh.','李老师','teacher','13900139000',1,1,'2023-09-01 00:00:00'),(3,'student1','$2a$10$I.3zIC7.FnfH7PVum.JftOpQBv5JqdDHLGFuVJ46w5Mf1yFaVw9Gy','张同学','student','13400050060',1,1,'2023-09-01 00:00:00'),(4,'2026001001001','$2a$10$PtB0j5QolmqmFb/qHgX1i.omYTD3.oqmY8crAlkc63JQUiMhCEItC','jack','student','10001111100',1,1,'2026-04-16 16:13:47'),(5,'2026001001003','$2a$10$h84PwCVXVjdHgSA5YDSSXuiam7XEU7hzm3tkWoHnfu8Slcj/SwBsu','lisa','student',NULL,3,1,'2026-04-17 02:01:51'),(6,'2026001001010','$2a$10$eP0WFoZqBLcNXdQbKKCJpOfbZ.oUmaUiJW3KAony/CGnKYvnIPOfy','测试注册同学','student',NULL,3,0,'2026-04-17 14:26:33'),(7,'th002','$2a$10$NmiQn4L.oAcx.uzOvw5rGuBayk5TcXMYO/SkadEIPN4Fh0LvquRRm','李四','teacher','',4,1,'2026-04-30 16:21:33'),(8,'2023200300400','$2a$10$VUWX04frKPCr3RdwlBLivewo7oN1Hs2eYBMuOYRzPZF5UG1Zi8vQ6','王五','student',NULL,3,1,'2026-05-08 13:21:20'),(9,'2023100200300','$2a$10$ZJNS1ZruKE/E/bn.LMLrLOOLSxKG7jEuMNwejBys6N7k6d7Yk1IQC','张六','student','',4,1,'2026-05-08 14:09:02'),(10,'admin1','$2a$10$N9ZJYR64FokeqBdMoJnFF.hNwOw4THcyN7jy7Bd/3cftuvJ23wh02','王六','admin','',NULL,1,'2026-05-08 14:43:25');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_paper`
--

DROP TABLE IF EXISTS `test_paper`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `test_paper` (
  `paper_id` int(11) NOT NULL AUTO_INCREMENT,
  `paper_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '试卷名称',
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对应科目',
  `total_score` int(11) NOT NULL DEFAULT '100' COMMENT '总分',
  `pass_score` int(11) NOT NULL DEFAULT '60' COMMENT '及格线',
  `duration` int(11) NOT NULL DEFAULT '120' COMMENT '考试时长(分钟)',
  `create_by` int(11) DEFAULT NULL COMMENT '创建教师ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`paper_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='试卷模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_paper`
--

LOCK TABLES `test_paper` WRITE;
/*!40000 ALTER TABLE `test_paper` DISABLE KEYS */;
INSERT INTO `test_paper` VALUES (1,'2023-2024学年第一学期《JavaWeb》期中测验卷','JavaWeb',100,60,120,2,'2026-04-15 18:26:30'),(2,'2023-2024学年第一学期《高等数学》历史摸底测试','高等数学',100,60,60,2,'2026-04-15 18:26:30'),(3,'实例考试 试卷','高等数学',100,60,120,2,'2026-04-15 18:30:56'),(4,'test exam 试卷','高等数学',100,60,43320,2,'2026-04-16 00:31:07'),(5,'高等数学自动组卷验证-二次验证 试卷','高等数学',100,60,43320,2,'2026-04-16 00:59:20'),(6,'matn test 1 试卷','高等数学',100,60,43320,2,'2026-04-16 15:16:03'),(7,'2025-2026 学年第二学期 高数测试1 试卷','高等数学',100,60,50,7,'2026-05-08 14:07:12'),(8,'2025-2026 学年第二学期 测试2 试卷','JavaWeb',100,60,35,2,'2026-05-09 15:26:56'),(9,'2025-2026 学年第二学期 测试3 试卷','JavaWeb',100,60,46080,2,'2026-05-09 16:07:12'),(10,'2025-2026 学年第二学期 测试4 试卷','JavaWeb',100,60,44640,2,'2026-05-09 20:55:05');
/*!40000 ALTER TABLE `test_paper` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wrong_question_book`
--

DROP TABLE IF EXISTS `wrong_question_book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `wrong_question_book` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `student_id` int(11) NOT NULL,
  `question_id` int(11) NOT NULL,
  `source_exam_id` int(11) DEFAULT NULL,
  `error_count` int(11) DEFAULT '1',
  `last_error_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='错题本';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wrong_question_book`
--

LOCK TABLES `wrong_question_book` WRITE;
/*!40000 ALTER TABLE `wrong_question_book` DISABLE KEYS */;
INSERT INTO `wrong_question_book` VALUES (1,3,5,102,4,'2026-04-16 15:19:08'),(4,3,10,105,1,'2026-04-16 01:09:13'),(5,3,4,105,2,'2026-04-16 10:44:03'),(7,3,15,105,1,'2026-04-16 01:21:19'),(8,3,2,101,2,'2026-05-09 15:28:43'),(9,3,3,101,4,'2026-05-09 20:56:30'),(10,3,7,106,1,'2026-04-16 15:19:08'),(11,3,9,106,1,'2026-04-16 15:19:08'),(12,3,12,106,1,'2026-04-16 15:19:08'),(13,3,13,106,1,'2026-04-16 15:19:08'),(14,3,14,106,1,'2026-04-16 15:19:08'),(15,9,7,107,1,'2026-05-08 14:13:47'),(16,9,8,107,1,'2026-05-08 14:13:47'),(17,9,12,107,1,'2026-05-08 14:13:47'),(18,9,13,107,1,'2026-05-08 14:13:47'),(19,9,14,107,1,'2026-05-08 14:13:47'),(20,3,30,108,1,'2026-05-09 15:28:43'),(21,3,31,108,1,'2026-05-09 15:28:43'),(22,3,34,108,2,'2026-05-09 16:08:37'),(23,8,3,109,1,'2026-05-09 16:22:21'),(24,8,34,109,1,'2026-05-09 16:22:21'),(25,3,61,110,1,'2026-05-09 20:56:30');
/*!40000 ALTER TABLE `wrong_question_book` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-09 21:08:55
