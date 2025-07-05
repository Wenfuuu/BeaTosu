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
  `slider_multiplier` double NOT NULL,
  `slider_tick_rate` double NOT NULL,
  `star_rating` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `beatmaps`
--

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
  `is_supporter` BOOLEAN NOT NULL DEFAULT false
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `country_code`, `profile_picture`, `performance`, `accuracy`, `play_count`, `level`, `experience`, `is_supporter`) VALUES
  (1, 'wenfu', 'bertrand13022005@gmail.com', 'd1ea055730ff077714908cd3a849632a849764d05a0ecf45d98483f7e08cf0ea', 'ID', NULL, 3234, '94.67', 1023, 78, 15600, false),
  (2, 'artificed', 'artificed@gmail.com', 'cad8c22a24ea48ea0050614ae74f2e059efbb58ab5c8c23ec03c5d65a9700890', 'US', NULL, 7313, '78.23', 1343, 94, 28800, true);

-- --------------------------------------------------------

--
-- Indexes for dumped tables
--

ALTER TABLE `beatmaps`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `beatmaps`
  ADD KEY `beatmap_set_id` (`beatmap_set_id`);

ALTER TABLE `scores`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `scores`
  ADD KEY `beatmap_id` (`beatmap_id`);

ALTER TABLE `scores`
  ADD KEY `user_id` (`user_id`);

ALTER TABLE `beatmap_sets`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `users`
  ADD UNIQUE KEY `email` (`email`);

-- --------------------------------------------------------

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

-- --------------------------------------------------------

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
  ADD CONSTRAINT `scores_ibfk_1` FOREIGN KEY (`beatmap_id`) REFERENCES `beatmaps` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `scores`
  ADD CONSTRAINT `scores_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;