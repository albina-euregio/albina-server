# Changelog

All notable changes to this project will be documented in this file.

## [unreleased]

### 🚀 Features

- *(BlogController)* Send new tech blogs

### 🚜 Refactor

- *(BlogController)* Use CriteriaBuilder
- *(BlogController)* Do not return Optional

### ⚙️ Miscellaneous Tasks

- *(pom)* Update jackson to 2.18.1
- Upgrade to Hibernate 6.6.2
- *(pom)* Update mariadb-java-client to 3.5.0

## [7.0.1] - 2024-11-13

### ⚙️ Miscellaneous Tasks

- *(AvalancheReportController)* Filter bulletin.affectsRegionWithoutSuggestions for PDF

## [7.0.0] - 2024-11-04

### 🚀 Features

- *(TextToSpeech)* Enable for ES and CA
- *(PublicationJob)* Parallelize publication by region
- *(StatisticsService)* POST /statistics/vr
- Add StressLevel
- Add StressLevel
- *(StressLevelService)* Team stress levels
- *(AvalancheBulletinService)* Create bulletin PDF on demand

### 🐛 Bug Fixes

- *(PublicationJob)* Try to use custom ForkJoinPool
- *(PublicationJob)* Try to use custom ForkJoinWorkerThreadFactory
- *(PublicationJob)* Try to use CompletableFuture.runAsync
- *(publication)* Also publish super regions
- *(TextToSpeech)* Key speech.tendency.null
- *(user)* Udpate comments
- *(validity)* Test
- *(validity)* Update tests
- *(strategic-mindset)* Region test
- *(strategic-mindset)* Region test
- *(regions)* Test
- *(statistics)* Use microregion ids
- *(statistics)* Test
- *(statistics)* Test
- *(StressLevelService)* JSON serialization
- *(avalanche-types)* Typo
- *(avalanche-types)* Tests
- *(stress-level)* Test
- *(stress-level)* CriteriaQuery
- *(stress-level)* Exclude deleted users from team
- *(danger-source)* Typo, variant db table
- *(danger-sources)* Db script
- *(danger-source)* Typos, rename status to danger_source_status
- *(danger-sources)* Typo probability
- *(danger-sources)* Fix cascade type and save method for danger sources
- *(danger-sources)* Parsing dates and jackson serialization
- *(danger-sources)* Ignore getter methods in eawsMatrixInformation
- *(danger-sources)* Case insensitive aspect enum
- *(danger-sources)* Typo in TerrainType
- *(danger-sources)* Time range for getVariantsForDangerSource
- RegionTest.testCreateObjectFromJSONAndBack
- *(danger-sources)* Check duplicate regions only for same dangerSourceVariantType
- *(caamlV5)* Use lower case string for aspects
- *(aspects)* ToLowerCaseString
- *(AvalancheProblemType)* No_distinct_problem
- *(no-distinct-avalanche-problem)* Use same string
- AlbinaUtil.isLatest
- PublicationJob.createSymbolicLinks
- PublicationJob.createSymbolicLinks
- PublicationJob.createSymbolicLinks
- PublicationJob.createSymbolicLinks
- PublicationJob.createSymbolicLinks
- PublicationJob.createSymbolicLinks

### 🚜 Refactor

- Use LocalDate.toString
- Use DateTimeFormatter.ofLocalizedDate
- *(MediaFileService)* Move functions
- *(user)* Use change and reset consistently
- Remove obsolete tests and resources
- Remove unused certificates/emailsys.jks
- *(AvalancheBulletinController)* Remove redundant contains checks
- AlbinaUtil.validityStart
- Remove apps.tirol.gv.at/lwd/produkte/json_schema prefix
- Simplify Stream API call chain
- *(danger-sources)* Remove obsolete imports
- *(danger-sources)* Rename status to danger_source_variant_status
- *(danger-sources)* Import avalanche type
- *(danger-sources)* Rename originalDangerSourceVariantId
- Eu.albina.model.EawsMatrixInformation.compareTo
- *(imports)* Organize imports!
- TextToSpeech.createScript
- *(SimpleHtmlUtil)* Use AvalancheReport.getHtmlDirectory
- PublicationJob.createSymbolicLinks without shell script
- *(PdfUtil)* Move methods
- Remove AlbinaUtil.runUpdateFilesScript
- AvalancheReport.isUpdate and AvalancheReport.getPublicationDate
- HasPublicationDate, HasValidityDate

### ⚙️ Miscellaneous Tasks

- *(LanguageCode)* ReplaceAranes
- *(user)* Allow deletion of users via db flag, add columns to user table
- *(i18n)* Update translations
- Update TextToSpeechTest
- Update SimpleHtmlUtilTest
- *(scripts)* Create symbolic links
- *(user)* Allow to update image
- *(user)* Add method to reset password
- *(user)* Encrypt password during creation of user
- *(user)* Allow to update own user
- Sort texts by language to allow caching of API calls
- Sort texts by language to allow caching of API calls
- Sort texts by language to allow caching of API calls
- *(persistence)* Migrate to mariadb-java-client
- *(persistence)* Migrate to hibernate-hikaricp connection pool
- *(bom)* Update guava to 33.2.0
- *(pom)* Upgrade slf4j 2.0.12
- *(pom)* Fix mariadb-java-client exclusion
- *(pom)* Fix testing dependencies scopes
- *(pom)* Byte-buddy is needed by hibernate
- Access-Control-Expose-Headers
- Upgrade to Hibernate 6.5.2
- *(validity)* Use 12AM as am/pm split in caamlv5
- *(validity)* Change startDate and endDate in tests
- *(validity)* Update method to get day of validity
- *(validity)* Update validity and tendency date
- *(pom)* Update jetty-maven-plugin
- *(strategic-mindset)* Add model and logic
- *(avalanche-type)* Add db column, enum, sql script
- *(regions)* Add enableWeatherbox
- *(statistics)* Add region to endpoint
- *(pom)* Update jetty-maven-plugin
- *(persistence)* Org.hibernate.dialect.MariaDBDialect
- *(avalanche-types)* Add to caaml
- *(stress-level)* Add to region config
- *(danger-source)* Add model
- *(danger-sources)* Add controller
- *(StressLevelService)* Ensure @ is not present for team
- *(danger-sources)* Add compare method
- *(danger-sources)* Add DangerSource to DangerSourceVariant model
- *(danger-sources)* Implement controller and service
- *(danger-sources)* Add to region
- *(desktop.ini)* Remove
- *(persistence)* Remove dialect, change property names to jakarta
- *(danger-source)* Add sql to create danger source db tables
- *(danger-sources)* Add title!
- *(danger-sources)* Rename controller, endpoint to fetch variants of specific source
- *(danger-sources)* Remove variants from danger source model
- *(danger-sources)* Update sql script
- *(danger-sources)* Add danger source controller
- *(danger-sources)* Update api urls
- *(danger-sources)* Add api endpoint to get danger sources
- *(danger-sources)* Use LocalDateTime
- *(danger-sources)* Java.time.Instant
- *(danger-sources)* Remove type from variant
- *(danger-sources)* Extend service and controller
- *(danger-sources)* Jackson ignore unknown properties
- *(danger-sources)* Prevent duplicate regions within one dangersource
- *(danger-sources)* Add gliding snow activity value to db
- *(danger-sources)* Handle enums as strings in object mapper
- *(danger-sources)* Update danger source
- *(danger-sources)* Change type of elevation from int to Integer
- *(danger-sources)* Use custom serializer for variant
- *(danger-sources)* Add danger source variant type, load status
- *(danger-sources)* Remove field analysisDangerSourceVariantId
- *(aspect)* Change toString to uppercase
- *(vs-code)* Add extension recommendations!
- *(vs-code)* Add words to dictionary
- *(AvalancheReportController)* PDF preview using HTTP POST
- *(scripts)* Remove EUREGIO handling from updateFiles.sh
- *(scripts)* Remove region dependency from updateLatestFiles.sh
- Omit `chmod 755`, rely on correct UMASK instead
- *(TextToSpeech)* Write SSML file
- Omit `chmod 755`, rely on correct UMASK instead
- *(AvalancheBulletinPublishService)* Remove separate pdf/html/map/caaml publication endpoints

### Hack

- *(mapyrus)* Disable tests

## [6.2.0] - 2024-04-08

### 🚀 Features

- *(PdfUtil)* Replace am/pm with "earlier" and "later" from CAAMLv6
- *(EmailUtil)* Replace am/pm with "earlier" and "later" from CAAMLv6
- *(SimpleHtmlUtil)* Replace am/pm with "earlier" and "later" from CAAMLv6
- *(PdfUtil)* Replace am/pm with "earlier" and "later" from CAAMLv6
- *(EmailUtil)* Replace am/pm with "earlier" and "later" from CAAMLv6
- *(SimpleHtmlUtil)* Replace am/pm with "earlier" and "later" from CAAMLv6

### 🐛 Bug Fixes

- Unit tests
- *(TextToSpeech)* Ssml_gender computation
- *(AlbinaUtil)* NPE
- Unit tests
- *(log4j2)* Leading whitespace on production w/o prefix
- *(pdf)* Check for gliding snow
- *(bulletin)* Move published regions to saved regions in case of change
- RegionTest
- *(MapUtilTest)* Avalanche-warning-maps update
- *(MapUtilTest)* Avalanche-warning-maps update
- *(MapUtilTest)* Avalanche-warning-maps update

### 🚜 Refactor

- *(AlbinaUtil)* Public interface AlbinaUtil
- *(AlbinaUtil)* Use StandardCharsets.UTF_8
- *(AlbinaUtil)* Extract getScriptPath
- *(AlbinaUtil)* Inline getDangerPatternText
- LanguageCode.getTendencyDate
- LanguageCode.getLongDate
- *(AlbinaUtil)* Move encodeFileToBase64Binary to unit test
- LinkUtil.getBulletinLink
- *(AlbinaUtil)* GetPublicationDate returns Instant
- *(AlbinaUtil)* Inline greyDarkColor
- *(AlbinaUtil)* NewShellProcessBuilder
- *(AlbinaUtil)* Rename getPublicationDateDirectory
- Remove unused daytime.* strings
- Remove unused daytime.* strings

