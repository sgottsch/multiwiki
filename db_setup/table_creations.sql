
CREATE TABLE `article` (
  `article_uri` varchar(250) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `language` varchar(2) CHARACTER SET latin1 NOT NULL,
  `english_uri` varchar(250) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `title` varchar(100) DEFAULT NULL,
  `wiki_id` varchar(100) NOT NULL,
  PRIMARY KEY (`article_uri`),
  KEY `fk_article_english_uri` (`english_uri`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `revision` (
  `article_uri` varchar(250) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `revision_id` int(10) unsigned NOT NULL,
  `title` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `date` date DEFAULT NULL,
  `original_html_text` longtext CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `language` varchar(2) CHARACTER SET latin1 NOT NULL,
  `comment` text,
  `loaded_heideltimes` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `loaded_spotlightlinks` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `loaded_spotlightlinks_new` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`revision_id`,`language`),
  KEY `idx_revision_revision_id` (`revision_id`),
  KEY `language_revision_id_idx` (`language`,`revision_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `comparison` (
  `comparison_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `article1_uri` varchar(250) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `article2_uri` varchar(250) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `article3_uri` varchar(250) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `revision1_id` int(10) unsigned NOT NULL,
  `revision2_id` int(10) unsigned NOT NULL,
  `revision3_id` int(10) unsigned DEFAULT NULL,
  `language1` varchar(2) CHARACTER SET latin1 NOT NULL,
  `language2` varchar(2) CHARACTER SET latin1 NOT NULL,
  `language3` varchar(2) CHARACTER SET latin1 DEFAULT NULL,
  `date` datetime NOT NULL,
  `unused` tinyint(1) NOT NULL DEFAULT '0',
  `store_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comparison_id`),
  UNIQUE KEY `unique_comparison` (`article1_uri`,`article2_uri`,`article3_uri`,`revision1_id`,`revision2_id`,`revision3_id`,`date`),
  KEY `fk_comparison_revision1` (`revision1_id`,`language1`),
  KEY `fk_comparison_revision2` (`revision2_id`,`language1`),
  KEY `comparison2_revision_fk_idx` (`language2`,`revision2_id`),
  CONSTRAINT `comparison1_revision_fk` FOREIGN KEY (`revision1_id`, `language1`) REFERENCES `revision` (`revision_id`, `language`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `comparison2_revision_fk` FOREIGN KEY (`language2`, `revision2_id`) REFERENCES `revision` (`language`, `revision_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1184 DEFAULT CHARSET=utf8;

CREATE TABLE `annotation` (
  `article_uri` varchar(250) COLLATE utf8_unicode_ci NOT NULL,
  `revision_id` int(10) unsigned NOT NULL,
  `annotation_id` bigint(20) NOT NULL,
  `annotation_type` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `language` varchar(2) CHARACTER SET latin1 NOT NULL,
  `start_position` int(11) NOT NULL,
  `end_position` int(11) NOT NULL,
  `original_text_html` text COLLATE utf8_unicode_ci NOT NULL,
  `original_text` text COLLATE utf8_unicode_ci NOT NULL,
  `english_text` text COLLATE utf8_unicode_ci,
  `english_text_html` text COLLATE utf8_unicode_ci,
  `stemmed_english_text` text COLLATE utf8_unicode_ci,
  `to_translate` tinyint(1) NOT NULL DEFAULT '0',
  `in_infobox` tinyint(1) NOT NULL DEFAULT '0',
  `containing_paragraph_id` int(11) NOT NULL,
  `text_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`revision_id`,`annotation_id`,`language`),
  KEY `annotation_text_idx` (`original_text_html`(50)),
  KEY `annotation_revision_id_idx` (`revision_id`),
  KEY `annotation_original_language_idx` (`language`),
  KEY `annotation_text_id_idx` (`text_id`),
  KEY `annotation_to_translate_idx` (`to_translate`),
  KEY `annotation_in_infobox_idx` (`in_infobox`),
  KEY `idx_annotation_annotation_id` (`annotation_id`),
  KEY `fk_annotation_revision_idx` (`language`,`revision_id`,`annotation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `annotation_external_link` (
  `article_uri` varchar(250) COLLATE utf8_unicode_ci NOT NULL,
  `language` varchar(2) CHARACTER SET latin1 NOT NULL,
  `revision_id` int(10) unsigned NOT NULL,
  `annotation_id` bigint(20) NOT NULL,
  `external_link_uri` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `start_position` int(11) NOT NULL DEFAULT '0',
  `end_position` int(11) NOT NULL DEFAULT '0',
  `external_link_host` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`revision_id`,`annotation_id`,`external_link_uri`,`start_position`,`end_position`,`language`),
  KEY `fk_annotation_external_link_annotation` (`article_uri`,`revision_id`,`annotation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `annotation_heideltime` (
  `annotation_heidel_time_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `language` varchar(2) CHARACTER SET latin1 NOT NULL,
  `revision_id` int(10) unsigned NOT NULL,
  `annotation_id` bigint(20) NOT NULL,
  `begin_time` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `end_time` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `covered_text` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `number_in_sentence` int(2) NOT NULL,
  PRIMARY KEY (`annotation_heidel_time_id`),
  KEY `annotation_id_idx` (`annotation_id`),
  KEY `revision_id_idx` (`revision_id`,`annotation_id`),
  KEY `annotation_article_id_idx` (`revision_id`,`annotation_id`),
  KEY `annotation_heideltime_revision_fk_idx` (`language`,`revision_id`),
  CONSTRAINT `annotation_heideltime_revision_fk` FOREIGN KEY (`language`, `revision_id`) REFERENCES `revision` (`language`, `revision_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=32079 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `annotation_internal_link` (
  `annotation_internal_link_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `article_uri` varchar(250) COLLATE utf8_unicode_ci NOT NULL,
  `revision_id` int(10) unsigned NOT NULL,
  `annotation_id` bigint(20) NOT NULL,
  `wiki_link` varchar(250) COLLATE utf8_unicode_ci NOT NULL,
  `language` varchar(2) CHARACTER SET latin1 NOT NULL,
  `start_position` int(11) DEFAULT NULL,
  `end_position` int(11) DEFAULT NULL,
  PRIMARY KEY (`annotation_internal_link_id`),
  KEY `idx_wiki_link` (`wiki_link`,`language`),
  KEY `idx_sentence_id` (`article_uri`,`revision_id`,`annotation_id`),
  KEY `idx_article` (`article_uri`),
  KEY `idx_sentence` (`annotation_id`),
  KEY `idx_revision` (`language`,`revision_id`)
) ENGINE=InnoDB AUTO_INCREMENT=143440 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `annotation_spotlightlink` (
  `revision_id` int(10) unsigned NOT NULL,
  `annotation_id` bigint(20) NOT NULL,
  `wiki_link` varchar(250) COLLATE utf8_unicode_ci NOT NULL,
  `language` varchar(2) CHARACTER SET latin1 NOT NULL,
  `number_in_sentence` int(3) NOT NULL,
  `meta_data` text COLLATE utf8_unicode_ci,
  `covered_text` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `has_type` tinyint(1) unsigned DEFAULT NULL,
  PRIMARY KEY (`language`,`revision_id`,`annotation_id`,`number_in_sentence`),
  CONSTRAINT `annotation_spotlightlink_annotation_fk` FOREIGN KEY (`language`, `revision_id`, `annotation_id`) REFERENCES `annotation` (`language`, `revision_id`, `annotation_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `annotation_spotlightlink_revision_fk` FOREIGN KEY (`language`, `revision_id`) REFERENCES `revision` (`language`, `revision_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `author` (
  `name` varchar(150) COLLATE utf8_unicode_ci NOT NULL,
  `location` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `has_ip_address` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `revision_author` (
  `language` varchar(2) CHARACTER SET latin1 NOT NULL,
  `revision_id` int(10) unsigned NOT NULL,
  `author` varchar(250) COLLATE utf8_unicode_ci NOT NULL,
  `edits` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`language`,`revision_id`,`author`),
  KEY `idx_revision_author_revision_id` (`revision_id`),
  CONSTRAINT `revision_author_revision_fk` FOREIGN KEY (`language`, `revision_id`) REFERENCES `revision` (`language`, `revision_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `revision_external_link` (
  `revision_id` int(10) unsigned NOT NULL,
  `language` varchar(2) CHARACTER SET latin1 NOT NULL,
  `external_link_uri` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `external_link_host` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `number_of_occurrences` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`revision_id`,`language`,`external_link_uri`),
  KEY `idx_revision_external_link_revision_id` (`revision_id`),
  CONSTRAINT `revision_external_link_revision_fk` FOREIGN KEY (`revision_id`, `language`) REFERENCES `revision` (`revision_id`, `language`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `revision_history` (
  `article_uri` varchar(250) COLLATE utf8_unicode_ci NOT NULL,
  `language` varchar(2) CHARACTER SET latin1 DEFAULT NULL,
  `revision_id` int(10) unsigned NOT NULL,
  `parent_revision_id` bigint(20) DEFAULT NULL,
  `author` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `minor` tinyint(1) NOT NULL,
  `size` bigint(20) NOT NULL,
  `date` datetime NOT NULL,
  `end_date` datetime DEFAULT NULL,
  `hash` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `valid` tinyint(1) NOT NULL,
  PRIMARY KEY (`article_uri`,`revision_id`),
  KEY `idx_revision_history_article_uri` (`article_uri`),
  KEY `idx_revision_history_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `revision_image` (
  `language` varchar(2) CHARACTER SET latin1 NOT NULL,
  `revision_id` int(10) unsigned NOT NULL,
  `image_uri` varchar(250) COLLATE utf8_unicode_ci NOT NULL,
  `number_of_occurrences` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`revision_id`,`image_uri`,`language`),
  KEY `idx_revision_image_revision_id` (`revision_id`),
  KEY `revision_image_revision_fk_idx` (`language`,`revision_id`),
  CONSTRAINT `revision_image_revision_fk` FOREIGN KEY (`language`, `revision_id`) REFERENCES `revision` (`language`, `revision_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `revision_internal_link` (
  `language` varchar(2) CHARACTER SET latin1 NOT NULL,
  `revision_id` int(10) unsigned NOT NULL,
  `wiki_link` varchar(200) CHARACTER SET utf8 NOT NULL,
  `number_of_occurrences` int(11) unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`language`,`revision_id`,`wiki_link`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `paragraph` (
  `article_uri` varchar(250) COLLATE utf8_unicode_ci NOT NULL,
  `language` varchar(2) CHARACTER SET latin1 NOT NULL,
  `revision_id` int(10) unsigned NOT NULL,
  `paragraph_id` int(11) NOT NULL,
  `paragraph_type` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `start_position` int(11) DEFAULT NULL,
  `end_position` int(11) DEFAULT NULL,
  `content_start_position` int(11) DEFAULT NULL,
  `content_end_position` int(11) DEFAULT NULL,
  `to_translate` tinyint(1) NOT NULL DEFAULT '0',
  `above_paragraph_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`language`,`revision_id`,`paragraph_id`),
  CONSTRAINT `paragraph_revision_fk` FOREIGN KEY (`language`, `revision_id`) REFERENCES `revision` (`language`, `revision_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `paragraph_image` (
  `article_uri` varchar(250) COLLATE utf8_unicode_ci NOT NULL,
  `revision_id` int(10) unsigned NOT NULL,
  `language` varchar(2) CHARACTER SET latin1 NOT NULL,
  `paragraph_id` int(11) NOT NULL,
  `image_url` varchar(250) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`article_uri`,`revision_id`,`paragraph_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `text` (
  `text_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `original_text_html` text COLLATE utf8_unicode_ci NOT NULL,
  `original_text` text COLLATE utf8_unicode_ci NOT NULL,
  `translated_text_html` text COLLATE utf8_unicode_ci,
  `translated_text` text COLLATE utf8_unicode_ci,
  `translated_text_stemmed` text COLLATE utf8_unicode_ci,
  `original_language` varchar(10) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`text_id`),
  KEY `index_text_hash` (`original_text_html`(200)) USING HASH,
  KEY `text_original_text_html_idx` (`original_text_html`(30)),
  KEY `text_original_language_idx` (`original_language`)
) ENGINE=InnoDB AUTO_INCREMENT=123232 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DELIMITER $$
CREATE PROCEDURE `move_annotations_to_texts` ()
BEGIN
-- Write the text_ids into the annotations. If the texts are already translated, add that as well
UPDATE annotation a
JOIN text t ON(a.original_text_html = t.original_text_html AND a.language = t.original_language)
SET a.text_id = t.text_id, a.english_text = t.translated_text, a.english_text_html = t.translated_text_html, a.stemmed_english_text = t.translated_text_stemmed
WHERE a.text_id IS NULL;

-- for all important annotations: move their texts into the text table
INSERT INTO text(original_text_html, original_text, original_language)
SELECT
original_text_html, original_text, language
FROM annotation
WHERE text_id IS NULL
AND to_translate IS TRUE
AND in_infobox IS FALSE
GROUP BY language, original_text_html;

-- Write the newly created text_ids into the annotations.
UPDATE annotation a
JOIN text t ON(a.original_text_html = t.original_text_html AND a.language = t.original_language)
SET a.text_id = t.text_id, a.english_text = t.translated_text, a.english_text_html = t.translated_text_html, a.stemmed_english_text = t.translated_text_stemmed
WHERE a.text_id IS NULL;

END
$$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE `update_annotation_translations` ()
BEGIN

UPDATE annotation a
JOIN text t ON(a.text_id = t.text_id)
SET a.english_text = t.translated_text, a.english_text_html = t.translated_text_html, a.stemmed_english_text = t.translated_text_stemmed
WHERE a.text_id IS NOT NULL AND a.language != 'en' AND t.translated_text IS NOT NULL;

END
$$
DELIMITER ;
