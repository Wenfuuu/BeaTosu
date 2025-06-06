-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 31, 2025 at 08:23 AM
-- Server version: 10.4.19-MariaDB
-- PHP Version: 8.0.7

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `beatosu`
--

-- --------------------------------------------------------

--
-- Table structure for table `beatmaps`
--

CREATE TABLE `beatmaps` (
  `id` int(11) NOT NULL,
  `beatmap_set_id` int(11) NOT NULL,
  `version` varchar(255) NOT NULL,
  `hp_drain_rate` double NOT NULL,
  `circle_size` double NOT NULL,
  `overall_difficulty` double NOT NULL,
  `approach_rate` double NOT NULL,
  `slide_multiplier` double NOT NULL,
  `slider_tick_rate` double NOT NULL,
  `star_rating` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `beatmaps`
--

INSERT INTO `beatmaps` (`id`, `beatmap_set_id`, `version`, `hp_drain_rate`, `circle_size`, `overall_difficulty`, `approach_rate`, `slide_multiplier`, `slider_tick_rate`, `star_rating`) VALUES
(1201284, 567148, 'Hard', 5, 4, 6, 7.5, 1.4, 1, 5.07),
(1201410, 567148, 'Normal', 4, 3.5, 4, 5, 1.1, 1, 3.38),
(1201550, 567148, 'Easy', 2, 3.2, 2, 3, 0.8, 1, 1.71),
(1205138, 567148, 'HB\'s Insane', 6, 4, 7, 9, 1.6, 1, 6.08),
(1207223, 569503, 'Extra', 6, 4, 8.5, 9.3, 1.7, 1, 6.625),
(1222417, 569503, 'Collab Insane', 6, 4, 8, 9, 1.6, 1, 6.33),
(1240054, 569503, '117\'s Hard', 5, 4, 6, 8, 1.5, 1, 5.3),
(1242829, 569503, 'Akanya\'s Easy', 2, 3, 2, 3, 1, 1, 1.85),
(1245721, 569503, 'Regraz\'s Normal', 3.5, 3.9, 4.1, 5.3, 1, 1, 3.38),
(1255495, 569503, 'NiNo\'s Extra', 5, 4, 8.5, 9.4, 1.9, 1, 6.665),
(2014468, 962088, 'Hard', 4.5, 3.2, 6, 8, 1.3, 1, 4.984999999999999),
(2014469, 962088, 'Horizon', 5.5, 3.8, 9, 9.2, 1.4, 1, 6.385),
(2014470, 962088, 'Insane', 5, 3.6, 8, 9, 1.3, 1, 5.9),
(2014471, 962088, 'Normal', 3.5, 3, 4, 5, 1.1, 1, 3.255),
(2581182, 1241559, 'Tenshi', 5, 4, 7.8, 8.6, 1.6, 1, 6.01),
(2586612, 1241559, 'xidorn\'s Normal', 3, 3, 4, 5, 1.2, 1, 3.26),
(2603213, 1241559, 'bongo\'s Hard', 4, 3.7, 6, 6.7, 1.4, 1, 4.6499999999999995),
(2753267, 1328989, 'Hard', 4.5, 3.5, 6, 8, 1.2, 1, 4.935),
(2753268, 1328989, 'gokugohan12468\'s Easy', 2, 2, 2, 3, 1, 1, 1.75),
(2753473, 1328989, 'Wanpachi\'s Normal', 4, 3, 4, 5.5, 1, 1, 3.3999999999999995),
(2755127, 1328989, 'Beast', 5.6, 4.5, 8.5, 9.2, 1.6, 1, 6.505),
(2755633, 1328989, 'Syoko\'s Expert', 5.2, 4.4, 8, 8.8, 1.8, 1, 6.35),
(2756887, 1328989, 'PikA\'s Insane ft. Kuro', 5, 4, 7.5, 9, 1.8, 1, 6.215),
(2865437, 1328989, 'Addy\'s Extra', 5.4, 3.8, 8, 9.2, 1.8, 1, 6.4399999999999995);

-- --------------------------------------------------------

--
-- Table structure for table `beatmap_scores`
--

CREATE TABLE `beatmap_scores` (
  `id` int(11) NOT NULL,
  `beatmap_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `highest_combo` int(11) NOT NULL,
  `accuracy` double NOT NULL,
  `perfect_hit` int(11) NOT NULL,
  `geki_hit` int(11) NOT NULL,
  `great_hit` int(11) NOT NULL,
  `katu_hit` int(11) NOT NULL,
  `good_hit` int(11) NOT NULL,
  `miss` int(11) NOT NULL,
  `grade` varchar(2) NOT NULL,
  `date` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Table structure for table `beatmap_sets`
--

CREATE TABLE `beatmap_sets` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `artist` varchar(255) NOT NULL,
  `creator` varchar(255) NOT NULL,
  `length` varchar(20) NOT NULL,
  `bpm` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `beatmap_sets`
--

INSERT INTO `beatmap_sets` (`id`, `title`, `artist`, `creator`, `length`, `bpm`) VALUES
(567148, 'Heikousen', 'Sayuri', 'Lilyanna', '01:29', 73),
(569503, 'Uso no Hibana', '96neko', 'Yasaija 714', '03:42', 187),
(962088, 'Marshmary', 'MIMI feat. Hatsune Miku', 'Log Off Now', '01:31', 181),
(1241559, '#Aquairo Palette', 'Minato Aqua', 'Amateurre', '04:32', 155),
(1328989, 'Kaibutsu (TV Size)', 'YOASOBI', '[-Evil-]', '01:28', 170);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `country_code` varchar(5) DEFAULT NULL,
  `profile_picture` blob DEFAULT NULL,
  `performance` int(10) DEFAULT NULL,
  `accuracy` decimal(5,2) DEFAULT NULL,
  `play_count` int(10) DEFAULT NULL,
  `level` int(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `country_code`, `profile_picture`, `performance`, `accuracy`, `play_count`, `level`) VALUES
(1, 'wenfu', 'bertrand13022005@gmail.com', 'wenfu', 'ID', NULL, 3234, '94.67', 1023, 78),
(2, 'paula', 'paula@gmail.com', 'paula', 'US', NULL, 7313, '78.23', 1343, 94),
(3, 'kepin', 'kepin@gmail.com', 'kepin123', 'JP', NULL, 5723, '85.78', 653, 86);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `beatmaps`
--
ALTER TABLE `beatmaps`
  ADD PRIMARY KEY (`id`),
  ADD KEY `beatmap_set_id` (`beatmap_set_id`);

--
-- Indexes for table `beatmap_scores`
--
ALTER TABLE `beatmap_scores`
  ADD PRIMARY KEY (`id`),
  ADD KEY `beatmap_id` (`beatmap_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `beatmap_sets`
--
ALTER TABLE `beatmap_sets`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `beatmaps`
--
ALTER TABLE `beatmaps`
  ADD CONSTRAINT `beatmaps_ibfk_1` FOREIGN KEY (`beatmap_set_id`) REFERENCES `beatmap_sets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `beatmap_scores`
--
ALTER TABLE `beatmap_scores`
  ADD CONSTRAINT `beatmap_scores_ibfk_1` FOREIGN KEY (`beatmap_id`) REFERENCES `beatmaps` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `beatmap_scores_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;