### ⚙️ Miscellaneous Tasks

- *(AvalancheReportController)* Tune logging
- *(AvalancheReportController)* One transaction for saveBulletins
- *(AvalancheReportController)* Synchronized saveBulletins
- *(AvalancheReportController)* SaveBulletins returns all bulletins
- *(i18n)* Update translations
- *(i18n)* Update translations
- *(TextToSpeech)* Tune German voices
- *(i18n)* Update translations
- *(pom)* Remove unused javax.mail
- *(pom)* Update log4j2
- *(RegionService)* Enable GET for all authenticated users
- *(pom)* Remove c3po/HikariCP dependency from quartz
- *(bom)* Update guava to 33.0.0
- *(bom)* Update mysql-connector-j to 8.3.0
- *(pom)* Update jackson to 2.16.1
- *(pom)* Update hibernate to 5.6.15.Final
- *(pom)* Update itext to 7.2.6
- *(pdf)* Remove snowpack stability for gliding snow
- *(i18n)* Update translations
- *(Regions)* Add flags enableObservations and enableModelling
- *(i18n)* Update translations
- *(MapUtilTest)* Report base64-encoded images
- *(i18n)* Update translations
- *(PublicationJob)* Handle super regions like normal regions
- *(PdfUtil)* Reuse fonts for header and footer
- *(PdfUtil)* Reuse images (repeated logos)

## [6.1.9] - 2024-02-17

### 🚀 Features

- *(TextToSpeech)* Enable for IT

### 🐛 Bug Fixes

- *(TextToSpeech)* 2024-10-02 is spoken as "October 2nd"
- *(TextToSpeech)* IT replacement keys
- *(TextToSpeech)* "del limite del bosco"

### ⚙️ Miscellaneous Tasks

- *(TextToSpeech)* Voice config for IT
- *(TextToSpeech)* Add voice config to unit tests

## [6.1.8] - 2024-02-09

### 🐛 Bug Fixes

- *(save-bulletins)* Use correct savedRegions

### ⚙️ Miscellaneous Tasks

- *(TextToSpeech)* Remove validityDate from filename
- *(TextToSpeech)* Disable for sub-regions

## [6.1.7] - 2024-02-02

### 🐛 Bug Fixes

- *(TextToSpeech)* Add bulletinID to filename

## [6.1.6] - 2024-01-31

### 🚀 Features

- TextToSpeech
- *(TextToSpeech)* German

### 🐛 Bug Fixes

- TextToSpeech.ENABLED
- RegionTest

### 🚜 Refactor

- *(TextToSpeech)* No "this."
- *(TextToSpeech)* Extract dangerPatterns
- *(TextToSpeech)* Remove unused dp1.long strings
- *(TextToSpeech)* Enabled languages
- *(caaml)* AvalancheBulletinCustomData

### ⚙️ Miscellaneous Tasks

- *(bulletin-lock)* GetLockedBulletins
- *(auto-save)* Add method to save, update and delete single bulletins
- *(TextToSpeech)* Audio config
- *(TextToSpeech)* Aspects
- *(TextToSpeech)* Reorder valid time period
- *(TextToSpeech)* Jingle from static.avalanche.report
- *(TextToSpeech)* Omit empty danger patterns sentence
- *(README)* Add link to Transifex
- *(TextToSpeech)* TextToSpeech.createAudioFile
- *(TextToSpeech)* Tune gender
- *(TextToSpeech)* Write <voice>
- *(TextToSpeech)* TextToSpeech.createAudioFiles
- TextToSpeechTest.test20231201mp3
- TextToSpeechTest add 2024-01-28
- Tune valid-time-period
- Tune logging
- *(PublicationController)* Enable text-to-speech

## [6.1.5] - 2024-01-10

### ⚙️ Miscellaneous Tasks

- *(i18n)* Update translations

## [6.1.4] - 2024-01-04

### ⚙️ Miscellaneous Tasks

- *(i18n)* Update translations

## [6.1.3] - 2023-12-22

### 🐛 Bug Fixes

- *(Caaml6)* Do not merge am/pm problems to retain order
- *(PushNotificationUtil)* No criteria query roots were specified
- *(SubscriberController)* Removing a detached instance

### ⚙️ Miscellaneous Tasks

- *(container)* Remove container image creation
- *(Caaml6)* Set unscheduled flag
- *(ServerInstanceService)* Add public /info service
- *(Caaml6)* Set tendency valid time
- *(AuthenticationService)* Add /test to test access token
- *(CaamlTest)* Add 2023-12-21
- *(BlogController)* Tune logging
- *(AuthenticationService)* Add /test to test access token
- *(SubscriberController)* Tune logging

## [6.1.2] - 2023-12-01

### 🐛 Bug Fixes

- *(Wordpress)* Date parsing
- BlogControllerTest

### ⚙️ Miscellaneous Tasks

- *(change-bulletin)* Start changeThread instantly if status is submitted or resubmitted

## [6.1.1] - 2023-11-30

### 🐛 Bug Fixes

- *(BlogController.getConfiguration)* Might not return a result
- *(RapidMailController.getConfiguration)* Might not return a result
- *(TelegramController.getConfiguration)* Might not return a result
- *(Blogger)* Deserialization of "published":"2023-11-22T08:44:00-08:00"
- Confusing warning in SubscriberController.createSubscriber
- *(MultichannelMessage)* RapidMailController.sendEmail
- *(AvalancheBulletinService)* Change bulletin should not yield status resubmitted

### 🚜 Refactor

- *(BlogController)* Clearly separate config loading, post loading, sending, exception handling
- *(RapidMailController)* Clearly separate config loading, post loading, sending, exception handling
- *(TelegramController)* Clearly separate config loading, post loading, sending, exception handling
- *(PushNotificationUtil.getConfiguration)* Return optional
- *(EmailUtil)* Freemarker.template.Configuration.setClassForTemplateLoading
- Introduce new interface MultichannelMessage
- MultichannelMessage.sendToAllChannels
- Class AvalancheReportMultichannelMessage
- Class BlogItemMultichannelMessage

### ⚙️ Miscellaneous Tasks

- Add /server/health
- *(websocket)* Log @OnError errors as debug
- *(BlogConfiguration.getConfiguration)* Check blogApiUrl!=null
- *(BlogController)* Tune logging
- *(pom)* Update slf4j to 2.0.9
- *(MultichannelMessage)* Memoize html message
- *(MultichannelMessage)* Tune logging
- *(MultichannelMessage)* Tune logging

## [6.1.0] - 2023-11-08

### 🚀 Features

- Caaml6.createXML using jackson-dataformat-xml

### 🚜 Refactor

- XmlUtil.convertDocToString
- Rename CaamlVersion.V6_JSON
- Change suffix _CAAMLv6.json
- Obsolete throws TransformerException
- Add CAAMLv6_BulletinEAWS.xsd
- Delete Caaml6.java
- Rename Caaml6.toCAAML
- Change suffix _CAAMLv6.json

### ⚙️ Miscellaneous Tasks

- *(Wordpress)* Test featured_image_url null
- Update CaamlBulletin2022

### Wordpress

- Run StringEscapeUtils.unescapeHtml4 on blog title

## [6.0.1] - 2023-09-05

### 🚀 Features

- *(RapidMailConfiguration)* Store mailinglistName in database

### 🐛 Bug Fixes

- *(blog)* Use new table name in db query
- *(AvalancheReportController)* Org.hibernate.PersistentObjectException: detached entity passed to persist
- EmailUtilTest.sendMediaEmails

### 🚜 Refactor

- *(RapidMailConfiguration)* Use CriteriaQuery

### ⚙️ Miscellaneous Tasks

- *(test-publication)* Remove test methods for telegram, email and push
- *(test-publication)* Remove test methods for blog
- *(test-publication)* Fix logging

## [6.0.0] - 2023-08-22

### 🚀 Features

- *(ci)* Add container image creation
- *(db)* Prepare db scripts fof liquibase
- *(pom)* Use Java 11
- Reuse existing AvalancheReport if status does not change
- Implement Wordpress API

### 🐛 Bug Fixes

- *(readme)* Bulletin status graph
- *(db)* Adjust markdown syntax
- Resolve Liqubase deprecation warning

### 🚜 Refactor

- *(ci)* Use ref slug instead on ref name
- Replace with Stream API equivalent
- *(BlogController)* Happy path
- Rename GoogleBloggerConfiguration to BlogConfiguration
- Introduce interface BlogItem
- Extract Blogger API

### ⚙️ Miscellaneous Tasks

- *(readme)* Update status graph
- *(db)* Allow db migration on server start
- *(db)* Creaet auto configuration
- *(doc)* Adapt db migration readme
- *(db)* Delete old database scripts
- *(db)* Delete creation script creation
- *(pom)* Update log4j2
- *(pom)* Update mysql-connector-java
- Update jackson to 2.15.2
- Update io.swagger.core.v3
- Update jersey

### README

- Document bulletin status/workflow

## [5.1.15] - 2023-05-03

### 🐛 Bug Fixes

- *(statistic)* Move deliminator

## [5.1.14] - 2023-05-03

### ⚙️ Miscellaneous Tasks

- *(statistics)* Add matrix values

## [5.1.13] - 2023-05-02

### 🐛 Bug Fixes

- *(container)* Map dir name adjusted to configuration
- *(container)* Adjust file permissions

### ⚙️ Miscellaneous Tasks

- Region.toString
- *(container)* Remove obsolete env variables
- *(container)* Tomcat should only log to the console
- *(container)* Avoid sentry initialize hint

### RapidMailController

- Disable FAIL_ON_UNKNOWN_PROPERTIES

### RapidMailRecipientListResponse

- @JsonIgnoreProperties(ignoreUnknown=true)

## [5.1.11] - 2023-04-18

### 🚀 Features

- Allow log configuration via env variables
- *(log4j)* [**breaking**] Add adhoc debug logger configuration

### ⚙️ Miscellaneous Tasks

