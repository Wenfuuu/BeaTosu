-- MySQL dump 10.13  Distrib 9.2.0, for macos15.2 (arm64)
--
-- Host: localhost    Database: beatosu2
-- ------------------------------------------------------
-- Server version	9.2.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `beatmaps`
--

DROP TABLE IF EXISTS `beatmaps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `beatmaps` (
  `id` int NOT NULL,
  `beatmap_set_id` int NOT NULL,
  `version` varchar(255) NOT NULL,
  `hp_drain_rate` double NOT NULL,
  `circle_size` double NOT NULL,
  `overall_difficulty` double NOT NULL,
  `approach_rate` double NOT NULL,
  `slide_multiplier` double NOT NULL,
  `slider_tick_rate` double NOT NULL,
  `star_rating` double NOT NULL,
  PRIMARY KEY (`id`),
  KEY `beatmap_set_id` (`beatmap_set_id`),
  CONSTRAINT `beatmaps_ibfk_1` FOREIGN KEY (`beatmap_set_id`) REFERENCES `beatmap_sets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `beatmaps`
--

LOCK TABLES `beatmaps` WRITE;
/*!40000 ALTER TABLE `beatmaps` DISABLE KEYS */;
INSERT INTO `beatmaps` VALUES
(1201284,567148,'Hard',5,4,6,7.5,1.4,1,5.07),
(1201410,567148,'Normal',4,3.5,4,5,1.1,1,3.38),
(1201550,567148,'Easy',2,3.2,2,3,0.8,1,1.71),
(1205138,567148,'HB\'s Insane',6,4,7,9,1.6,1,6.08),
(1207223,569503,'Extra',6,4,8.5,9.3,1.7,1,6.625),
(1222417,569503,'Collab Insane',6,4,8,9,1.6,1,6.33),
(1240054,569503,'117\'s Hard',5,4,6,8,1.5,1,5.3),
(1242829,569503,'Akanya\'s Easy',2,3,2,3,1,1,1.85),
(1245721,569503,'Regraz\'s Normal',3.5,3.9,4.1,5.3,1,1,3.38),
(1255495,569503,'NiNo\'s Extra',5,4,8.5,9.4,1.9,1,6.665);
/*!40000 ALTER TABLE `beatmaps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `beatmap_scores`
--

DROP TABLE IF EXISTS `beatmap_scores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `beatmap_scores` (
  `id` int NOT NULL AUTO_INCREMENT,
  `beatmap_id` int NOT NULL,
  `user_id` int NOT NULL,
  `grade` varchar(2) NOT NULL,
  `score` int NOT NULL,
  `accuracy` double NOT NULL,
  `date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `beatmap_id` (`beatmap_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `beatmap_scores_ibfk_1` FOREIGN KEY (`beatmap_id`) REFERENCES `beatmaps` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `beatmap_scores_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `beatmap_scores`
--

LOCK TABLES `beatmap_scores` WRITE;
/*!40000 ALTER TABLE `beatmap_scores` DISABLE KEYS */;
/*!40000 ALTER TABLE `beatmap_scores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `beatmap_sets`
--

DROP TABLE IF EXISTS `beatmap_sets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `beatmap_sets` (
  `id` int NOT NULL,
  `title` varchar(255) NOT NULL,
  `artist` varchar(255) NOT NULL,
  `creator` varchar(255) NOT NULL,
  `length` varchar(20) NOT NULL,
  `bpm` int NOT NULL,
  `background_path` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `beatmap_sets`
--

LOCK TABLES `beatmap_sets` WRITE;
/*!40000 ALTER TABLE `beatmap_sets` DISABLE KEYS */;
INSERT INTO `beatmap_sets` VALUES
(567148,'Heikousen','Sayuri','Lilyanna','01:29',73,'BG (2).jpg'),
(569503,'Uso no Hibana','96neko','Yasaija 714','03:42',187,'BG.jpg');
/*!40000 ALTER TABLE `beatmap_sets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `country_code` varchar(5) DEFAULT NULL,
  `profile_picture` blob,
  `performance` int(10),
  `accuracy` decimal(5,2),
  `play_count` int(10),
  `level` int(10),
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES
(1, 'wenfu', 'bertrand13022005@gmail.com', 'wenfu', 'ID', NULL, 3234, 94.67, 1023, 78),
(2, 'paula', 'paula@gmail.com', 'paula', 'US', NULL, 7313, 78.23, 1343, 94),
(3, 'kepin', 'kepin@gmail.com', 'kepin123', 'JP', NULL, 5723, 85.78, 653, 86);
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

-- Dump completed on 2025-05-26 11:57:54
