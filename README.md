# MultiWiki
MultiWiki is a tool to compare Wikipedia articles from different languages by automatically aligning text passages containing common information.

# More Information
[http://multiwiki.l3s.uni-hannover.de]

# Preparation
* install HeidelTime
* fill out parameters in "app/Configuration"
* (optionally) install local instance of DBpedia Spotlight
* create database tables using SQL script in "db_setup/"
* install language links database table from [http://multiwiki.l3s.uni-hannover.de/data/language_links.sql]

# Processing
* Example class: "trials/DBPopulateTest"
* starts with extraction from Wikipedia followed by Spotlight extraction, HeidelTime extraction and translation

# Text passage alignment
* Example class: "trials/PassageAlignmentTest"