- *(statistics)* Add new matrix information and danger rating modificator
- *(publish)* Start threads for publication and change!
- *(publication)* Start publication thread
- *(publication)* Start change thread

## [5.1.10] - 2023-04-11

### 🐛 Bug Fixes

- *(AuthenticationController)* ServerKeys constructor argument order
- Create required directory if it not exists

### 🚜 Refactor

- Eu.albina.util.DBEnvConfig.asMap
- *(tx)* Tx migrate

### ⚙️ Miscellaneous Tasks

- *(AuthenticationController)* Use ECDSA256 == ES256
- *(deployment)* Create docker file
- *(BlogController)* Store lastPublishedTimestamp

## [5.1.9] - 2023-02-02

### 🐛 Bug Fixes

- AvalancheBulletinService
- *(RssUtil)* Public URL
- *(media-email)* Send important media file to additional recipients

### 🚜 Refactor

- Migrate unit tests to JUnit 5
- *(PublicationJob)* Use getClass().getSimpleName()
- AvalancheBulletinController.getAllBulletins
- *(AvalancheBulletinController)* Do not throw AlbinaException
- Inline PublicationController.publish
- Inline AvalancheBulletinController.publishBulletins
- *(PublicationJob)* Simplify
- *(PublicationJob)* Simplify
- *(MediaFileService)* Use Files.copy
- Use {} placeholders for logging
- Use String.format

### ⚙️ Miscellaneous Tasks

- Eu.albina.util.HibernateUtilTest.createSchema
- *(RssUtil)* Add owner/name/email elements
- *(i18n)* Update translations

## [5.1.8] - 2022-12-23

### 🚀 Features

- *(bulletins)* Separate /caaml, /caaml/json, /json
- *(openapi)* Rapidoc frontend

### 🐛 Bug Fixes

- *(publication)* Check for status republished
- *(publication)* Use getPublishedBulletinRegions()
- *(AvalancheReport)* Set publicationTimeString from globalBulletins
- *(MapUtil)* Daytime dependency not based on global bulletins
- *(pom)* Update jackson
- *(change)* Same code to publish/update/change
- *(json)* Add regions to EUREGIO JSON
- *(change)* Save/submit/publish bulletins
- *(update)* Reproduce all regions
- *(publication)* Keep already published bulletins
- *(publication)* Load published bulletins
- *(publication)* Bulletins for super region
- *(save)* Set publicationTime to null
- *(change)* Use publicationDate
- *(publication)* Use same publication time
- *(publication)* Use correct avalancheReport for notifications
- *(update)* Set enabled true for manual update
- *(publication)* PublicationTime string
- *(change)* Use same publish method
- *(publication)* Add sleep for change method
- *(publishBulletins)* Set publicationDate for all bulletins
- *(publishedBulletins)* Correct merging of bulletins
- *(publication)* Set same publicationDate for all bulletins
- *(report)* Always set a status
- *(PublicationController)* Bulletins in log

### 🚜 Refactor

- *(jobs)* Harmonize code structure
- *(jobs)* UpdateJob extends PublicationJob
- *(jobs)* /bulletins/publish/all use UpdateJob
- *(jobs)* Inline AlbinaUtil.isReportSubmitted
- *(MapUtil)* Inline DaytimeDependency.of
- *(LinkUtil)* Use AvalancheReport
- *(AvalancheBulletinController)* SaveBulletins
- *(saveBulletins)* Remove publicationTime
- *(PublicationController)* Remove obsolete code

### ⚙️ Miscellaneous Tasks

- *(regions)* Make getActiveRegions() private
- *(AvalancheReport)* Check that bulletins is subset of globalBulletins
- *(AvalancheReport)* Get rid of getRegionBulletins
- *(CaamlTest)* Add 2022-12-20
- *(CaamlTest)* AssertStringEquals
- *(publication)* Add logging

## [5.1.7] - 2022-12-13

### 🐛 Bug Fixes

- *(TelegramController)* SendPhoto using multipart/form-data

### ⚙️ Miscellaneous Tasks

- Add TelegramController.getMe

## [5.1.6] - 2022-12-13

### 🐛 Bug Fixes

- *(scripts)* Manual publication scripts
- *(update)* Publish only submitted/resubmitted reports
- *(publish)* Manually publish all regions

## [5.1.5] - 2022-12-12

### 🐛 Bug Fixes

- *(PublicationJob)* Truncate publication date to seconds
- *(pdf)* Manual pdf production
- *(pdf)* Typo
- *(publication)* Manual resource production

## [5.1.4] - 2022-12-12

### 🐛 Bug Fixes

- *(publication)* Set server instance
- *(publication)* Harmonize publish, update and change
- *(AvalancheBulletinService)* Default to all regions
- *(AvalancheBulletinService)* Default to language=en

## [5.1.3] - 2022-12-09

### 🐛 Bug Fixes

- *(scripts)* Fix paths

### ⚙️ Miscellaneous Tasks

- *(publication)* Create files without region id

## [5.1.2] - 2022-12-06

### 🚀 Features

- *(MediaFileService)* Publish media files as Podcast RSS
- *(PdfUtil)* Add EAWS matrix information

### 🐛 Bug Fixes

- *(Caaml6_2022)* RegionID
- *(Caaml6_2022)* MatrixInformation
- *(PublicationController)* AvalancheReport.setBulletins regionBulletins
- *(SimpleHtmlUtil)* AvalancheReport.getRegionBulletins
- *(Caaml6_2022)* TruncatedTo(ChronoUnit.SECONDS)
- *(Caaml6_2022)* All_day avalancheProblems
- *(PublicationJob)* NPE in UserController.get
- *(AlbinaUtil)* Comparator.nullsFirst
- *(AvalancheReport)* NPE
- *(PdfUtil)* GetPreviewPdf
- *(MapUtilTest)* TestMapyrusMapsTyrol
- *(PublicationController)* Use global bulletins for maps!
- *(MapUtilTest)* Map extends of regions
- *(publication)* Create maps and pdfs for all region on update and publish, send notifications afterwards
- *(zoneId)* Add missing import
- *(publication)* Set status published only for updated regions
- *(update)* Ignore regions that were not published at all
- *(update)* Ignore regions that were not published at all
- *(publication)* Fix update
- *(change)* Reproduce resources for all regions
- *(publication)* Combine scripts for json and xml, create correct xmls on manual trigger
- *(RssUtil)* Valid RSS feed
- *(PublicationJob)* NPE in AvalancheBulletinController.publishBulletins

### 🚜 Refactor

- PdfUtil.getMapImage
- PdfUtil.getPath
- AvalancheReport.getRegionBulletins
- Extract RegionTestUtils
- Use Stream.max
- Inline AlbinaUtil.getDaytimeString
- Extract LanguageCode.getFormatter
- Use String.format
- Test PdfUtil.getPath
- *(CaamlTest)* Extract toCAAMLv6_2022
- *(MediaFileService.saveMediaFile)* Use getMediaPath and Files
- *(PdfUtil)* Use lang,  grayscale fields
- *(PdfUtil)* Extract methods from createAvalancheProblem
- *(PdfUtil)* WebColors.getRGBColor

### ⚙️ Miscellaneous Tasks

- Rebuild 2022 maps
- *(PdfUtil)* Cache resource images for performance
- *(PdfUtil)* Deflater.BEST_SPEED for performance
- *(rebuildMaps)* Use ExecutorService
- *(rebuildMaps)* AvalancheReportController is too complicated
- *(rebuildMaps)* CompletableFuture is too complicated
- *(rebuildMaps)* Logging
- ImageTestUtils.assertEquals w/ message
- *(rebuildMaps)* RebuildPdfUtil
- *(rebuildMaps)* 2022-05-02
- CaamlTest.createOldCaamlFiles2022
- CaamlTest.createOldCaamlFiles2022
- CaamlTest.createOldCaamlFiles2022
- CaamlTest.createOldCaamlFiles2022
- *(Caaml6_2022)* DangerPattern implements CustomDatum
- *(Caaml6_2022)* MainDate implements CustomDatum
- *(Caaml6_2022)* Update JSON schema file
- *(PublicationController)* Logging
- UpdateLatestFiles.sh
- *(Caaml6_2022)* "namespace" customData
- *(RssUtil)* Limit 10
- *(MediaFileService)* GetMediaPath+region+language

## [5.1.1] - 2022-11-21

### 🐛 Bug Fixes

- Image paths in region_AT-07.json
- *(PdfUtil)* RegionBulletins

### 🚜 Refactor

- *(AvalancheReport.toCAAML)* Inline
- *(AuthorizationFilter)* Prefer happy-path

### ⚙️ Miscellaneous Tasks

- *(AuthorizationFilter)* Clearer exception message
- *(AuthorizationFilter)* Clearer exception message

## [5.1.0] - 2022-11-11

### 🚀 Features

- Use eaws-regions/micro-regions_names
- Build albina-server as Docker image
- Build albina-server as Docker image
- Build CAAML v6 JSON
- Upgrade to Swagger 2
- *(DateControllerUtil)* Parse LocalDate

### 🐛 Bug Fixes

