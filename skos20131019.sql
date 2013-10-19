-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.0.96-community-nt


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema sampleskos
--

CREATE DATABASE IF NOT EXISTS sampleskos;
USE sampleskos;

--
-- Definition of table `altlabel`
--

DROP TABLE IF EXISTS `altlabel`;
CREATE TABLE `altlabel` (
  `altLabelID` int(10) unsigned NOT NULL auto_increment,
  `altLabel` text,
  `language` varchar(2) default NULL,
  `terminologyID` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`altLabelID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `altlabel`
--

/*!40000 ALTER TABLE `altlabel` DISABLE KEYS */;
/*!40000 ALTER TABLE `altlabel` ENABLE KEYS */;


--
-- Definition of table `baseschema`
--

DROP TABLE IF EXISTS `baseschema`;
CREATE TABLE `baseschema` (
  `schemaID` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(200) default NULL,
  `alias` text,
  `nameSpace` varchar(200) default NULL,
  PRIMARY KEY  USING BTREE (`schemaID`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `baseschema`
--

/*!40000 ALTER TABLE `baseschema` DISABLE KEYS */;
/*!40000 ALTER TABLE `baseschema` ENABLE KEYS */;


--
-- Definition of table `preflabel`
--

DROP TABLE IF EXISTS `preflabel`;
CREATE TABLE `preflabel` (
  `prefLabelID` int(10) unsigned NOT NULL auto_increment,
  `prefLabel` text,
  `language` varchar(2) default NULL,
  `terminologyID` int(10) unsigned NOT NULL,
  PRIMARY KEY  USING BTREE (`prefLabelID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `preflabel`
--

/*!40000 ALTER TABLE `preflabel` DISABLE KEYS */;
/*!40000 ALTER TABLE `preflabel` ENABLE KEYS */;


--
-- Definition of table `property`
--

DROP TABLE IF EXISTS `property`;
CREATE TABLE `property` (
  `propertyID` int(10) unsigned NOT NULL auto_increment,
  `property` text,
  `schemaID` int(10) unsigned default NULL,
  `URL` varchar(200) default NULL,
  `terminologyID` int(10) unsigned default NULL,
  PRIMARY KEY  (`propertyID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `property`
--

/*!40000 ALTER TABLE `property` DISABLE KEYS */;
/*!40000 ALTER TABLE `property` ENABLE KEYS */;


--
-- Definition of table `propertyrelation`
--

DROP TABLE IF EXISTS `propertyrelation`;
CREATE TABLE `propertyrelation` (
  `propertyRelationID` int(10) unsigned NOT NULL auto_increment,
  `propertyID` int(10) unsigned default NULL,
  `relatedPropertyID` int(10) unsigned default NULL,
  `relation` varchar(200) default NULL,
  PRIMARY KEY  USING BTREE (`propertyRelationID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `propertyrelation`
--

/*!40000 ALTER TABLE `propertyrelation` DISABLE KEYS */;
/*!40000 ALTER TABLE `propertyrelation` ENABLE KEYS */;


--
-- Definition of table `relatedterminolgy_to_property`
--

DROP TABLE IF EXISTS `relatedterminolgy_to_property`;
CREATE TABLE `relatedterminolgy_to_property` (
  `ID` int(10) unsigned NOT NULL auto_increment,
  `terminologyID` int(10) unsigned default NULL,
  `propertyID` int(10) unsigned default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `relatedterminolgy_to_property`
--

/*!40000 ALTER TABLE `relatedterminolgy_to_property` DISABLE KEYS */;
/*!40000 ALTER TABLE `relatedterminolgy_to_property` ENABLE KEYS */;


--
-- Definition of table `terminology`
--

DROP TABLE IF EXISTS `terminology`;
CREATE TABLE `terminology` (
  `terminologyID` int(10) unsigned NOT NULL auto_increment,
  `terminology` text,
  `schemaID` int(10) unsigned default NULL,
  `modified` timestamp NOT NULL default '0000-00-00 00:00:00',
  `hasStatus` varchar(100) default NULL,
  `created` timestamp NOT NULL default '0000-00-00 00:00:00',
  `nameSpace` varchar(1000) default NULL,
  `URL` varchar(1000) default NULL,
  PRIMARY KEY  (`terminologyID`)
) ENGINE=InnoDB AUTO_INCREMENT=16741 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `terminology`
--

/*!40000 ALTER TABLE `terminology` DISABLE KEYS */;
/*!40000 ALTER TABLE `terminology` ENABLE KEYS */;


--
-- Definition of table `terminology_relationship`
--

DROP TABLE IF EXISTS `terminology_relationship`;
CREATE TABLE `terminology_relationship` (
  `terminologyID` int(10) unsigned default NULL,
  `relatedtermID` int(10) unsigned default NULL,
  `relationshipID` int(10) unsigned NOT NULL auto_increment,
  `relation` varchar(200) default NULL,
  PRIMARY KEY  (`relationshipID`)
) ENGINE=InnoDB AUTO_INCREMENT=33451 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `terminology_relationship`
--

/*!40000 ALTER TABLE `terminology_relationship` DISABLE KEYS */;
/*!40000 ALTER TABLE `terminology_relationship` ENABLE KEYS */;


--
-- Definition of procedure `cleanAllTables`
--

DROP PROCEDURE IF EXISTS `cleanAllTables`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `cleanAllTables`()
BEGIN

DROP Temporary TABLE IF EXISTS AllTables;

Create Temporary Table AllTables (

SELECT @curRow := @curRow + 1 AS row_number
, table_name 
FROM INFORMATION_SCHEMA.tables s
JOIN    (SELECT @curRow := 0
) r
WHERE s.table_schema = 'sampleskos');

set @countOfAllTables = (select count(*) from AllTables);
set @c = 1;
WHILE @c<=@countOfAllTables DO

    set @table_name = (select table_name from AllTables where row_number = @c);
    set @stmt = concat( 'Truncate Table ', @table_name);
    Prepare stmt from @stmt;
    Execute stmt;
  SET @c = @c + 1 ;
END WHILE ;

END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
