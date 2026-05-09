-- MySQL dump 10.13  Distrib 8.0.12, for Win64 (x86_64)
--
-- Host: localhost    Database: online_exam
-- ------------------------------------------------------
-- Server version	8.0.12

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8mb4 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `online_exam`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `online_exam` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;

USE `online_exam`;

--
-- Table structure for table `exam_arrangement`
--

DROP TABLE IF EXISTS `exam_arrangement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `exam_arrangement` (
  `exam_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鑰冭瘯鏍囬',
  `paper_id` int(11) NOT NULL COMMENT '鍏宠仈璇曞嵎ID',
  `target_classes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐩爣鐝骇ID闆嗗悎(閫楀彿鍒嗛殧)',
  `start_time` datetime NOT NULL COMMENT '寮€濮嬫椂闂?,
  `end_time` datetime NOT NULL COMMENT '缁撴潫鏃堕棿',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT '鐘舵€? pending/running/finished',
  `create_by` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`exam_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='鑰冭瘯瀹夋帓浠诲姟琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exam_arrangement`
--

LOCK TABLES `exam_arrangement` WRITE;
/*!40000 ALTER TABLE `exam_arrangement` DISABLE KEYS */;
INSERT INTO `exam_arrangement` VALUES (101,'2023-2024 绗竴瀛︽湡銆奐avaWeb銆嬫湡涓€冭瘯 (姝ｅ湪杩涜)',1,'1,2','1990-01-01 00:00:00','2099-12-31 23:59:59','running',2,'2026-04-15 18:26:30'),(102,'2023-2024 绗竴瀛︽湡銆婇珮绛夋暟瀛︺€嬪巻鍙叉祴璇?(宸茬粨鏉?',2,'1','2022-10-01 09:00:00','2022-10-01 11:00:00','finished',2,'2026-04-15 18:26:30'),(103,'瀹炰緥鑰冭瘯',3,'杞欢1鐝?,'2026-04-15 00:00:00','2026-04-15 02:00:00','pending',2,'2026-04-15 18:30:56'),(104,'test exam',4,'杞欢1鐝?,'2026-04-16 00:00:00','2026-05-16 02:00:00','cancelled',2,'2026-04-16 00:31:07'),(105,'楂樼瓑鏁板鑷姩缁勫嵎楠岃瘉-浜屾楠岃瘉',5,'杞欢1鐝?,'2026-04-16 11:00:00','2026-05-16 13:00:00','pending',2,'2026-04-16 00:59:20'),(106,'matn test 1',6,'杞欢1鐝?,'2026-04-16 15:00:00','2026-05-16 17:00:00','pending',2,'2026-04-16 15:16:03');
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
  `score` int(11) NOT NULL COMMENT '璇ラ鍦ㄤ竴鍗蜂腑鐨勫垎鍊?,
  `sort_order` int(11) DEFAULT '0' COMMENT '鎺掑簭鍙?,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `fk_rel_paper` (`paper_id`) USING BTREE,
  KEY `fk_rel_question` (`question_id`) USING BTREE,
  CONSTRAINT `fk_rel_paper` FOREIGN KEY (`paper_id`) REFERENCES `test_paper` (`paper_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_rel_question` FOREIGN KEY (`question_id`) REFERENCES `question_bank` (`question_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='璇曞嵎-棰樼洰鍏宠仈琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paper_question_rel`
--

LOCK TABLES `paper_question_rel` WRITE;
/*!40000 ALTER TABLE `paper_question_rel` DISABLE KEYS */;
INSERT INTO `paper_question_rel` VALUES (1,1,1,30,1),(2,1,2,30,2),(3,1,3,40,3),(4,2,4,50,1),(5,2,5,50,2),(6,3,5,5,1),(7,3,4,5,16),(8,4,5,5,1),(9,4,4,5,16),(10,5,5,5,1),(11,5,7,5,2),(12,5,8,5,3),(13,5,9,5,4),(14,5,10,5,5),(15,5,12,5,6),(16,5,13,5,7),(17,5,4,5,8),(18,5,14,5,9),(19,5,15,5,10),(20,5,17,10,11),(21,6,5,5,1),(22,6,7,5,2),(23,6,8,5,3),(24,6,9,5,4),(25,6,10,5,5),(26,6,14,5,6),(27,6,15,5,7),(28,6,13,5,8),(29,6,12,5,9),(30,6,17,10,10),(31,6,16,5,11);
/*!40000 ALTER TABLE `paper_question_rel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_bank`
--

DROP TABLE IF EXISTS `question_bank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `question_bank` (
  `question_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '棰樼洰ID',
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瀛︾/绉戠洰',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '棰樺瀷: 鍗曢€夐/澶氶€夐/鍒ゆ柇棰?濉┖棰?绠€绛旈',
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '棰樺共',
  `options` json DEFAULT NULL COMMENT '閫夐」(JSON鏍煎紡)',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏍囧噯绛旀',
  `difficulty` tinyint(4) DEFAULT '3' COMMENT '闅惧害绯绘暟1-5',
  `create_by` int(11) DEFAULT NULL COMMENT '鍒涘缓鏁欏笀ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='棰樺簱琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_bank`
--

LOCK TABLES `question_bank` WRITE;
/*!40000 ALTER TABLE `question_bank` DISABLE KEYS */;
INSERT INTO `question_bank` VALUES (1,'JavaWeb','鍗曢€夐','鍦⊿ervlet鐨勭敓鍛藉懆鏈熶腑锛岀敤浜庣郴缁熷垵濮嬪寲鐨勬柟娉曟槸锛?,'[\"init()\", \"service()\", \"destroy()\", \"doGet()\"]','A',2,2,'2026-04-15 18:26:30'),(2,'JavaWeb','鍗曢€夐','鍦ㄤ笅闈㈤€夐」涓紝鍝釜涓嶅睘浜嶫SP椤甸潰鐨勫唴寤哄璞★紵','[\"request\", \"response\", \"session\", \"window\"]','D',3,2,'2026-04-15 18:26:30'),(3,'JavaWeb','澶氶€夐','浠ヤ笅灞炰簬Spring妗嗘灦鏍稿績璁捐鐗规€х殑鏈夛細','[\"IoC 鎺у埗鍙嶈浆\", \"AOP 闈㈠悜鍒囬潰缂栫▼\", \"MVC 鍒嗗眰鏋舵瀯\", \"Hibernate 瀵硅薄鏄犲皠\"]','A,B',4,2,'2026-04-15 18:26:30'),(4,'楂樼瓑鏁板','鍒ゆ柇棰?,'鍑芥暟 f(x)=|x| 鍦?x=0 澶勬槸鍙鐨勶紙瀵规垨閿欙級銆?,'[\"姝ｇ‘\", \"閿欒\"]','閿欒',2,2,'2026-04-15 18:26:30'),(5,'楂樼瓑鏁板','鍗曢€夐','姹傛瀬闄?lim(x->0) (sinx / x) 鐨勮绠楃粨鏋滀负锛?,'[\"0\", \"1\", \"鏃犵┓澶", \"涓嶅瓨鍦╘"]','B',3,2,'2026-04-15 18:26:30'),(6,'澶у鑻辫','鍗曢€夐','It is raining cats and ___, stay at home today.','[\"dogs\", \"mice\", \"birds\", \"pigs\"]','A',1,2,'2026-04-15 18:26:30'),(7,'楂樼瓑鏁板','鍗曢€夐','鍑芥暟y=x^2鍦▁=1澶勭殑瀵兼暟鍊兼槸锛?,'[\"1\", \"2\", \"3\", \"4\"]','B',2,2,'2026-04-16 00:48:31'),(8,'楂樼瓑鏁板','鍗曢€夐','涓嶅畾绉垎鈭?x dx鐨勭粨鏋滄槸锛?,'[\"x^2+C\", \"2x+C\", \"x+C\", \"4x+C\"]','A',2,2,'2026-04-16 00:48:31'),(9,'楂樼瓑鏁板','鍗曢€夐','瀹氱Н鍒嗚〃绀烘洸杈规褰㈤潰绉椂锛岀Н鍒嗕笂涓嬮檺瀵瑰簲鐨勬槸锛?,'[\"鍑芥暟鍊艰寖鍥碶", \"鑷彉閲忓尯闂碶", \"瀵兼暟鍖洪棿\", \"鏋佸€肩偣涓暟\"]','B',3,2,'2026-04-16 00:48:31'),(10,'楂樼瓑鏁板','鍗曢€夐','绾ф暟1+1/2+1/4+...鐨勫拰鏀舵暃浜庯紵','[\"1\", \"2\", \"3\", \"鍙戞暎\"]','B',3,2,'2026-04-16 00:48:31'),(11,'楂樼瓑鏁板','鍗曢€夐','鐭╅樀涔樻硶婊¤冻涓嬪垪鍝竴鎬ц川锛?,'[\"浜ゆ崲寰嬫€绘垚绔媆", \"缁撳悎寰嬫垚绔媆", \"浠绘剰鍙€哱", \"缁撴灉缁村害涓嶅彉\"]','B',4,2,'2026-04-16 00:48:31'),(12,'楂樼瓑鏁板','澶氶€夐','涓嬪垪鍝簺鍑芥暟鏄鍑芥暟锛?,'[\"x^3\", \"sinx\", \"cosx\", \"tanx\"]','A,B,D',3,2,'2026-04-16 00:48:31'),(13,'楂樼瓑鏁板','澶氶€夐','涓嬪垪鍝簺鏂规硶鍙敤浜庢眰鏋侀檺锛?,'[\"娲涘繀杈炬硶鍒橽", \"绛変环鏃犵┓灏忔浛鎹", \"閰嶆柟娉昞", \"澶归€煎噯鍒橽"]','A,B,D',4,2,'2026-04-16 00:48:31'),(14,'楂樼瓑鏁板','鍒ゆ柇棰?,'鑻ュ嚱鏁板湪鏌愮偣鍙锛屽垯璇ョ偣涓€瀹氳繛缁€?,'[\"姝ｇ‘\", \"閿欒\"]','姝ｇ‘',1,2,'2026-04-16 00:48:31'),(15,'楂樼瓑鏁板','鍒ゆ柇棰?,'瀹氱Н鍒嗙殑鍊间竴瀹氭槸姝ｆ暟銆?,'[\"姝ｇ‘\", \"閿欒\"]','閿欒',2,2,'2026-04-16 00:48:31'),(16,'楂樼瓑鏁板','鍒ゆ柇棰?,'鍚戦噺鏁伴噺绉粨鏋滄槸涓€涓爣閲忋€?,'[\"姝ｇ‘\", \"閿欒\"]','姝ｇ‘',1,2,'2026-04-16 00:48:31'),(17,'楂樼瓑鏁板','绠€绛旈','璇存槑瀵兼暟鐨勫嚑浣曟剰涔夈€?,NULL,'瀵兼暟琛ㄧず鍑芥暟鍥惧儚鍦ㄨ鐐瑰垏绾跨殑鏂滅巼銆?,3,2,'2026-04-16 00:48:31'),(18,'澶у鑻辫','鍗曢€夐','Choose the correct word: She ___ to school by bus every day.','[\"go\", \"goes\", \"gone\", \"going\"]','B',1,2,'2026-04-16 00:48:31'),(19,'澶у鑻辫','鍗曢€夐','Which word is closest in meaning to rapid?','[\"slow\", \"fast\", \"quiet\", \"late\"]','B',2,2,'2026-04-16 00:48:31'),(20,'澶у鑻辫','鍗曢€夐','We have lived here ___ 2018.','[\"for\", \"since\", \"from\", \"at\"]','B',2,2,'2026-04-16 00:48:31'),(21,'澶у鑻辫','鍗曢€夐','The meeting was canceled ___ the heavy rain.','[\"because of\", \"instead of\", \"according to\", \"such as\"]','A',2,2,'2026-04-16 00:48:31'),(22,'澶у鑻辫','鍗曢€夐','If I ___ enough time, I will finish the report.','[\"have\", \"had\", \"will have\", \"having\"]','A',3,2,'2026-04-16 00:48:31'),(23,'澶у鑻辫','澶氶€夐','Which of the following are countable nouns?','[\"book\", \"water\", \"idea\", \"apple\"]','A,C,D',2,2,'2026-04-16 00:48:31'),(24,'澶у鑻辫','澶氶€夐','Which sentences are in the present perfect tense?','[\"I have finished my homework.\", \"She is reading now.\", \"They have gone home.\", \"We played football yesterday.\"]','A,C',3,2,'2026-04-16 00:48:32'),(25,'澶у鑻辫','鍒ゆ柇棰?,'The word information is an uncountable noun.','[\"姝ｇ‘\", \"閿欒\"]','姝ｇ‘',1,2,'2026-04-16 00:48:32'),(26,'澶у鑻辫','鍒ゆ柇棰?,'In English, every sentence must contain a verb.','[\"姝ｇ‘\", \"閿欒\"]','姝ｇ‘',1,2,'2026-04-16 00:48:32'),(27,'澶у鑻辫','鍒ゆ柇棰?,'The comparative form of good is gooder.','[\"姝ｇ‘\", \"閿欒\"]','閿欒',1,2,'2026-04-16 00:48:32'),(28,'澶у鑻辫','绠€绛旈','Write two sentences to describe your college life.',NULL,'绛旀寮€鏀撅紝璇硶姝ｇ‘涓旇〃杈惧畬鏁村嵆鍙€?,3,2,'2026-04-16 00:48:32'),(29,'JavaWeb','鍗曢€夐','HTTP鍗忚榛樿浣跨敤鐨勭鍙ｅ彿鏄紵','[\"21\", \"80\", \"3306\", \"8080\"]','B',1,2,'2026-04-16 00:48:32'),(30,'JavaWeb','鍗曢€夐','鍦⊿pring MVC涓紝璐熻矗鎺ユ敹璇锋眰骞跺垎鍙戠殑鏍稿績缁勪欢鏄紵','[\"DispatcherServlet\", \"Filter\", \"Listener\", \"ViewResolver\"]','A',3,2,'2026-04-16 00:48:32'),(31,'JavaWeb','鍗曢€夐','Session閫氬父淇濆瓨鍦紵','[\"瀹㈡埛绔祻瑙堝櫒\", \"鏈嶅姟鍣ㄧ\", \"鏁版嵁搴撲腑鍥哄畾琛╘", \"DNS鏈嶅姟鍣╘"]','B',2,2,'2026-04-16 00:48:32'),(32,'JavaWeb','鍗曢€夐','RESTful椋庢牸涓紝鐢ㄤ簬鍒犻櫎璧勬簮鐨凥TTP鏂规硶閫氬父鏄紵','[\"GET\", \"POST\", \"PUT\", \"DELETE\"]','D',2,2,'2026-04-16 00:48:32'),(33,'JavaWeb','鍗曢€夐','MyBatis涓敤浜庨槻姝QL娉ㄥ叆骞惰繘琛岄缂栬瘧鐨勫崰浣嶇鏄紵','[\"${}\", \"#{}\", \"@{}\", \"%{}\"]','B',3,2,'2026-04-16 00:48:32'),(34,'JavaWeb','澶氶€夐','涓嬪垪鍝簺灞炰簬甯歌鐨勪細璇濊窡韪妧鏈紵','[\"Cookie\", \"Session\", \"URL閲嶅啓\", \"DNS缂撳瓨\"]','A,B,C',3,2,'2026-04-16 00:48:32'),(35,'JavaWeb','鍒ゆ柇棰?,'GET璇锋眰閫傚悎浼犺緭澶ч噺鏁忔劅鏁版嵁銆?,'[\"姝ｇ‘\", \"閿欒\"]','閿欒',1,2,'2026-04-16 00:48:32'),(36,'JavaWeb','鍒ゆ柇棰?,'Filter鍙互鍦ㄨ姹傚埌杈維ervlet鍓嶈繘琛屽鐞嗐€?,'[\"姝ｇ‘\", \"閿欒\"]','姝ｇ‘',2,2,'2026-04-16 00:48:32'),(37,'JavaWeb','鍒ゆ柇棰?,'鍓嶅悗绔垎绂婚」鐩腑锛屽悗绔€氬父杩斿洖JSON鏁版嵁銆?,'[\"姝ｇ‘\", \"閿欒\"]','姝ｇ‘',1,2,'2026-04-16 00:48:32'),(38,'JavaWeb','绠€绛旈','绠€杩癈ookie涓嶴ession鐨勫尯鍒€?,NULL,'Cookie淇濆瓨鍦ㄥ鎴风锛孲ession淇濆瓨鍦ㄦ湇鍔＄锛屼簩鑰呭父閰嶅悎瀹炵幇浼氳瘽绠＄悊銆?,3,2,'2026-04-16 00:48:32'),(39,'鏁版嵁缁撴瀯','鍗曢€夐','涓嬪垪缁撴瀯涓紝鍏堣繘鍚庡嚭鐨勬槸锛?,'[\"闃熷垪\", \"鏍圽", \"鏁扮粍\", \"閾捐〃\"]','B',1,2,'2026-04-16 00:48:32'),(40,'鏁版嵁缁撴瀯','鍗曢€夐','浜屽弶鏍戠殑鍓嶅簭閬嶅巻椤哄簭鏄紵','[\"鏍瑰乏鍙砛", \"宸︽牴鍙砛", \"宸﹀彸鏍筡", \"鍙冲乏鏍筡"]','A',2,2,'2026-04-16 00:48:32'),(41,'鏁版嵁缁撴瀯','澶氶€夐','涓嬪垪鍝簺灞炰簬绾挎€х粨鏋勶紵','[\"鏁扮粍\", \"閾捐〃\", \"鏍圽", \"浜屽弶鏍慭"]','A,B,C',2,2,'2026-04-16 00:48:32'),(42,'鏁版嵁缁撴瀯','鍒ゆ柇棰?,'蹇€熸帓搴忓湪鏈€鍧忔儏鍐典笅鏃堕棿澶嶆潅搴︿负O(n^2)銆?,'[\"姝ｇ‘\", \"閿欒\"]','姝ｇ‘',3,2,'2026-04-16 00:48:32'),(43,'鏁版嵁缁撴瀯','绠€绛旈','绠€杩伴槦鍒楀拰鏍堢殑涓昏鍖哄埆銆?,NULL,'闃熷垪鍏堣繘鍏堝嚭锛屾爤鍚庤繘鍏堝嚭銆?,2,2,'2026-04-16 00:48:32');
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
  `answer_content` text COLLATE utf8mb4_unicode_ci,
  `is_correct` tinyint(1) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  PRIMARY KEY (`detail_id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='瀛︾敓绛旈鏄庣粏琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_answer_detail`
--

LOCK TABLES `student_answer_detail` WRITE;
/*!40000 ALTER TABLE `student_answer_detail` DISABLE KEYS */;
INSERT INTO `student_answer_detail` VALUES (1,1,4,'閿欒',1,50),(2,1,5,'D',0,0),(3,2,5,'',0,0),(4,2,7,'',0,0),(5,2,8,'',0,0),(6,2,10,'',0,0),(7,2,12,'',NULL,0),(8,2,13,'',NULL,0),(9,3,4,'B',0,0),(10,3,5,'C',0,0),(11,3,7,'C',0,0),(12,3,8,'C',0,0),(13,3,9,'B',1,5),(14,3,10,'B',1,5),(15,3,12,'A,B',NULL,0),(16,3,13,'A,B',NULL,0),(17,3,14,'A',0,0),(18,3,15,'B',0,0),(19,5,7,'A',0,0),(20,6,4,'姝ｇ‘',0,0),(21,6,5,'B',1,5),(22,7,1,'A',1,30),(23,7,2,'A',0,0),(24,7,3,'C,B',0,0),(25,9,5,'A',0,0),(26,9,7,'A',0,0),(27,9,8,'A',1,5),(28,9,9,'A',0,0),(29,9,10,'B',1,5),(30,9,12,'C',0,0),(31,9,13,'A,B',0,0),(32,9,14,'閿欒',0,0),(33,9,15,'閿欒',1,5),(34,9,17,'',0,0),(35,2,17,'',NULL,0),(36,3,17,'',NULL,0),(37,5,17,'',NULL,0);
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
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='瀛︾敓鑰冭瘯璁板綍琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_exam_record`
--

LOCK TABLES `student_exam_record` WRITE;
/*!40000 ALTER TABLE `student_exam_record` DISABLE KEYS */;
INSERT INTO `student_exam_record` VALUES (1,102,3,50,50,0,'finished'),(2,105,3,0,0,0,'finished'),(3,105,3,10,10,0,'finished'),(5,105,3,0,0,0,'finished'),(6,104,3,5,5,0,'finished'),(7,101,3,30,30,NULL,'grading'),(8,103,3,0,0,0,'abnormal'),(9,106,3,15,15,0,'finished');
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鍏憡琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_announcement`
--

LOCK TABLES `sys_announcement` WRITE;
/*!40000 ALTER TABLE `sys_announcement` DISABLE KEYS */;
INSERT INTO `sys_announcement` VALUES (1,'2023-2024瀛﹀勾绗竴瀛︽湡鑰冭瘯绾緥鏈€鏂伴€氱煡','鍚勪綅鍚屽璇锋敞鎰忥紝鎵€鏈夋湡涓€冭瘯涓庢湡鏈€冭瘯鍧囧紩鍏ラ槻浣滃紛鏈哄埗锛堝垏灞忕洃鎺у強闈㈤儴璇嗗埆妫€娴嬶級锛岃鍔″繀閬靛畧鑰冭瘯绾緥锛岃瘹淇¤€冭瘯锛?,1,1,'2023-11-01 10:00:00'),(2,'绯荤粺鍋滄満鍗囩骇缁存姢閫氱煡','鏈郴缁熷皢浜庢湰鍛ㄤ簲鏅?2:00杩涜鎬ц兘鎵╁鍗囩骇锛屾湡闂撮璁″仠鏈?涓皬鏃讹紝閮ㄥ垎鍚屽鍙兘鏃犳硶鐧诲綍锛岃鐭ユ倝銆?,0,1,'2023-11-05 14:00:00');
/*!40000 ALTER TABLE `sys_announcement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_class`
--

DROP TABLE IF EXISTS `sys_class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `sys_class` (
  `class_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '鐝骇ID',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐝骇鍚嶇О',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鎵€灞炰笓涓?,
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鎵€灞為櫌绯?,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`class_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='鐝骇淇℃伅琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_class`
--

LOCK TABLES `sys_class` WRITE;
/*!40000 ALTER TABLE `sys_class` DISABLE KEYS */;
INSERT INTO `sys_class` VALUES (1,'杞欢宸ョ▼1鐝?,'杞欢宸ョ▼','璁＄畻鏈轰笌淇℃伅瀛﹂櫌','2023-09-01 00:00:00'),(2,'杞欢宸ョ▼2鐝?,'杞欢宸ョ▼','璁＄畻鏈轰笌淇℃伅瀛﹂櫌','2023-09-01 00:00:00'),(3,'璁＄1鐝?,'璁＄畻鏈虹瀛︿笌鎶€鏈?,'璁＄畻鏈轰笌淇℃伅瀛﹂櫌','2023-09-01 08:00:00'),(4,'鏁板1鐝?,'鏁板涓庡簲鐢ㄦ暟瀛?,'鐞嗗闄?,'2023-09-01 08:00:00');
/*!40000 ALTER TABLE `sys_class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `sys_user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐧诲綍璐﹀彿(瀛﹀彿/宸ュ彿)',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瀵嗙爜(BCrypt鍔犲瘑)',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐪熷疄濮撳悕',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瑙掕壊: admin, teacher, student',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鑱旂郴鏂瑰紡',
  `class_id` int(11) DEFAULT NULL COMMENT '鎵€灞炵彮绾D(澶栭敭)',
  `status` tinyint(1) DEFAULT '1' COMMENT '鐘舵€? 1姝ｅ父 0绂佺敤',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE,
  KEY `fk_user_class` (`class_id`) USING BTREE,
  CONSTRAINT `fk_user_class` FOREIGN KEY (`class_id`) REFERENCES `sys_class` (`class_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='绯荤粺鐢ㄦ埛琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$7Q/.0Qj7YQQ0K2i3HnZ4e.2N.L/h0U/ZzD/x6xY25a58U8KFVpPoy','瓒呯骇绠?,'admin','13800138000',NULL,1,'2023-09-01 00:00:00'),(2,'teacher1','$2a$10$7Q/.0Qj7YQQ0K2i3HnZ4e.2N.L/h0U/ZzD/x6xY25a58U8KFVpPoy','鏉庤€佸笀','teacher','13900139000',1,1,'2023-09-01 00:00:00'),(3,'student1','$2a$10$7Q/.0Qj7YQQ0K2i3HnZ4e.2N.L/h0U/ZzD/x6xY25a58U8KFVpPoy','寮犲悓瀛?,'student','13700137000',1,1,'2023-09-01 00:00:00'),(4,'2026001001001','$2a$10$PtB0j5QolmqmFb/qHgX1i.omYTD3.oqmY8crAlkc63JQUiMhCEItC','jack','student','10001111100',1,1,'2026-04-16 16:13:47'),(5,'2026001001003','$2a$10$8vTiOMguLoUXxYS4ZjwrfuSmjJ0QhPR40XxBe73zf8Jul37B0ABLW','lisa','student',NULL,3,1,'2026-04-17 02:01:51');
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
  `paper_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '璇曞嵎鍚嶇О',
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瀵瑰簲绉戠洰',
  `total_score` int(11) NOT NULL DEFAULT '100' COMMENT '鎬诲垎',
  `pass_score` int(11) NOT NULL DEFAULT '60' COMMENT '鍙婃牸绾?,
  `duration` int(11) NOT NULL DEFAULT '120' COMMENT '鑰冭瘯鏃堕暱(鍒嗛挓)',
  `create_by` int(11) DEFAULT NULL COMMENT '鍒涘缓鏁欏笀ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`paper_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='璇曞嵎妯℃澘琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_paper`
--

LOCK TABLES `test_paper` WRITE;
/*!40000 ALTER TABLE `test_paper` DISABLE KEYS */;
INSERT INTO `test_paper` VALUES (1,'2023-2024瀛﹀勾绗竴瀛︽湡銆奐avaWeb銆嬫湡涓祴楠屽嵎','JavaWeb',100,60,120,2,'2026-04-15 18:26:30'),(2,'2023-2024瀛﹀勾绗竴瀛︽湡銆婇珮绛夋暟瀛︺€嬪巻鍙叉懜搴曟祴璇?,'楂樼瓑鏁板',100,60,60,2,'2026-04-15 18:26:30'),(3,'瀹炰緥鑰冭瘯 璇曞嵎','楂樼瓑鏁板',100,60,120,2,'2026-04-15 18:30:56'),(4,'test exam 璇曞嵎','楂樼瓑鏁板',100,60,43320,2,'2026-04-16 00:31:07'),(5,'楂樼瓑鏁板鑷姩缁勫嵎楠岃瘉-浜屾楠岃瘉 璇曞嵎','楂樼瓑鏁板',100,60,43320,2,'2026-04-16 00:59:20'),(6,'matn test 1 璇曞嵎','楂樼瓑鏁板',100,60,43320,2,'2026-04-16 15:16:03');
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
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閿欓鏈?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wrong_question_book`
--

LOCK TABLES `wrong_question_book` WRITE;
/*!40000 ALTER TABLE `wrong_question_book` DISABLE KEYS */;
INSERT INTO `wrong_question_book` VALUES (1,3,5,102,4,'2026-04-16 15:19:08'),(4,3,10,105,1,'2026-04-16 01:09:13'),(5,3,4,105,2,'2026-04-16 10:44:03'),(7,3,15,105,1,'2026-04-16 01:21:19'),(8,3,2,101,1,'2026-04-16 10:45:17'),(9,3,3,101,1,'2026-04-16 10:45:17'),(10,3,7,106,1,'2026-04-16 15:19:08'),(11,3,9,106,1,'2026-04-16 15:19:08'),(12,3,12,106,1,'2026-04-16 15:19:08'),(13,3,13,106,1,'2026-04-16 15:19:08'),(14,3,14,106,1,'2026-04-16 15:19:08');
/*!40000 ALTER TABLE `wrong_question_book` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'online_exam'
--

--
-- Dumping routines for database 'online_exam'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-17  2:20:50
