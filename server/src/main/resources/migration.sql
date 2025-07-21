-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jul 21, 2025 at 01:18 PM
-- Server version: 10.4.19-MariaDB
-- PHP Version: 8.2.0

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
  `slider_multiplier` double NOT NULL,
  `slider_tick_rate` double NOT NULL,
  `star_rating` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `beatmaps`
--

INSERT INTO `beatmaps` (`id`, `beatmap_set_id`, `version`, `hp_drain_rate`, `circle_size`, `overall_difficulty`, `approach_rate`, `slider_multiplier`, `slider_tick_rate`, `star_rating`) VALUES
(1213767, 572897, 'Insane', 7, 4, 7, 9, 1.8, 1, 6.390000000000001),
(1214269, 572897, 'Extra', 8, 4, 9, 9.2, 1.9, 1, 7.179999999999999),
(1214270, 572897, 'Hard', 5, 4, 6, 7.5, 1.5, 1, 5.15),
(1214271, 572897, 'Satellite\'s Easy', 2, 3, 2, 3, 0.799999999999999, 1, 1.6899999999999993),
(1215136, 572897, 'Normal', 4, 3.5, 5, 5, 1, 1, 3.55),
(2222070, 1061287, 'Christmas Drift', 6, 4.2, 9, 9.3, 1.8, 1, 6.85),
(2222443, 1061287, 'Normal', 3, 3, 4.6, 5, 0.999999999999999, 1, 3.2499999999999987),
(2222861, 1061287, 'FuJu\'s Expert', 5, 4, 8.3, 9, 1.75, 1, 6.375),
(2222862, 1061287, 'SMOKELIND\'s Expert', 5.4, 4.1, 8.5, 9.2, 1.77, 1, 6.571),
(2222887, 1061287, 'Insane', 5, 4, 7.7, 9, 1.6, 1, 6.105),
(2222967, 1061287, 'Frontier\'s Expert', 5, 4.5, 8.1, 9, 2, 1, 6.574999999999998),
(2223057, 1061287, 'Deppy\'s Expert', 6, 4.2, 8.5, 9.2, 1.8, 1, 6.695),
(2223134, 1061287, 'iljaaz\'s Expert', 6, 4.2, 8.2, 9, 1.74, 1, 6.511999999999999),
(2223135, 1061287, 'Rolniczy\'s Hi-Speed Expert', 6, 3.6, 8.6, 9.5, 2.1, 1, 6.989999999999999),
(2223144, 1061287, 'Anzei\'s Another', 5.4, 4, 8, 9, 1.69, 1, 6.311999999999999),
(2223734, 1061287, 'Kowari\'s Hard', 4, 3.8, 6.5, 8, 1.3, 1, 5.095),
(2223735, 1061287, 'Xenon\'s Hi-Speed Expert', 5.5, 4.4, 8.8, 9.5, 2, 1, 6.965),
(2226486, 1061287, 'Dorsalplum\'s Extra', 6, 4, 8.7, 9.3, 1.8, 1, 6.754999999999999),
(2227344, 1061287, 'AF\'s Extreme', 5.8, 3.3, 8.7, 9.3, 0.999999999999999, 1, 6.014999999999999),
(2231267, 1061287, 'Light\'s Expert', 6, 4, 8.1, 9, 1.85, 1, 6.555),
(2231839, 1061287, 'Haruki\'s Easy', 2, 2.5, 2, 3, 0.799999999999999, 1, 1.6399999999999992),
(2238336, 1069248, 'Can I Become One, Please?', 6, 3.5, 9, 9.2, 2.8, 2, 7.599999999999999),
(2269409, 1069248, 'papple\'s Hard', 4.5, 3.3, 7, 7.7, 1.9, 1, 5.634999999999999),
(2395915, 1069248, 'Zelq\'s Expert', 6, 4.2, 8.5, 9, 2.5, 1, 7.194999999999999),
(2397393, 1069248, 'TheBlank\'s Insane', 5, 3.3, 8, 8.7, 1.8, 2, 6.2299999999999995),
(3152118, 1542046, '>w<', 6, 4, 8, 9, 1.6, 1, 6.33),
(3152790, 1542046, 'Hard', 5, 4, 6.5, 8, 1.2, 1, 5.185),
(3153850, 1542046, 'Normal', 3, 3.5, 5, 6, 1, 1, 3.6999999999999993);

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
(572897, 'Catch the Moment', 'LiSA', 'Delis', '04:42', 189),
(1061287, 'PADORU / PADORU', 'Turbo', 'DeRandom Otaku', '00:32', 189),
(1069248, 'I wanna be a girl', 'mafumafu', 'Night Mare', '03:58', 139),
(1542046, 'Good-bye sengen', 'Uruha Rushia', 'Akito', '02:56', 170);

-- --------------------------------------------------------

--
-- Table structure for table `scores`
--

CREATE TABLE `scores` (
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
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `country_code` varchar(5) DEFAULT NULL,
  `profile_picture` longblob DEFAULT NULL,
  `performance` int(10) DEFAULT NULL,
  `accuracy` decimal(5,2) DEFAULT NULL,
  `play_count` int(10) DEFAULT NULL,
  `level` int(10) DEFAULT NULL,
  `experience` int(10) DEFAULT NULL,
  `is_supporter` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `country_code`, `profile_picture`, `performance`, `accuracy`, `play_count`, `level`, `experience`, `is_supporter`) VALUES
(1, 'wenfu', 'bertrand13022005@gmail.com', 'd1ea055730ff077714908cd3a849632a849764d05a0ecf45d98483f7e08cf0ea', 'ID', NULL, 3234, '94.67', 1023, 78, 15600, 0),
(2, 'artificed', 'artificed@gmail.com', 'cad8c22a24ea48ea0050614ae74f2e059efbb58ab5c8c23ec03c5d65a9700890', 'US', NULL, 7313, '78.23', 1343, 94, 28800, 1);

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
-- Indexes for table `beatmap_sets`
--
ALTER TABLE `beatmap_sets`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `scores`
--
ALTER TABLE `scores`
  ADD PRIMARY KEY (`id`),
  ADD KEY `beatmap_id` (`beatmap_id`),
  ADD KEY `user_id` (`user_id`);

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
-- AUTO_INCREMENT for table `scores`
--
ALTER TABLE `scores`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

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
-- Constraints for table `scores`
--
ALTER TABLE `scores`
  ADD CONSTRAINT `scores_ibfk_1` FOREIGN KEY (`beatmap_id`) REFERENCES `beatmaps` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `scores_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;