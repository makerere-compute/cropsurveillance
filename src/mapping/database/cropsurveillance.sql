-- phpMyAdmin SQL Dump
-- version 3.3.2deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Nov 21, 2010 at 09:52 PM
-- Server version: 5.1.41
-- PHP Version: 5.3.2-1ubuntu4.5

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `cropsurveillance`
--

-- --------------------------------------------------------

--
-- Table structure for table `imagetiles`
--

CREATE TABLE IF NOT EXISTS `imagetiles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `zoom` int(11) NOT NULL,
  `tile_lon_ul` double NOT NULL,
  `tile_lat_ul` double NOT NULL,
  `tile_lon_lr` double NOT NULL,
  `tile_lat_lr` double NOT NULL,
  `tile_blob` blob NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `imagetiles`
--

INSERT INTO `imagetiles` (`id`, `zoom`, `tile_lon_ul`, `tile_lat_ul`, `tile_lon_lr`, `tile_lat_lr`, `tile_blob`) VALUES
(1, 0, 32.6666666667, 1, 33, 0.666666666667, 0x3c50494c2e496d6167652e496d61676520696d616765206d6f64653d524742412073697a653d3530783530206174203078394436373330433e);