- *(Region)* NeighborRegions is undefined
- Lauegi_map.png logo
- RegionTest
- *(Region)* Getter/setter name
- SimpleHtmlUtilTest
- PublicationController.sendEmails
- BulletinStatusTest.testCompare
- PushNotificationUtilTest
- *(websocket)* Never timeout due to inactivity
- *(XmlUtil)* Use correct map filenames
- *(AvalancheBulletin.createCAAMLv6Bulletin)* Use correct map filenames
- *(EmailUtil)* Use correct map filenames
- *(SimpleHtmlUtil)* Use correct map filenames
- Missing AvalancheReport.setBulletins
- *(docker)* Custom network
- *(docker)* AllowPublicKeyRetrieval
- *(docker)* Database name
- *(docker)* Git version
- *(docker)* No git version
- *(docker)* Albina-admin-gui environment-relative
- Max key length is 767 bytes
- *(docker)* Avalanche-warning-maps
- *(docker)* Install ghostscript imagemagick webp
- *(MapUtil)* MapProductionResource for docker
- Could not initialize proxy [eu.albina.model.Region#AT-07] - no Session
- MapUtilTest.testMapyrusMaps
- "regionId"
- Trim strings in AvalancheBulletin.getTextPartIn
- Duplicate dangerRatings in CAAML v6
- AvalancheReportTest
- @SuppressWarnings for Hibernate queries
- *(Caaml6_2022)* NPE

### 🚜 Refactor

- Remove jts dependency
- Move constants to StatisticsController
- Move constants to AuthenticationController
- *(GlobalVariables)* Remove unused code
- Move constants to AlbinaUtil
- Move function to PdfUtil
- Load properties using java.util.Properties
- Use String.isEmpty
- Replace albina_files with bulletins in tests
- Split MapUtil.filename into two variants
- Enhance AvalancheReport class
- If (regionBulletins.isEmpty())
- Use AvalancheReport for MapUtil
- Use AvalancheReport for MapUtil
- Use AvalancheReport.status==draft as preview
- Use AvalancheReport for PdfUtil
- Use AvalancheReport for XmlUtil.createCaaml
- Use AvalancheReport for SocialMediaUtil
- Use AvalancheReport for JsonUtil.createJsonFile
- Use AvalancheReport for EmailUtil
- Use AvalancheReport for SimpleHtmlUtilTest
- AvalancheReportController.setAvalancheReportFlag
- AvalancheReportController.setAvalancheReportFlag
- AvalancheBulletinService.getPreviewPdf
- Simplify AuthenticationController.isUserInRole
- Move AuthenticationController.authenticate/isUserInRole
- Use DateControllerUtil.DATE_FORMAT_DESCRIPTION
- AvalancheReport.toCAAML
- Remove unused AlbinaUtil.getThumbnailFileName
- Gitlab.com/albina-euregio/albina-docker
- "regionID" in CAAML v6
- Fix typo "boundary" in CAAML v6
- Use EnumSet.allOf
- Remove obsolete org.jadira.usertype
- Remove obsolete hibernate-spatial
- Replace with Stream API equivalent
- Remove obsolete test/resources/regions.geojson
- Eu.albina.caaml.Caaml
- Eu.albina.caaml.Caaml5
- Eu.albina.caaml.Caaml6
- Eu.albina.caaml.Caaml6_2022
- Interface Caaml, interface CaamlValidator
- Eu.albina.rest.AuthenticationService.Credentials
- Eu.albina.rest.AvalancheBulletinPublishService
- Eu.albina.controller.RegionController.getRegionOrThrowAlbinaException
- Eu.albina.rest.AvalancheBulletinStatusService
- Rename class EmailSubscription
- *(AlbinaUtil.isUpdate)* LocalTime.of(17, 0)
- *(AlbinaUtil.hasDaytimeDependency)* Inline

### ⚙️ Miscellaneous Tasks

- *(RegionTest)* Test com.fasterxml.jackson
- I18n update
- Add MapUtilTest.testFilename
- Use BulletinStatus.isDraftOrUpdated
- Update mysql-connector-java
- Update commons-text
- Update io.sentry logging
- Update swagger-jersey2-jaxrs
- Update org.glassfish.jersey
- Static BulletinStatus.isDraftOrUpdated (fix NPE)
- Update .tx/config for eaws-regions.micro-region-names
- I18n update
- Update itextpdf
- *(docker)* Add albina-admin-gui
- *(docker)* Add avalanche-warning-maps
- *(docker)* Add textcat-ng
- *(docker)* Albina-admin-gui w/ envsubst
- *(docker)* Use volumes for docker-entrypoint-initdb.d
- *(docker)* ALBINA_DB_CONNECTION_URL
- *(docker)* Custom log4j2.xml
- *(docker)* Use albina.war
- *(docker)* ALBINA_JWT_SECRET
- *(media-file)* Add lang to path
- ImageTestUtils.assertImageEquals w/ message
- Validate CAAML v6 JSON
- Add DangerPattern name to CAAML v6
- Add avalanche problems to CAAML v6
- Danger pattern as type/id/name in CAAML v6
- *(PublicationController)* Generate CAAML v6 JSON
- *(persistence)* MySQL55Dialect
- *(Caaml)* Javadoc
- @OpenAPIDefinition
- @Schema for getJSONBulletin
- @Schema for getJSONBulletins
- @Tag w/o "/"
- @Operation and @Schema for UserService
- @Operation and @Schema for ServerInstanceService
- @Operation and @Schema for RegionService
- @Operation and @Schema for AvalancheBulletinService
- @SecurityScheme for AuthenticationService
- @SecurityRequirement
- @Operation for PushNotificationService
- @Operation for ObservationService
- @ArraySchema
- @Server
- @Produces for ObservationService
- @Operation for AvalancheBulletinService
- @Schema for AvalancheBulletinStatusService
- @Operation for MediaFileService
- @Operation for SubscriptionService
- @Operation for StatisticsService
- @Operation for AuthenticationService
- *(AvalancheBulletinService.getPublishedXMLBulletins)* JSON for CaamlVersion.V6_2022

### Albina_create

- Bit default false

## [5.0.1] - 2022-10-11

### 🐛 Bug Fixes

- MapUtilTest.testMapyrusMaps
- *(region)* Update region json for test
- *(test)* Update new regions
- Map production for preview
- *(pom)* Update log4j2
- *(pom)* Update log4j2
- *(MapUtilTest)* TestMayrusBindings
- Subscribe

### 🚜 Refactor

- Use method reference

### ⚙️ Miscellaneous Tasks

- *(avalanche-problems)* Make cornices and no distinct problem optional
- Disable MapUtilTest.testMapyrusMaps
- *(MapUtil)* Remap regions prior to 2022-10-01
- *(pom)* Update slf4j

## [5.0.0] - 2022-06-24

### 🐛 Bug Fixes

- *(blog-controller)* Change method signature
- *(email)* Fix expected result
- *(region-test)* Move element to correct position
- *(static-content)* Update url
- *(MapUtilTest)* Make tests actually run, no assume
- *(regions)* Remove server instance data from test json
- *(pdf)* Path for images
- *(region)* MapLogoColorPath and mapLogoBwPath
- *(region)* Fix naming of shape files for aran
- *(tendency)* Path!
- *(pdf)* Footer logo
- *(regions)* Remove empty part from test regions
- *(map-production)* Wrong file extension
- *(region)* To json
- *(map-production)* Remove windows commands
- *(map-production)* Variable name
- *(region)* Parameter typo
- *(server-instance)* Json export typo
- *(user)* Update user
- *(media-file)* Use region id for file path
- *(region)* Map center to double
- *(hibernate)* Pass region to query
- *(email-test)* Fix method call
- *(test)* Region
- *(media-file)* Fix mp3 file url in email
- *(media-file)* Fix mp3 file url in email
- *(listener)* Shutdown
- *(scripts)* Update scripts, fix /bin/sh call
- *(publication)* Publish report only once
- *(publication)* Create CAAMLv6 on update
- *(scripts)* Update scripts
- *(sql)* Use plural for table name
- *(publication)* Do not run telegram, emails, push multiple times for one region
- *(test)* Simple html
- *(publication)* Use correct directory for maps, show neighbor regions in region maps, limit number of thumbnail maps
- Map tests

### ⚙️ Miscellaneous Tasks

- *(test)* Remove hibernate from util classes, update tests
- *(resources)* Add resource files
- *(resources)* Update resource JSON files for regions
- *(HibernateUtil)* No Hibernate for continuous integration
- *(test)* Ignore blog tests due to hibernate
- *(test)* Ignore push tests due to hibernate
- *(test)* Ignore user tests due to hibernate
- *(static-content)* Add url for static content
- *(email-tests)* Ignore tests
- *(MapUtilTest)* Clone branch region-mgmt for avalanche-warning-maps
- *(config)* Add methods to edit server and region configs via api!
- *(map-production)* Rename ressource files
- *(mapyrus)* Get rid of german texts
- *(test)* Ignore isLatest test, fails outside season
- *(debug)* Change info to debug
- *(region-mgmt)* Add api method to retrieve all regions, fix permissions for roles
- *(region-mgmt)* Add full region configuration to user api call
- *(media-file)* Add flag to region configuration
- *(region)* Add map center lat and lng to region configuration
- *(media-email)* Finalize media email
- *(neighbor-regions)* Add neighbors to region object
- *(hibernate)* Lower case table names
- *(observations)* Change type to lob for content
- *(hibernate)* Harmonize table names (lower case, plural)
- *(hibernate)* Fix typo
- *(MapUtilTest)* Clone branch master for avalanche-warning-maps
- *(avalanche-problems)* Rename avalanche situations to avalanche problems
- *(media-file)* Send media file to additional addresses
- *(media-file)* Change subject for important emails
- *(matrix)* Add new eaws matrix and corresponding enums
- *(matrix-information)* Delete matrix information for avalanche bulletin (outside an avalanche problem)
- *(eaws-matrix)* Add new matrix fields to avalanche problem model, add sql script
- *(user)* Add method to retrieve users for admin gui
- *(json)* Add avalanche problems, remove matrix information§
- *(windows)* Check os for process builder calls
- *(publication)* Do not publish report for super regions
- *(thumbnail-maps)* Create for each region and add region id to filename
- *(matrix)* Add information of new matrix to CAAMLv6
- *(matrix)* Optional visualization of matrix
- *(matrix)* Update test resources
- *(matrix)* Update test resources
- *(wind_slab)* Rename avalanche problem wind_drifted_snow to wind_slab
- *(wind_slab)* Update resource files
- *(avalanche-problems)* Add cornices and no distinct problem images
- *(avalanche-problems)* Add cornices and no distinct problem
- *(danger-rating-modificator)* Add field to store modificator for danger rating (-, =, +)
- *(matrix)* Add values for snowpack stability, frequency and avalanche size
- *(matrix)* Update sql script

## [4.1.8] - 2022-04-11

### 🚀 Features

- *(TelegramChannelProcessorController)* Retry
- *(EmailUtil)* Add Val d'Aran

### 🐛 Bug Fixes

- *(RegionConfigurationController)* Use TypedQuery.setParameter
- *(LinkUtil)* Trailing slash in website URL
- *(config)* Albina.conf.publish-bulletins-aran=false

### 🚜 Refactor

- *(TelegramChannelUtil)* Optional.orElseThrow
- *(TelegramChannelUtil)* Throws
- Remove import
- *(HibernateUtil)* PersistenceException
- *(EmailUtil)* Simplify

### ⚙️ Miscellaneous Tasks

- *(external)* Edit api path for external server instances
- *(EmailUtil)* Aran color
- *(EmailUtil)* Lauegi@aran.org
- *(EmailUtil)* LanguageCode.getSocialMedia for Aran
- *(EmailUtilTest)* SendEmailAran
- *(EmailUtil)* Subject for Aran
- *(AuthenticationService)* Access_token expires at 03:00
- *(test)* Update tests

## [4.1.7] - 2022-03-07

### 🐛 Bug Fixes

- *(media-file)* Set media file flag in avalanche report!
- *(media-file)* Set defaut value false for new column
- *(mapyrus)* Placement of logo
- *(albina-util)* Remove space

### 🚜 Refactor

- *(publication-controller)* Remove unused function
- Remove Hibernate dependencies from model

### ⚙️ Miscellaneous Tasks

- *(media-file)* Add methods to upload media files
- *(media-file)* Add media path to server instance
- *(media-file)* Send media emails
- *(region-mgmt)* Fix multiple bugs
- *(static-widget)* Remove complete functionality
- *(static-widget)* Add sql script to delete db columns
- *(xml)* Use server instance name as operation in caaml
- *(xml)* Remove todo
- *(mapyrus)* Remove unused font definitions
- *(mapyrus)* Allow positioning of logo
- Update test resources and more
- *(server-instance)* Remove field regions
- *(publication)* Set flags in database
- *(sql)* Add script for media file columns in db

### TelegramChannelUtil

- Catch all exceptions

## [4.1.6] - 2022-02-15

### 🐛 Bug Fixes

- *(PushNotificationUtil)* Content-Encoding

### 🚜 Refactor

- Remove unused imports

### ⚙️ Miscellaneous Tasks

- *(PushNotificationUtil)* Error handling

## [4.1.5] - 2022-02-14

### 🐛 Bug Fixes

- *(blog-controller)* Fix typo
- *(tests)* Initialize hibernate
- *(subscriber)* Typo
- *(map-type)* Update comments
- *(statistic)* Use all publish regions for statistic

### 🚜 Refactor

- *(pdf)* Remove unused imports
- *(static-widget)* Remove comments
- *(BlogController)* JAX-RS Client
- *(RapidMailProcessorController)* JAX-RS Client
- *(PushNotificationUtil)* JAX-RS Client
- *(TelegramChannelProcessorController)* JAX-RS Client
- *(ObservationLwdKipService)* JAX-RS Client
- Remove obsolete CommonProcessor
- Use com.google.common.hash.Hashing
- Use java.util.Base64
- Remove org.apache.httpcomponents dependency
- Consolidate org.apache dependencies
- *(region)* Rename copyright
- *(region)* Remove comment, change copyright
- *(daytime-dependency)* Move enum
- *(mapyrus)* Region ID
- *(mapyrus)* Euregio_image_file from geodata_dir
- Fix logging

### ⚙️ Miscellaneous Tasks

- *(bulletin-regions)* Do not create regions.json
- *(region)* Reduce region model to ID
- *(social-media)* Refactor social media classes
- *(region-test)* Remove obsolete tests
- *(region)* Move configurations to region / db
- *(region)* Add fields external and externalApiUrl
- *(user)* Use region object
- *(region)* Extend region service and controller
- *(regions)* Extend class region, add class serverInstance, replace set of region ids in global variables
- *(region)* Replace region code variables in tests
- *(avalanche-bulletin-service)* Secure aineva endpoint
- *(region)* Add comments
- *(region)* Add UserRegionRole class
- *(region)* Add endpoint to get external server instances
- *(region)* Add todo
- *(region)* Remove regions.json from external files in caaml
- *(region)* Remove region specific maps and pdfs from external files in caaml
- *(region)* Use region objects nearly everywhere
- *(region)* Move simple html template name to region class
- *(region)* Add subregions and superregions to region json
- *(region)* Use color of region in pdf
- *(region)* Use region object for subscriber
- *(region)* Remove color codes for aran and albina (moved to region object)
- Update test
- *(region)* Limit social media publish always to one region
- *(region)* Move configuration parameter to server instance
- *(region)* Use push configuration
- *(region)* Update pom variables
- *(region)* Remove serverMapsUrl, serverPdfUrl, serverSimpleHtmlUrl from global variables (use variables from db)
- *(region)* Use region object in map production
- *(pdf)* Region dependent resource strings
- *(region)* Move parameters to region class
- *(region)* Remove publication provider class
- *(region)* Rename parameter for map production (logo_file)
- *(region)* Statistic for regions
- *(region)* Define secondary logo of static widget in region class
- *(region)* Add MapProductionConfiguration class
- *(region)* Extract all parameters from map production
- *(region)* Rename field external -> externalServer
- *(i18n)* Update translations

## [4.1.4] - 2022-01-16

### 🐛 Bug Fixes

- *(pdf)* Fill up table rows, fixes #233
- *(email)* Send emails only if bulletins affect region (fixes #232)
- *(imprint)* Update url for imprint

### ⚙️ Miscellaneous Tasks

- *(blog)* Add methods to publish latest blogs manually
- *(publication)* Do not use threads for emails and telegram (for testing)
- *(email)* Add logging
- *(email)* Add even more logging
- *(email)* Add return statement, add try catch block

## [4.1.3] - 2022-01-13

### 🐛 Bug Fixes

- *(telegram)* Use correct chat id

## [4.1.2] - 2022-01-13

### 🐛 Bug Fixes

- *(telegram)* Use correct chat id

## [4.1.1] - 2022-01-13

### 🐛 Bug Fixes

- *(logging)* Non-constant string concatenation
- *(pom)* Scope=test for test libraries
- *(log4j)* Sentry

### 🚜 Refactor

- *(LanguageCode)* Public static final
- *(log4j)* Xmlns
- Extract DateControllerUtil
- *(EmailUtil)* Extract variables
- *(EmailUtil)* Status

### ⚙️ Miscellaneous Tasks

- *(publish-all)* Allow manual publication of all regions without 5PM flag
- *(email-test)* Send test emails for all regions
- *(push)* Add API to trigger push notifications
- *(email)* Logging
- *(email)* Add sendEmailIssue232 test
- *(email)* SendEmailIssue232 in test mode
- *(publication)* Allow manual publication for specific languages
- *(test)* Add test methods for telegram and push

## [4.1.0] - 2022-01-05

### 🚀 Features

- *(MapUtil)* Val d'Aran
- *(PdfUtil)* Val d'Aran
- *(PdfUtil)* Val d'Aran color/logo
- *(AvalancheBulletinService)* For all publish regions
- *(controllers/jobs)* For all publish regions
- *(SimpleHtmlUtil)* Add Val d'Aran
- *(LinkUtul)* Override various base URLs via properties

### 🐛 Bug Fixes

- *(ImageTestUtils)* Error message
- "UTC"
- Tests for LinkUtil changes
- *(SimpleHtmlUtil)* Images//warning_pictos
- *(AlbinaServiceContextListener)* Do not fail without sentry
- *(pom)* Update log4j2
- *(bulletins)* Return Europa/Vienna timezone for /status
- *(XmlUtil)* Remove duplicate locRef elements in CAAMLv5
- *(test)* Caaml v5 test
- XmlUtilTest
- *(PublicationController)* CodeEuregio for PDF and simple HTML
- *(preview)* CodeEuregio→region
- *(pdf-preview)* Create all maps!
- *(pdf-preview)* Add region string to pdf filename
- *(maps)* Use euregio logo only on map for whole euregio
- String comparison
- *(pdf-preview)* Load internal reports
- *(pdf-preview)* Use bulletins with every status, return no content if no bulletin was found
- *(pdf-preview)* Do not use bulletins without regions

### 🚜 Refactor

- *(MapUtil)* Convert utility class to interface
- *(MapUtil)* Bindings
- *(MapUtil)* Extract MapyrusInterpreter
- *(MapUtil)* Extract MapSize.of
- *(MapUtil)* Simplify outputFile
- *(MapUtil)* Extract DaytimeDependency.of
- *(MapUtilTest)* Use temporary mapsPath
- *(MapUtil)* Simplify createMayrusInput
- *(MapUtil)* Simplify getDangerRatingString
- *(MapUtilTest)* Compare fd_albina_thumbnail.png
- *(mapyrus)* Remove debugging output
- *(MapUtil)* Replace drmFile with mapyrus bindings
- *(MapUtil)* Package eu.albina.map
- *(MapUtil)* Extract enums and utility classes to files
- *(MapUtil)* Extract enum MapImageFormat
- *(MapUtil)* Move unrelated functions
- *(MapUtil)* Remove unused working_dir binding
- *(MapUtil)* Remove unused language binding
- *(MapUtil)* Remove unused problem_icon_l/problem_icon_h
- *(mapyrus)* Remove dead code
- *(MapUtil)* Remove unused interreg logo
- *(MapUtil)* Extract getOutputDirectory
- *(MapUtil)* Extract AvalancheBulletin.regions
- *(MapUtil)* Use UUID for bulletin_ids
- *(MapUtil)* Rename map_level, use as string
- *(MapUtil)* Remove map_xsize
- *(MapUtil)* Separate MapType/MapLevel
- *(MapUtil)* Inline publicationTime
- *(MapUtil)* Extract BulletinRegions
- *(MapUtil)* Inline outputDirectory/preview
- AvalancheBulletin.affectsRegion
- *(PdfUtil)* Extract getFilename
- *(PdfUtil)* Try-with-resources
- *(PdfUtil)* Exception handling
- *(MapType)* Rename realm()
- *(MapUtil)* Euregio_image_file
- *(MapUtil)* Lauegi_map.png logo
- *(PdfUtl)* Extract color blue
- *(PdfUtl)* Reuse colors
- Use MapUtil.getOverviewMapFilename
- *(LinkUtil)* Move getMapsUrl and getSimpleHtmlUrl
- *(LinkUtil)* Merge getSimpleHtmlUrl/getAvalancheReportSimpleBaseUrl
- *(LinkUtil)* GetPdfLink using GlobalVariables
- *(LinkUtil)* GetSocialMediaAttachmentUrl using getMapsUrl
- *(LinkUtil)* GetWebsiteStaticFiles
- *(GlobalVariables)* GetPublishRegions
- *(GlobalVariables)* GetPublishBlogRegions
- *(AlbinaServiceContextListener)* Remove Sentry.init
- Use Objects.equals
- Use Strings.isNullOrEmpty
- Package eu.albina.rest.websocket
- Inline variable

### ⚙️ Miscellaneous Tasks

- *(email)* Ignore EmailUtilTest.sendEmail
- Update .gitignore
- *(MapUtilTest)* Add lauegi.report-2021-12-10
- *(SimpleHtmlUtil)* Add Val d'Aran templates from 2020/2021
- Sort config.properties
- *(bulletins)* Make timezone configurable for /status
- *(i18n)* Update translations
- *(hibernate)* Use HibernatUtil.getInstance().runTransaction
- *(email)* Use open sans font
- *(pdf-preview)* Generate pdf for one region only

## [4.0.10] - 2021-12-15

### 🚀 Features

- *(PushNotification)* Use ch.rasc.webpush

### 🐛 Bug Fixes

- *(pom)* Update log4j2

### 🚜 Refactor

- *(ch.rasc.webpush)* Simplify/adapt for albina

## [4.0.9] - 2021-12-11

### 🐛 Bug Fixes

- *(pom)* Update log4j2 (CVE-2021-44228)

## [4.0.8] - 2021-12-06

### 🐛 Bug Fixes

- *(AlbinaUtil)* Format publication time in UTC

### 🚜 Refactor

- *(email)* Add logging
- *(email)* Unused return code
- *(email)* Simplify getRecipientsList
- *(email)* Simplify resolveRecipientListIdByName
- *(email)* Extract getRecipientId
- *(email)* Use LanguageCode
- *(test)* Rename AlbinaUtilTest

### ⚙️ Miscellaneous Tasks

- *(test-email)* Do not use a thread
- *(email)* Add logging
- *(email)* Add test
- *(email)* Enable test
- *(email)* Link to API docs
- *(email)* Omit send_at
- *(email)* Ignore EmailUtilTest.sendLangEmail
- *(email)* Logging
- *(email)* Add java version to test mail

## [4.0.7] - 2021-12-01

### 🐛 Bug Fixes

- *(static-widgets)* Rename latest static widget files
- *(static-widget)* Use correct logo

### 🚜 Refactor

- Remove import

### ⚙️ Miscellaneous Tasks

- *(email)* Test 2021-12-01 bulletin
- *(email)* Add api method to send test email

## [4.0.6] - 2021-12-01

### 🐛 Bug Fixes

- *(static-widget)* Date in filename
- *(publication)* Use correct startDate and endDate
- *(publication)* Use ZonedDateTime instead of OffsetDateTime

### 🚜 Refactor

- *(publication)* Add debug statements

### ⚙️ Miscellaneous Tasks

- *(sentry)* MinimumEventLevel=WARN
- *(sentry)* Configure using system environment variable
- *(sentry)* Update io.sentry:sentry
- *(pom)* Downgrade jetty-maven-plugin
- *(email)* Add test

### PushNotificationService

- GET /push/key

## [4.0.5] - 2021-11-30

### 🐛 Bug Fixes

- *(log4j2)* Filepath/filename
- *(log4j2)* Albina.log.prefix whitespace
- *(scripts)* Typos in file names, add additional language files for simple htmls

## [4.0.4] - 2021-11-30

### ⚙️ Miscellaneous Tasks

- *(RapidMailProcessorController)* Logging

## [4.0.3] - 2021-11-30

### 🐛 Bug Fixes

- *(AlbinaUtil)* Local timezone

## [4.0.2] - 2021-11-30

### 🐛 Bug Fixes

- *(Ãpersistent-weak-layers)* Rename icon files

## [4.0.1] - 2021-11-30

### 🐛 Bug Fixes

- *(logging)* SEVERE -> ERROR
- *(tests)* Do not HibernateUtil.setUp
- *(GlobalVariables)* DirectoryOffset

### 🚜 Refactor

- LanguageCode.ENABLED
- AvalancheBulletin.getHighestDangerRating
- *(messenger-people)* Delete GlobalVariables.targeting
- Tendency.getSymbolPath
- AvalancheSituation.getSymbolPath
- Aspect.getSymbolPath
- LinkUtil
- *(LanguageCode)* Use EnumSet

### ⚙️ Miscellaneous Tasks

- Delete tmp files at 3 AM, closes #222
- *(EmailUtil)* Logging
- *(sentry)* DiagnosticLevel=warning

## [4.0.0] - 2021-08-09

### 🚀 Features

- Delete interreg logo, add euregio logo, place euregio logo in maps
- Change folder structure to support multiple versions per day, include euregio logo, fix time change bug

### 🐛 Bug Fixes

- *(simple-html)* Fix links
- *(MapUtilTest)* Do not modify production properties
- *(user-service)* Fix path of new methods
- *(user-model)* Constructor
- Use isEmpty instead of isBlank
- *(gitlab-ci)* Do not allow errors on production build (java 8)
- *(pom)* Delete scope test from bytebuddy and objenesis dependency
- *(hibernate-query)* Typos
- *(test)* Use instant instead of zoneddatetime
- *(publication-date)* Use correct UTC time for publication datetime
- *(region-controller-test)* Delete comment
- *(hibernate-spatial)* Rename packages due to changes in hibernate-spatial 5.4
- *(caamlv6)* Use report publication time for all thumbnail map urls
- *(pom)* Do not filter binary files such as .ttf files
- *(statistics)* Use correct number of columns
- Configure log4j2.xml
- *(maps)* Use correct commands to convert image formats
- *(tests)* Change publication date of test bulletin, check for null after pdf creation
- *(pdf)* Check writer for null before closing

### 🚜 Refactor

- Delete ununsed imports and variables, fix constructor for Integer
- Add ids, remove todos
- *(string)* Use isBlank method
- *(chat-message)* Delete obsolete import
- *(albina-util)* Delete obsolete import
- Delete obsolete import
- Add logging
- *(xml-util)* Delete obsolete method
- Delete obsolete TODO
- *(persistence)* Delete property provider_class
- *(avlanche-bulletin)* Delete obsolete TODO
- *(MapUtil)* Load mapyrus files as resources
- *(GlobalVariables)* Remove unused scriptsPath
- Add missing @java.lang.Override annotations
- *(MapUtil)* Load mapyrus fonts as resources
- *(PdfUtil)* Use images as resources
- *(localImagesPath)* Delete obsolete parameter localImagesPath
- *(UtilsTest)* Use org.junit.Assert.assertTrue
- *(tmp)* Specify tmp directory

### ⚙️ Miscellaneous Tasks

- *(vscode)* Add config
- *(i18n)* Add config for i18n-ally
- *(sentry)* Only allow our own urls for errors
- *(i18n-ally)* Delete config
- *(messenger-people)* Delete all code for messenger people
- *(twitter)* Delete code for twitter
- Update dependency commons-beanutils
- *(region-service)* Remove
- *(pom.xml)* Update apache-commons, quartz scheduler
- *(shipment)* Delete
- Clean up social media
- *(user)* Add user service, move methods from authentication service
- *(user-controller)* Move method from authentication controller
- *(user-service)* Add methods for regions and roles
- *(user)* Create, delete user
- Update to mapyrus 2.106
- *(pom)* Update dependencies, add min maven version, add min java version
- *(pom)* Change required java version
- *(pom)* Change source and target version
- *(pom)* Target java 11
- *(pom)* Update mockito to v3.9.0
- *(pom)* Add dependencies byte-buddy and objenesis (for mockito), update mockito version
- *(test)* Ignore useless tests
- *(pom)* Update log4j2
- *(pom)* Update hibernate to 5.5.0.Alpha1, update commons-io  to 2.8.0
- *(pom)* Update guava to v30.1.1-jre
- *(pom)* Update freemarker to 2.3.31
- *(pom)* Keep Java 8 support
- *(java-time)* Remove joda time dependecy
- *(a11y)* Add translations for aspects
- *(i18n)* Update translations
- *(a11y)* Set alt texts for pdf images
- *(pom)* Update dependencies, add maven-war-plugin
- *(authentication-controller)* Use ZonedDateTime for method getValidityDate
- *(avalanche-bulletin)* Use ZonedDateTime for method getValidityDate
- *(bulletin)* Remove db type for datetime
- *(instant)* Use instant, create correct format of datetime objects for admin gui
- *(pom)* Remove log4j-slf4j-impl dependency
- *(publication-time)* Without nanos
- *(pom)* Add slf4j-log4j-impl
- *(pom)* Add hibernate-java8
- *(log4j2)* Rename config file
- *(tests)* Enable tests
- *(pom)* Reset hibernate version, clean logging dependencies
- *(hibernate)* Set up in constructor
- *(persistence)* Add provider class c3p0
- *(pom)* Delete entity manager
- *(test)* Enable region controller tests
- *(jts)* Update locationtech packages (needed for hibernate)
- *(pom)* Delete hibernat-java8 (already included), use latest prod version for hibernate 5.4.32
- *(region-controller-test)* Ignore tests
- *(statistic-test)* Update resource file
- *(pom)* Albina1rz:3306/albina_dev
- *(PdfUtil)* Use OpenSans from resources/fonts
- *(preview)* Allow PDF preview for bulletins
- *(i18n)* Update translations
- *(pom)* Albina1rz:3306/albina
- *(pom)* /var/www/avalanche.report
- *(pom)* Mysql://localhost:3306
- *(statistics)* Return csv file
- *(statistics)* Secure endpoint (admin, forecaster, foreman)
- *(statistics)* Remove matrix information from danger rating above and below (only available for each avalanche problem)
- *(observations)* Add csv export

### Core

- *(java-time)* Use zoneddatetime for publication and update jobs

## [3.1.9] - 2021-03-29

### ⚙️ Miscellaneous Tasks

- *(PushNotificationUtil)* No singleton instance

## [3.1.8] - 2021-03-29

### 🐛 Bug Fixes

- *(mapyrus)* Fix OpenTypeFont.getFontDefinition performance
- *(mapyrus)* Fix OpenTypeFont.getFontDefinition performance

### 🚜 Refactor

- MapUtilTest.assumeMapsPath

### ⚙️ Miscellaneous Tasks

- *(GlobalVariables)* Local maps-path/map-production-url
- Add org.mapyrus.font.OpenTypeFont for patching
- MapUtilTest.testMapyrusMapsDaylightSavingTime
- MapUtilTest w/o Hibernate
- *(MapUtilTest)* GlobalVariables.setMapProductionUrl
- *(MapUtilTest)* Compare with reference image
- *(MapUtilTest)* Only run one Mapyrus test
- *(BlogController)* Throw exception when fetching fails
- *(BlogController)* Tune logging
- *(BlogController)* Tune logging
- *(PushNotificationUtil)* Tune logging
- *(PushNotificationUtil)* New HttpClient instance
- *(PushNotificationUtil)* Test byte array length
- Debug logging for blogs and push notifications

## [3.1.7] - 2021-03-16

### 🐛 Bug Fixes

- *(PushSubscriptionController)* Detached entity passed to persist
- *(PushSubscriptionController)* AlbinaException: HTTP/1.1 201 Created

### ⚙️ Miscellaneous Tasks

- *(PushNotification)* Send to correct regions
- *(statistics)* Add additional avalanche problems and matrix information to statistics output
- *(PublicationController)* Tune logging

## [3.1.6] - 2021-03-09

### ⚙️ Miscellaneous Tasks

- *(SocialMediaUtil)* Use fd_albina_thumbnail.jpg only for Web Push

## [3.1.5] - 2021-03-09

### 🚀 Features

- *(push-notifications)* Send new blog posts

### 🐛 Bug Fixes

- Properly install org.bouncycastle in Tomcat
- Properly install org.bouncycastle in Tomcat
- *(BlogController)* LastFetch by blogId

### 🚜 Refactor

- Remove Hibernate ENVERS configuration
- Use Stream.anyMatch
- Use Stream.findFirst
- Use Collection.removeIf
- *(AlbinaUtil.getValidityDateString)* Offset parameter
- *(SimpleHtmlUtilTest)* Assert in unit test
- *(BlogController)* Use Table for blog IDs/URLs
- Final fields
- Logging

### ⚙️ Miscellaneous Tasks

- *(SocialMediaUtil)* Use fd_albina_thumbnail.jpg
- *(push-notifications)* Delete subscriptions after 10 failed attempts
- *(push-notifications)* Add bulletin URL to notification
- *(push-notifications)* "image"

## [3.1.4] - 2021-02-16

### 🚀 Features

- *(observations)* Proxy gis.tirol.gv.at for lwdkip

### 🐛 Bug Fixes

- *(PushNotification)* Use correct bouncycastle version
- Update mysql-connector-java to fix timezone issue
- *(BlogController)* FetchImages
- *(BlogController)* BlogIdSouthTyrolIt typo

### 🚜 Refactor

- *(PushNotification)* Exclude some unneeded dependencies from nl.martijndwars:web-push
- Migrate to json-schema-validator
- *(BlogController)* Use Stream.findFirst

### ⚙️ Miscellaneous Tasks

- *(simple-html)* Use webp
- *(push-notifications)* Keys for all environments
- *(observations)* Getenv ALBINA_JWT_SECRET
- Add version to GlobalVariables and index.jsp
- Remove GIT_VERSION from tomcat path

## [3.1.3] - 2021-02-05

### 🐛 Bug Fixes

- *(telegram-channel)* Send blogs without image, fix #211

### Sentry

- Add missing artifact io.sentry:sentry-log4j

## [3.1.2] - 2021-02-04

### 🐛 Bug Fixes

- *(TelegramChannelProcessorController)* Use URIBuilder

## [3.1.1] - 2021-02-04

### 🚀 Features

- *(observations)* GET
- *(observations)* POST
- *(observations)* PUT
- *(observations)* DELETE
- *(observations)* Return observation object

### 🐛 Bug Fixes

- *(email)* Add link to avalanche problems (pm)
- *(blogs)* Set config for blogs
- *(AuthenticationController)* TokenEncodingSecret

### 🚜 Refactor

- Use LocalDateTime for Observation

### ⚙️ Miscellaneous Tasks

- *(blogs)* Check for new blogs every 10 minutes
- *(blog)* Update comment
- *(pdf)* Show all avalanche problems
- *(email)* Add all avalanche problems
- *(simple-html)* Add all avalanche problems
- *(observations)* Add observations.sql

## [3.1.0] - 2021-01-25

### 🚀 Features

- Add support for push-notifications

### 🐛 Bug Fixes

- *(BlogController)* Messages.getString("avalanche-report.name")
- *(BlogController)* Object.getString("title")
- Rename SQL column KEY to P256DH
- *(pdf)* Fix #209
- *(email)* Add tendency text

### 🚜 Refactor

- *(BlogController)* POJO for Blogger API
- Use LanguageCode.getBundleString
- Extract interface SocialMediaUtil

### ⚙️ Miscellaneous Tasks

- Persist push-notifications
- Remove push-notifications
- Fetch push-notifications for newsletter
- *(push)* Send after update
- *(PushSubscription)* Add subscribeDate
- *(PushSubscription)* Add failedCount
- *(i18n)* Update translations
- *(email)* Add links to danger patterns and avalanche problems

## [3.0.12] - 2020-12-21

### 🐛 Bug Fixes

- *(social-media)* Only publish on social media if maps were created
- *(rapidmail)* Unsubscribe link

### 🚜 Refactor

- *(avalanche-bulletin)* Remove obsolete comment
- *(textcat)* Remove obsolete comment
- *(templates)* Delete unused templates

### ⚙️ Miscellaneous Tasks

- *(notes)* Add sql script
- *(notes)* Make notes persistent
- *(univie)* Delete map production code for univie
- *(import-sql)* Delete obsolete queries

## [3.0.11] - 2020-12-12

### 🐛 Bug Fixes

- *(publication)* Order of publication steps

## [3.0.10] - 2020-12-11

### 🐛 Bug Fixes

- *(social-media)* Delete duplicate slashs in attachment url

## [3.0.9] - 2020-12-11

### 🐛 Bug Fixes

- *(social-media)* Attachment url

## [3.0.8] - 2020-12-09

### 🐛 Bug Fixes

- *(social-media)* Use avalanche.report as base url for images

## [3.0.7] - 2020-12-09

### 🐛 Bug Fixes

- *(simple-html)* Create simple html for whole euregio

## [3.0.6] - 2020-12-09

### 🚜 Refactor

- *(junit)* Move messenger people test to own class

### ⚙️ Miscellaneous Tasks

- *(telegram-channel)* Add logging information on error!
- Choose file extension for map files
- *(telegram-channel-test)* Add test
- *(messenger-people)* Add logging in case of error

## [3.0.5] - 2020-12-07

### 🐛 Bug Fixes

- *(maps)* Tranparency of overlays

## [3.0.4] - 2020-12-07

### 🐛 Bug Fixes

- *(i18n)* Decreasing tendency in DE
- *(email)* Check for null for tendency
- *(telegram-channel)* Message for non update

### 🚜 Refactor

- *(messengerpeople)* Add logging information
- *(telegram-channel)* Add logging

## [3.0.3] - 2020-12-04

### 🐛 Bug Fixes

- *(map)* Add euregio logo on overview maps
- *(map)* Create transparency in PNG overlays
- *(messengerpeople-controller)* Stupid lt / gt error for targeting

### ⚙️ Miscellaneous Tasks

- Add user Andrea to tests
- *(map)* Add logging info

## [3.0.2] - 2020-12-03

### 🚀 Features

- XmlUtilTest.createOldCaamlFiles

### 🐛 Bug Fixes

- *(social-media)* Send only in DE, IT, EN to social media channels

### ⚙️ Miscellaneous Tasks

- *(tests)* Update junit to 4.13.1

## [3.0.1] - 2020-11-20

### 🐛 Bug Fixes

- Decode path to scripts
- Change filename of xml files in latest directory
- *(XmlUtil)* Check datetime for null

### ⚙️ Miscellaneous Tasks

- Change map production url for production environment to local map production
- Enable map production for aran
- Use scripts from resource folder

## [3.0.0] - 2020-11-12

### 🚀 Features

- Create CAAMLv5 and CAAMLv6 during publication
- Allow manual selection of danger rating
- Add danger rating direction to json schema
- XmlUtilTest.createOldCaamlFiles

### 🐛 Bug Fixes

- Generate valid CAAMLv6
- *(id)* Add prefix to id
- Update caaml v6 example file (fix ids)
- Check avalanche situations for null
- Set complexity of bulletin in copy method
- JSON schema, JSON unit tests, drop jsonassert
- Use ID of bulletins without prefix
- *(CAAML)* Replace xml:id, xml:lang with native types
- *(i18)* Use correct ResourceBundle
- *(MessagesBundle_de)* Encoding
- Persistent weak layers
- *(MessagesBundle)* Encoding
- AlbinaUtil.getRegionName for ""
- Test
- Test
- Allow map production only for EUREGIO
- Return no content for bulletins api call if region is not defined
- Use correct i18n string for simple html title
- To not serialize user image to JSON
- Do not create sql scripts
- Copy (latest) files even if map production is not enabled
- DangerPatternTest
- Add region to URL for other languages in simple html
- Add whitespace in simple html
- Add region to URL for other languages in simple html
- Text encoding for simple html
- Text encoding for simple html
- *(i18n)* Change expected test result
- *(i18n)* Translations for danger patterns
- *(test)* Enumerations test

### 🚜 Refactor

- *(Regions)* Collection class
- Use ResourceBundle for language dependent string
- *(DangerPattern)* Use ResourceBundle
- *(DangerRating)* Use ResourceBundle
- DangerRating#getDangerRatingColor
- Add comment
- Add new line at end of file
- *(API)* Version parameter for CAAML v6
- LanguageCode.getBundle
- Add i18n file for region names, delete region name maps from AlbinaUtil
- Move DangerRating.getDouble
- Remove comment
- Delete comment
- Delete comments
- Move sql scripts to own folder

### ⚙️ Miscellaneous Tasks

- Add sql files
- *(JSON)* Update json schema and example file
- *(CAAML)* Use danger rating type in avalanche situation
- Allow definition of the direction for the danger rating of an avalanche problem
- Delete variable isManualDangerRating
- Add RegionTest
- *(Region)* Parse from GeoJSON
- *(Region)* Parse regions from GeoJSON
- *(MapUtil)* Test createBulletinRegions
- Add complexity as an attribute to caaml v6
- Add transifex config
- Extend API to deliver CAAMLv6
- *(CAAML)* Trim texts
- *(CAAML)* Trim region name
- Add support for Aran
- Add translations
- Add Ivan as user
- Add Montse, refactor region names
- Use euregio as own region
- Update translations for regions
- Produce JSON for euregio only
- Return status ok after password change
- Update translations!
- Map production without thread, catch exception
- Use dev environment for map production
- Move elevation to daytime description
- Add users to junit test
- Update translations
- Add FR to list of languages
- Add icons to simple html
- Add special alert to simple html
- *(simple-html)* Remove regions, add avalanche problem text
- *(i18n)* Replace i18n property files with xml files (utf-8 support)
- *(i18n)* Add XML support for resource bundles
- *(i18n)* Use xml property files for translations
- *(i18n)* Update translations

### MapUtil

- Produce mapyrus maps sequentially
- Tune logging
- Create combined am/pm maps
- Replace Mapyrus includes

### Maven

- Update name, description, url

### UtilTest

- Use AvalancheBulletin.readBulletin

### Web.xml

- Update name

## [2.1.4] - 2020-04-30

### 🚀 Features

- Add tendency to caaml
- Update util tests
- *(simpleHTML)* Add links to other languages and link to standard view
- *(TelegramChannel)* Add telegram channels for publication

### 🐛 Bug Fixes

- *(AvalancheReportController)* Concurrent modification of map
- *(AvalancheBulletinService)* Return empty JSON
- Update resource files after publication finished
- *(scripts)* Update scripts
- *(GlobalVariables)* Fix wrong text in simple html.
- *(CAAML)* Add srcRef to MetaData
- *(CAAML)* LocRef belongs after bulletinResultsOf
- *(CAAML)* Empty <AvProblem/>
- *(MessengerPeopleProcessorController)* Add logging
- *(ExtFiles)* Add description to ext files in CAAML
- *(AvalancheSituation)* Initialize avalanche situations correctly
- *(AvalancheBulletin)* LinkReferenceURI

### 🚜 Refactor

- Fix comments
- *(AvalancheReportController)* Use putAll()
- Migrate to openjson
- *(AvalancheBulletin)* Extract getAvProblem method
- Extract CaamlValidator.validate()
- *(CaamlVersion)* Merge generation code
- AvalancheBulletinDaytimeDescription.getAvalancheSituations

### ⚙️ Miscellaneous Tasks

- Remove config parameter createCaaml (always create it)
- *(XmlUtilTest)* Add createValidCaaml
- *(CAAML)* Remove unused xmlns:app
- *(CAAML)* Add xmlns:schemaLocation
- *(CAAMLv6)* Allow creation of CAAMLv6
- *(MatrixInformation)* Add matrix information to avalanche problems, allow 5 avalanche problems
- *(AvalancheBulletin)* Add text part highlights
- *(terrainFeature)* Add field terrain feature to avalanche situation and bulletin daytime description
- *(TelegramChannel)* Send new blog posts to telegram channel
- *(Statistics)* Allow AM/ṔM entries for each day in statistics

## [2.1.3] - 2020-02-14

### 🐛 Bug Fixes

- Fix blog url in messengerpeople string

## [2.1.1] - 2020-02-12

### 🐛 Bug Fixes

- Bulletin status enum
- Bulletin status test

## [2.1.0] - 2020-02-11

### 🚀 Features

- MapUtil.createMapyrusMaps
- *(MapUtil)* Create regions file
- Save JSON file during publication

### 🐛 Bug Fixes

- *(pom.xml)* Exclude slf4j-simple
- *(MapUtilTest)* Slowness
- Create JSON at the end of the publication
- Refactor
- Create small json
- *(MapUtil)* Fix bulletin index
- *(JsonUtil)* Typo
- *(CAAML)* Typo "begionPosition"
- *(CAAML)* "uom" is required for "elevationRange"
- *(CAAML)* Add srcRef to MetaData
- *(CAAML)* LocRef belongs after bulletinResultsOf
- *(CAAML)* Empty <AvProblem/>
- *(BlogController)* NPE for blogs w/o images
- *(BlogController)* Incorrect URL for IT-32-BZ/it
- Sort bulletins by danger rating
- *(pom)* Fix local fonts path
- Fix util test
- Delete unused strings for fonts
- Initialize fonts only once
- Create fonts for each pdf document (necessary)
- *(StatisticsController)* Ignore tests
- *(UtilTest)* Define validity date and publication time in tests
- *(PdfUtil)* Close file handlers, refactor
- Allow regions to change owner (if no microregion of original owner is present anymore)

### 🚜 Refactor

- AvalancheBulletin.readBulletin
- Use GlobalVariables.regions
- MapUtil.getOverviewMapFilename
- Pass exceptions to logging
- Remove unused imports
- Use StandardCharsets
- *(AuthenticationController)* Add algorithm field
- *(CaamlValidatorTest)* Rename and simplify
- *(StatisticsControllerTest)* Simplify and assert
- *(AvalancheBulletinJsonValidatorTest)* Simplify
- *(SnowProfileJsonValidatorTest)* Simplify
- *(AvalancheBulletinTest)* Simplify
- *(JsonValidator)* Use Resources.getResource
- *(MapUtilTest)* Use Resources.getResource
- *(AvalancheBulleton)* Use Resources.toString
- No need to decode date parameters
- *(AvalancheBulletin)* Extract getAvProblem method
- *(BlogController)* Drop StringBuilder

### 🎨 Styling

- Add .editorconfig
- Re-indent using tabs
- Use editorconfig for yml
- Trim trailing whitespace

### 🧪 Testing

- MapUtil.getOverviewMapFilename

### ⚙️ Miscellaneous Tasks

- *(MapUtil)* Always generate pdf/png/webp/jpg maps
- *(MapUtil)* Handle daytime dependency
- *(MapUtil)* Decrease coordinate precision
- *(scripts)* Copy .webp files
- *(MapUtil)* Use paths from GlobalVariables
- *(MapUtil)* Run map production in parallel
- *(MapUtil)* Add logging
- *(CorsFilter)* Tune logging
- *(MapUtil)* Add logging
- *(AlbinaUtil)* Tune logging
- *(log4j)* Enable Sentry
- *(MapUtil)* Add logging
- Default to locally produced mapyrus maps
- *(build)* Add guava dependency
- *(XmlUtilTest)* Add createValidCaaml
- *(CAAML)* Remove unused xmlns:app
- *(CAAML)* Add xmlns:schemaLocation

## [2.0.4] - 2019-12-03

### 🚀 Features

- Send blog posts to rapidmail and messengerpeople
- Add sentry
- Sort bulletins by danger rating

### 🐛 Bug Fixes

- Fix scripts
- Organize imports
- Remove comments
- Ignore test
- Delete elevation if favourable situation was selected

### AvalancheReport

- Add index on DATE

## [2.0.3] - 2019-11-17

### 🐛 Bug Fixes

- Message for messengerpeople

## [2.0.2] - 2019-11-16

### 🐛 Bug Fixes

- Chmod to 755 for maps!
- Send bulletin to messengerpeople

## [2.0.1] - 2019-11-15

### 🐛 Bug Fixes

- Link next day in simple html
- Fix url to fetch blog posts
- Map urls in email
- Messengerpeople api fix

## [2.0.0] - 2019-11-14

### 🚀 Features

- Add textcat ids to statistics export

### 🐛 Bug Fixes

- Fix export of configuration parameter
- Use correct paths to images.
- Set timout for map production to 20min.
- Check for timeout at map production
- Add info logging
- Improve logging for blog job
- Connection timeout for map production
- Print stack trace
- Define connect timeout for map production (20min)
- Change map production url to localhost:8008
- Fix delete scripts
- Change file permissions
- File permissions
- File permissions
- Log output of scripts
- Update scripts (chmod)
- Remove connection timeout
- Positioning of euregio logo in pdfs
- Positioning of euregio logo in pngs
- Use correct urls for images and links in simple html
- Remove standard map production url
- Use names of regions instead of ids in simple html
- Use blogger api instead of blogcache
- Use en region names
- Use yyyy-MM-dd_HH-mm-ss as directory name for bulletin versions
- Copy am_regions.json and pm_regions.json to latest
- Fix copying of maps and permissions to latest dir
- Fix wrong parameters for copyLatestMaps script, fix script itself
- Allow map production on univie server
- Update URL for AM map in simple html
- Fix script to copy maps from univie server
- Delete additional / in path to maps on univie server
- Copying of maps!
- Copy maps from univie script
- Headline in static widget
- Use correct images for email
- Date links in simple html

### AvalancheReport#toJSON

- Simplify bool handling

### Web.xml

- Add listener-class

## [1.0.0] - 2018-11-30

### Refactor

- Create util class for xml and json.

<!-- generated by git-cliff -->
