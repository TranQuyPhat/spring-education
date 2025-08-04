CREATE DATABASE  IF NOT EXISTS `schoolmanagementsystem` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `schoolmanagementsystem`;
-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: schoolmanagementsystem
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `assignment_comments`
--

DROP TABLE IF EXISTS `assignment_comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assignment_comments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `assignment_id` int NOT NULL,
  `user_id` int NOT NULL,
  `comment` tinytext NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `parent_id` int DEFAULT NULL COMMENT 'Nếu là reply thì trỏ đến comment cha',
  PRIMARY KEY (`id`),
  KEY `assignment_id` (`assignment_id`),
  KEY `user_id` (`user_id`),
  KEY `fk_comment_parent` (`parent_id`),
  CONSTRAINT `assignment_comments_ibfk_1` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `assignment_comments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `assignment_comments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assignment_comments`
--

LOCK TABLES `assignment_comments` WRITE;
/*!40000 ALTER TABLE `assignment_comments` DISABLE KEYS */;
INSERT INTO `assignment_comments` VALUES (26,12,3,'Em chưa hiểu câu hỏi 3 lắm ạ.','2025-07-28 13:28:48',NULL),(27,12,4,'Phần bài tập có cần nộp bản viết tay không ạ?','2025-07-28 13:28:49',NULL),(30,12,2,'Câu 3 em cần áp dụng công thức đạo hàm chuỗi.','2025-07-28 13:30:41',26),(33,12,3,'Dạ em hiểu rồi, em cảm ơn thầy!','2025-07-28 13:39:04',26),(34,12,2,'Không cần em nhé, chỉ cần nộp bản PDF là được.','2025-07-28 13:39:08',27);
/*!40000 ALTER TABLE `assignment_comments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assignments`
--

DROP TABLE IF EXISTS `assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assignments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` tinytext,
  `class_id` int NOT NULL,
  `due_date` datetime NOT NULL,
  `max_score` decimal(5,2) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_assignments_class` (`class_id`),
  CONSTRAINT `assignments_ibfk_1` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assignments`
--

LOCK TABLES `assignments` WRITE;
/*!40000 ALTER TABLE `assignments` DISABLE KEYS */;
INSERT INTO `assignments` VALUES (1,'Algebra Homework','Solve equations chapter 1',1,'2023-10-15 23:59:00',10.00,'2025-07-24 13:19:36'),(12,'Bài Tập Tuần 1','Lý thuyết chương 1 và bài tập thực hành',1,'2025-08-01 23:59:00',10.00,'2025-07-28 13:09:01'),(13,'Bài Tập Tuần 2','Ôn tập chương 2 + làm bài 3.1 đến 3.5',1,'2025-08-08 23:59:00',15.00,'2025-07-28 13:09:01'),(14,'Thuyết trình nhóm','Chọn chủ đề và chuẩn bị slide',2,'2025-08-05 18:00:00',20.00,'2025-07-28 13:09:01'),(15,'Giữa kỳ','Làm bài kiểm tra giữa kỳ trên hệ thống',2,'2025-08-15 23:59:00',30.00,'2025-07-28 13:09:01'),(16,'Đồ án cuối kỳ','Triển khai mini project theo nhóm',2,'2025-09-01 17:00:00',50.00,'2025-07-28 13:09:01');
/*!40000 ALTER TABLE `assignments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class_activities`
--

DROP TABLE IF EXISTS `class_activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_activities` (
  `id` int NOT NULL AUTO_INCREMENT,
  `class_id` int NOT NULL,
  `user_id` int NOT NULL,
  `activity_type` tinytext NOT NULL,
  `target_id` int DEFAULT NULL,
  `description` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `class_id` (`class_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `class_activities_ibfk_1` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `class_activities_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class_activities`
--

LOCK TABLES `class_activities` WRITE;
/*!40000 ALTER TABLE `class_activities` DISABLE KEYS */;
INSERT INTO `class_activities` VALUES (1,1,3,'submit',1,'John submitted Algebra Homework','2025-07-24 13:19:36'),(2,1,4,'submit',2,'Mary submitted Algebra Homework','2025-07-24 13:19:36');
/*!40000 ALTER TABLE `class_activities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class_members`
--

DROP TABLE IF EXISTS `class_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_members` (
  `class_id` int NOT NULL,
  `student_id` int NOT NULL,
  `joined_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`class_id`,`student_id`),
  KEY `idx_class_members_student` (`student_id`),
  CONSTRAINT `class_members_ibfk_1` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `class_members_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class_members`
--

LOCK TABLES `class_members` WRITE;
/*!40000 ALTER TABLE `class_members` DISABLE KEYS */;
INSERT INTO `class_members` VALUES (2,3,'2025-07-24 13:19:36'),(2,4,'2025-07-24 13:19:36');
/*!40000 ALTER TABLE `class_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class_schedules`
--

DROP TABLE IF EXISTS `class_schedules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_schedules` (
  `id` int NOT NULL AUTO_INCREMENT,
  `class_id` int NOT NULL,
  `day_of_week` tinyint NOT NULL COMMENT '1=Thứ 2 … 7=Chủ nhật',
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `location` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_schedule_class` (`class_id`),
  CONSTRAINT `fk_schedule_class` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class_schedules`
--

LOCK TABLES `class_schedules` WRITE;
/*!40000 ALTER TABLE `class_schedules` DISABLE KEYS */;
/*!40000 ALTER TABLE `class_schedules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classes`
--

DROP TABLE IF EXISTS `classes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `class_name` varchar(100) NOT NULL,
  `subject` varchar(100) NOT NULL,
  `school_year` int NOT NULL,
  `semester` tinytext NOT NULL,
  `description` tinytext,
  `teacher_id` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `classes_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classes`
--

LOCK TABLES `classes` WRITE;
/*!40000 ALTER TABLE `classes` DISABLE KEYS */;
INSERT INTO `classes` VALUES (1,'Math 101','Mathematics',2023,'I','Introduction to Algebra',2,'2025-07-24 13:19:36'),(2,'Lớp 12A1','Toán',2025,'Học kỳ 1','Lớp học nâng cao',2,'2025-07-24 23:26:49');
/*!40000 ALTER TABLE `classes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `final_grades`
--

DROP TABLE IF EXISTS `final_grades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `final_grades` (
  `id` int NOT NULL AUTO_INCREMENT,
  `student_id` int NOT NULL,
  `class_id` int NOT NULL,
  `semester` tinytext NOT NULL,
  `school_year` int NOT NULL,
  `average_score` decimal(5,2) NOT NULL,
  `letter_grade` varchar(2) NOT NULL,
  `quiz_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `class_id` (`class_id`),
  KEY `idx_final_grades_student` (`student_id`),
  KEY `quiz_id` (`quiz_id`),
  CONSTRAINT `final_grades_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `final_grades_ibfk_2` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `final_grades_ibfk_3` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `final_grades`
--

LOCK TABLES `final_grades` WRITE;
/*!40000 ALTER TABLE `final_grades` DISABLE KEYS */;
INSERT INTO `final_grades` VALUES (1,3,1,'I',2023,8.50,'B',NULL),(2,4,1,'I',2023,9.20,'A',NULL);
/*!40000 ALTER TABLE `final_grades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quiz_answers`
--

DROP TABLE IF EXISTS `quiz_answers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_answers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `submission_id` int NOT NULL,
  `question_id` int NOT NULL,
  `selected_option` char(1) DEFAULT NULL,
  `is_correct` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `submission_id` (`submission_id`),
  KEY `question_id` (`question_id`),
  CONSTRAINT `quiz_answers_ibfk_1` FOREIGN KEY (`submission_id`) REFERENCES `quiz_submissions` (`id`),
  CONSTRAINT `quiz_answers_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `quiz_questions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quiz_answers`
--

LOCK TABLES `quiz_answers` WRITE;
/*!40000 ALTER TABLE `quiz_answers` DISABLE KEYS */;
/*!40000 ALTER TABLE `quiz_answers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quiz_options`
--

DROP TABLE IF EXISTS `quiz_options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_options` (
  `id` int NOT NULL AUTO_INCREMENT,
  `question_id` int NOT NULL,
  `option_label` varchar(255) NOT NULL,
  `option_text` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `question_id` (`question_id`),
  CONSTRAINT `quiz_options_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `quiz_questions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quiz_options`
--

LOCK TABLES `quiz_options` WRITE;
/*!40000 ALTER TABLE `quiz_options` DISABLE KEYS */;
INSERT INTO `quiz_options` VALUES (17,5,'A','Chủ nghĩa Mác - Lênin kết hợp với phong trào công nhân và phong trào yêu nước Việt Nam.'),(18,5,'B','Chủ nghĩa Mác - Lênin, tư tưởng Hồ Chí Minh với phong trào công nhân Việt Nam.'),(19,5,'C','Chủ nghĩa Mác - Lênin, tư tưởng Hồ Chí Minh với phong trào yêu nước và phong trào công nhân Việt Nam.'),(20,5,'D','Tư tưởng Hồ Chí Minh với phong trào công nhân và phong trào nông dân Việt Nam.'),(21,6,'A','Cuộc bãi công của công nhân thợ nhuộm Sài Gòn - Chợ Lớn (1922).'),(22,6,'B','Cuộc tổng bãi công của công nhân Bắc Kỳ (1922).'),(23,6,'C','Cuộc bãi công của thợ máy xưởng Ba Son cảng Sài Gòn (1925).'),(24,6,'D','Cuộc bãi công của công nhân nhà máy sợi Nam Định (1925).'),(25,7,'A','Hà Nội - Bí thư Trịnh Đình Cửu.'),(26,7,'B','Sài Gòn - Bí thư Ngô Gia Tự.'),(27,7,'C','Sài Gòn - Bí thư Trịnh Đình Cửu.'),(28,7,'D','Hà Nội - Bí thư Trần Văn Cung.'),(29,8,'A','Nguyễn Ái Quốc.'),(30,8,'B','Trần Phú.'),(31,8,'C','Lê Hồng Phong.'),(32,8,'D','Hà Huy Tập.'),(33,9,'A','Giai cấp tư sản.'),(34,9,'B','Giai cấp vô sản.'),(35,9,'C','Giai cấp nông dân.'),(36,9,'D','Giai cấp địa chủ.'),(37,10,'A','1932.'),(38,10,'B','1933.'),(39,10,'C','1934.'),(40,10,'D','1935.'),(41,11,'A','Năm 1920, khi tổ chức công hội ở Sài Gòn được thành lập'),(42,11,'B','Năm 1925, khi cuộc bãi công ở nhà máy Ba Son diễn ra rầm rộ'),(43,11,'C','Năm 1929, khi có sự ra đời của ba tổ chức cộng sản'),(44,11,'D','Năm 1930, khi Đảng Cộng sản Việt Nam ra đời'),(45,12,'A','Cách mạng Tháng Mười Nga bùng nổ và thắng lợi (1917)'),(46,12,'B','Sự thành lập Đảng Cộng sản Pháp (1920)'),(47,12,'C','Vụ mưu sát viên toàn quyền Méc-Lanh của Phạm Hồng Thái (1924)'),(48,12,'D','Sự ra đời của Hội Việt Nam cách mạng Thanh niên (1925)'),(49,13,'A','Khuynh hướng phong kiến'),(50,13,'B','Khuynh hướng dân chủ tư sản'),(51,13,'C','Khuynh hướng vô sản'),(52,13,'D','Khuynh hướng dân chủ'),(53,14,'A','Bùi Quang Chiêu'),(54,14,'B','Phan Châu Trinh'),(55,14,'C','Phan Bội Châu'),(56,14,'D','Nguyễn Ái Quốc'),(57,15,'A','Bỏ phiếu tán thành việc gia nhập Quốc tế III và tham gia thành lập Đảng Cộng sản Pháp'),(58,15,'B','Đọc bản Sơ thảo lần thứ nhất những luận cương về vấn đề dân tộc và vấn đề thuộc địa của Lênin'),(59,15,'C','Gửi Bản yêu sách của nhân dân An Nam tới Hội nghị Véc-xây'),(60,15,'D','Ra đi tìm đường cứu nước'),(61,16,'A','Thanh niên'),(62,16,'B','Cờ đỏ'),(63,16,'C','Độc lập'),(64,16,'D','Người cùng khổ'),(65,17,'A','1919'),(66,17,'B','1920'),(67,17,'C','1921'),(68,17,'D','1922');
/*!40000 ALTER TABLE `quiz_options` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quiz_questions`
--

DROP TABLE IF EXISTS `quiz_questions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_questions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `quiz_id` int NOT NULL,
  `question_text` tinytext NOT NULL,
  `correct_option` char(1) DEFAULT NULL,
  `score` decimal(5,2) DEFAULT '1.00',
  PRIMARY KEY (`id`),
  KEY `quiz_id` (`quiz_id`),
  CONSTRAINT `quiz_questions_ibfk_1` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quiz_questions`
--

LOCK TABLES `quiz_questions` WRITE;
/*!40000 ALTER TABLE `quiz_questions` DISABLE KEYS */;
INSERT INTO `quiz_questions` VALUES (5,4,'Sự ra đời của Đảng Cộng sản Việt Nam là sự kết hợp của các yếu tố nào?','C',1.00),(6,4,'Sự kiện nào đánh dấu giai cấp công nhân Việt Nam đã bước đầu đi vào đấu tranh tự giác?','C',1.00),(7,4,'Chi bộ cộng sản đầu tiên ở nước ta được thành lập ở đâu? Ai làm bí thư chi bộ?','D',1.00),(8,4,'Cương lĩnh chính trị đầu tiên của Đảng do ai soạn thảo?','A',1.00),(9,4,'Cương lĩnh chính trị đầu tiên của Đảng đã xác định giai cấp nào là lực lượng lãnh đạo cách mạng?','B',1.00),(10,4,'Trong giai đoạn Đảng lãnh đạo giành chính quyền, Ban Chỉ huy ở ngoài của Đảng được thành lập vào năm nào?','C',1.00),(11,4,'Sự kiện nào đã đánh dấu phong trào công nhân Việt Nam hoàn toàn trở thành một phong trào tự giác?','D',1.00),(12,4,'Sự kiện nào được Nguyễn Ái Quốc đánh giá “như chim én nhỏ báo hiệu mùa xuân”?','C',1.00),(13,4,'Phong trào đình công, bãi công của công nhân Việt Nam trong những năm 1926 - 1929 thuộc khuynh hướng nào?','C',1.00),(14,4,'Ai là người đại diện cho chủ trương đánh đuổi thực dân Pháp giành độc lập dân tộc, khôi phục chủ quyền quốc gia bằng biện pháp bạo động?','C',1.00),(15,4,'Sự kiện nào đánh dấu bước ngoặt trong cuộc đời hoạt động cách mạng của Nguyễn Ái Quốc - từ người yêu nước trở thành người cộng sản?','A',1.00),(16,4,'Hội Liên hiệp các dân tộc thuộc địa có cơ quan ngôn luận là tờ báo nào?','D',1.00),(17,4,'Nguyễn Ái Quốc đã đọc bản Sơ thảo lần thứ nhất những luận cương về vấn đề dân tộc và vấn đề thuộc địa của Lênin đăng trên báo Nhân đạo vào năm:','B',1.00);
/*!40000 ALTER TABLE `quiz_questions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quiz_submissions`
--

DROP TABLE IF EXISTS `quiz_submissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_submissions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `quiz_id` int NOT NULL,
  `student_id` int NOT NULL,
  `submitted_at` datetime DEFAULT NULL,
  `score` decimal(5,2) DEFAULT NULL,
  `graded_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `quiz_id` (`quiz_id`),
  KEY `student_id` (`student_id`),
  CONSTRAINT `quiz_submissions_ibfk_1` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`),
  CONSTRAINT `quiz_submissions_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quiz_submissions`
--

LOCK TABLES `quiz_submissions` WRITE;
/*!40000 ALTER TABLE `quiz_submissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `quiz_submissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quizzes`
--

DROP TABLE IF EXISTS `quizzes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quizzes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `subject` varchar(100) DEFAULT NULL,
  `grade` varchar(10) DEFAULT NULL,
  `description` longtext,
  `time_limit` int DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `class_id` int DEFAULT NULL,
  `created_by` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `class_id` (`class_id`),
  KEY `created_by` (`created_by`),
  CONSTRAINT `quizzes_ibfk_1` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`),
  CONSTRAINT `quizzes_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quizzes`
--

LOCK TABLES `quizzes` WRITE;
/*!40000 ALTER TABLE `quizzes` DISABLE KEYS */;
INSERT INTO `quizzes` VALUES (3,'DB kiểm tra 15 phút Toán học - Lớp 10','math','10','DB kiểm tra 15 phút chương T',15,'2025-07-28','2025-07-28',2,2,'2025-07-28 00:52:14'),(4,'Đề kiểm tra 15 phút Toán học - Lớp 10','math','10','Đề kiểm tra 15 phút chương I',12,'2025-07-28','2025-07-28',2,2,'2025-07-28 01:16:51');
/*!40000 ALTER TABLE `quizzes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (3,'admin'),(1,'student'),(2,'teacher');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `submissions`
--

DROP TABLE IF EXISTS `submissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `submissions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `assignment_id` int NOT NULL,
  `student_id` int NOT NULL,
  `submitted_at` datetime NOT NULL,
  `file_path` varchar(255) NOT NULL,
  `status` varchar(20) DEFAULT 'pending',
  `score` decimal(5,2) DEFAULT NULL,
  `teacher_comment` tinytext,
  `graded_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_submissions_assignment` (`assignment_id`),
  KEY `idx_submissions_student` (`student_id`),
  CONSTRAINT `submissions_ibfk_1` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `submissions_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `submissions`
--

LOCK TABLES `submissions` WRITE;
/*!40000 ALTER TABLE `submissions` DISABLE KEYS */;
INSERT INTO `submissions` VALUES (1,1,3,'2023-10-14 14:30:00','/files/submissions/hw1_john.pdf','pending',NULL,NULL,NULL),(2,1,4,'2023-10-15 10:15:00','/files/submissions/hw1_mary.docx','pending',NULL,NULL,NULL);
/*!40000 ALTER TABLE `submissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` int NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (3,1),(4,1),(2,2),(1,3),(5,3);
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin1','hashed_password','Admin One','admin1@school.edu','2025-07-24 13:19:36'),(2,'teacher1','hashed_password','Teacher Jane','teacher1@school.edu','2025-07-24 13:19:36'),(3,'student1','hashed_password','Student John','student1@school.edu','2025-07-24 13:19:36'),(4,'student2','hashed_password','Student Mary','student2@school.edu','2025-07-24 13:19:36'),(5,NULL,'123',NULL,'p@gmail.com','2025-07-25 01:35:05');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-28 21:07:05
