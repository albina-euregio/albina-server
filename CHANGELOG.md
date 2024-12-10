# Changelog

<!-- Update using `git-cliff -u -p CHANGELOG.md -t <TAG>` before creating new tag <TAG> with git. -->

## [7.0.6] - 2024-12-09

### 🐛 Bug Fixes

- _(danger-sources)_ Remove MySQL check from DANGER_SIGN/TERRAIN_TYPE
- _(PublicationJob)_ Run sendToAllChannels after directory update

## [7.0.5] - 2024-12-05

### 🐛 Bug Fixes

- _(AvalancheReportController)_ PDF preview

## [7.0.4] - 2024-12-05

### 🐛 Bug Fixes

- _(HibernateUtil)_ Transaction.rollback for NoResultException

## [7.0.3] - 2024-12-05

### 🐛 Bug Fixes

- _(BlogController)_ NonUniqueResultException

### ⚙️ Miscellaneous Tasks

- _(pom)_ Update HikariCP from 3.2.0 to 6.2.1
- _(pom)_ Update log4j2

## [7.0.2] - 2024-12-02

### 🚀 Features

- _(BlogController)_ Send new tech blogs

### 🐛 Bug Fixes

- _(email)_ Remove pdf button from email due to dynamic pdf creation per warning region, closes #307
- _(email)_ Tests
- _(email)_ Remove pdf button from email due to dynamic pdf creation per warning region, closes #307
- _(email)_ Remove pdf link due to dynamic pdf creation
- _(TextToSpeech)_ Text -> ssml
- _(TextToSpeech)_ German

### 🚜 Refactor

- _(BlogController)_ Use CriteriaBuilder
- _(BlogController)_ Do not return Optional
- _(TextToSpeech)_ Migrate to Google REST API
- _(TextToSpeech)_ Extract constants
- Migrate to GenericObservation
- _(observations)_ Remove obsolete code

### 📚 Documentation

- _(Changelog)_ Use git-cliff to generate a changelog

### ⚙️ Miscellaneous Tasks

- _(pom)_ Update jackson to 2.18.1
- Upgrade to Hibernate 6.6.2
- _(pom)_ Update mariadb-java-client to 3.5.0

## [7.0.1] - 2024-11-13

### ⚙️ Miscellaneous Tasks

- _(AvalancheReportController)_ Filter bulletin.affectsRegionWithoutSuggestions for PDF

## [7.0.0] - 2024-11-04

### 🚀 Features

- _(TextToSpeech)_ Enable for ES and CA
- _(PublicationJob)_ Parallelize publication by region
- _(StatisticsService)_ POST /statistics/vr
- Add StressLevel
- Add StressLevel
- _(StressLevelService)_ Team stress levels
- _(AvalancheBulletinService)_ Create bulletin PDF on demand

### 🐛 Bug Fixes

- _(PublicationJob)_ Try to use custom ForkJoinPool
- _(PublicationJob)_ Try to use custom ForkJoinWorkerThreadFactory
- _(PublicationJob)_ Try to use CompletableFuture.runAsync
- _(publication)_ Also publish super regions
- _(TextToSpeech)_ Key speech.tendency.null
- _(user)_ Udpate comments
- _(validity)_ Test
- _(validity)_ Update tests
- _(strategic-mindset)_ Region test
- _(strategic-mindset)_ Region test
- _(regions)_ Test
- _(statistics)_ Use microregion ids
- _(statistics)_ Test
- _(statistics)_ Test
- _(StressLevelService)_ JSON serialization
- _(avalanche-types)_ Typo
- _(avalanche-types)_ Tests
- _(stress-level)_ Test
- _(stress-level)_ CriteriaQuery
- _(stress-level)_ Exclude deleted users from team
- _(danger-source)_ Typo, variant db table
- _(danger-sources)_ Db script
- _(danger-source)_ Typos, rename status to danger_source_status
- _(danger-sources)_ Typo probability
- _(danger-sources)_ Fix cascade type and save method for danger sources
- _(danger-sources)_ Parsing dates and jackson serialization
- _(danger-sources)_ Ignore getter methods in eawsMatrixInformation
- _(danger-sources)_ Case insensitive aspect enum
- _(danger-sources)_ Typo in TerrainType
- _(danger-sources)_ Time range for getVariantsForDangerSource
- RegionTest.testCreateObjectFromJSONAndBack
- _(danger-sources)_ Check duplicate regions only for same dangerSourceVariantType
- _(caamlV5)_ Use lower case string for aspects
- _(aspects)_ ToLowerCaseString
- _(AvalancheProblemType)_ No_distinct_problem
- _(no-distinct-avalanche-problem)_ Use same string
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
- _(MediaFileService)_ Move functions
- _(user)_ Use change and reset consistently
- Remove obsolete tests and resources
- Remove unused certificates/emailsys.jks
- _(AvalancheBulletinController)_ Remove redundant contains checks
- AlbinaUtil.validityStart
- Remove apps.tirol.gv.at/lwd/produkte/json_schema prefix
- Simplify Stream API call chain
- _(danger-sources)_ Remove obsolete imports
- _(danger-sources)_ Rename status to danger_source_variant_status
- _(danger-sources)_ Import avalanche type
- _(danger-sources)_ Rename originalDangerSourceVariantId
- Eu.albina.model.EawsMatrixInformation.compareTo
- _(imports)_ Organize imports!
- TextToSpeech.createScript
- _(SimpleHtmlUtil)_ Use AvalancheReport.getHtmlDirectory
- PublicationJob.createSymbolicLinks without shell script
- _(PdfUtil)_ Move methods
- Remove AlbinaUtil.runUpdateFilesScript
- AvalancheReport.isUpdate and AvalancheReport.getPublicationDate
- HasPublicationDate, HasValidityDate

### ⚙️ Miscellaneous Tasks

- _(LanguageCode)_ ReplaceAranes
- _(user)_ Allow deletion of users via db flag, add columns to user table
- _(i18n)_ Update translations
- Update TextToSpeechTest
- Update SimpleHtmlUtilTest
- _(scripts)_ Create symbolic links
- _(user)_ Allow to update image
- _(user)_ Add method to reset password
- _(user)_ Encrypt password during creation of user
- _(user)_ Allow to update own user
- Sort texts by language to allow caching of API calls
- Sort texts by language to allow caching of API calls
- Sort texts by language to allow caching of API calls
- _(persistence)_ Migrate to mariadb-java-client
- _(persistence)_ Migrate to hibernate-hikaricp connection pool
- _(bom)_ Update guava to 33.2.0
- _(pom)_ Upgrade slf4j 2.0.12
- _(pom)_ Fix mariadb-java-client exclusion
- _(pom)_ Fix testing dependencies scopes
- _(pom)_ Byte-buddy is needed by hibernate
- Access-Control-Expose-Headers
- Upgrade to Hibernate 6.5.2
- _(validity)_ Use 12AM as am/pm split in caamlv5
- _(validity)_ Change startDate and endDate in tests
- _(validity)_ Update method to get day of validity
- _(validity)_ Update validity and tendency date
- _(pom)_ Update jetty-maven-plugin
- _(strategic-mindset)_ Add model and logic
- _(avalanche-type)_ Add db column, enum, sql script
- _(regions)_ Add enableWeatherbox
- _(statistics)_ Add region to endpoint
- _(pom)_ Update jetty-maven-plugin
- _(persistence)_ Org.hibernate.dialect.MariaDBDialect
- _(avalanche-types)_ Add to caaml
- _(stress-level)_ Add to region config
- _(danger-source)_ Add model
- _(danger-sources)_ Add controller
- _(StressLevelService)_ Ensure @ is not present for team
- _(danger-sources)_ Add compare method
- _(danger-sources)_ Add DangerSource to DangerSourceVariant model
- _(danger-sources)_ Implement controller and service
- _(danger-sources)_ Add to region
- _(desktop.ini)_ Remove
- _(persistence)_ Remove dialect, change property names to jakarta
- _(danger-source)_ Add sql to create danger source db tables
- _(danger-sources)_ Add title!
- _(danger-sources)_ Rename controller, endpoint to fetch variants of specific source
- _(danger-sources)_ Remove variants from danger source model
- _(danger-sources)_ Update sql script
- _(danger-sources)_ Add danger source controller
- _(danger-sources)_ Update api urls
- _(danger-sources)_ Add api endpoint to get danger sources
- _(danger-sources)_ Use LocalDateTime
- _(danger-sources)_ Java.time.Instant
- _(danger-sources)_ Remove type from variant
- _(danger-sources)_ Extend service and controller
- _(danger-sources)_ Jackson ignore unknown properties
- _(danger-sources)_ Prevent duplicate regions within one dangersource
- _(danger-sources)_ Add gliding snow activity value to db
- _(danger-sources)_ Handle enums as strings in object mapper
- _(danger-sources)_ Update danger source
- _(danger-sources)_ Change type of elevation from int to Integer
- _(danger-sources)_ Use custom serializer for variant
- _(danger-sources)_ Add danger source variant type, load status
- _(danger-sources)_ Remove field analysisDangerSourceVariantId
- _(aspect)_ Change toString to uppercase
- _(vs-code)_ Add extension recommendations!
- _(vs-code)_ Add words to dictionary
- _(AvalancheReportController)_ PDF preview using HTTP POST
- _(scripts)_ Remove EUREGIO handling from updateFiles.sh
- _(scripts)_ Remove region dependency from updateLatestFiles.sh
- Omit `chmod 755`, rely on correct UMASK instead
- _(TextToSpeech)_ Write SSML file
- Omit `chmod 755`, rely on correct UMASK instead
- _(AvalancheBulletinPublishService)_ Remove separate pdf/html/map/caaml publication endpoints

### Hack

- _(mapyrus)_ Disable tests

## [6.2.0] - 2024-04-08

### 🚀 Features

- _(PdfUtil)_ Replace am/pm with "earlier" and "later" from CAAMLv6
- _(EmailUtil)_ Replace am/pm with "earlier" and "later" from CAAMLv6
- _(SimpleHtmlUtil)_ Replace am/pm with "earlier" and "later" from CAAMLv6
- _(PdfUtil)_ Replace am/pm with "earlier" and "later" from CAAMLv6
- _(EmailUtil)_ Replace am/pm with "earlier" and "later" from CAAMLv6
- _(SimpleHtmlUtil)_ Replace am/pm with "earlier" and "later" from CAAMLv6

### 🐛 Bug Fixes

- Unit tests
- _(TextToSpeech)_ Ssml_gender computation
- _(AlbinaUtil)_ NPE
- Unit tests
- _(log4j2)_ Leading whitespace on production w/o prefix
- _(pdf)_ Check for gliding snow
- _(bulletin)_ Move published regions to saved regions in case of change
- RegionTest
- _(MapUtilTest)_ Avalanche-warning-maps update
- _(MapUtilTest)_ Avalanche-warning-maps update
- _(MapUtilTest)_ Avalanche-warning-maps update

### 🚜 Refactor

- _(AlbinaUtil)_ Public interface AlbinaUtil
- _(AlbinaUtil)_ Use StandardCharsets.UTF_8
- _(AlbinaUtil)_ Extract getScriptPath
- _(AlbinaUtil)_ Inline getDangerPatternText
- LanguageCode.getTendencyDate
- LanguageCode.getLongDate
- _(AlbinaUtil)_ Move encodeFileToBase64Binary to unit test
- LinkUtil.getBulletinLink
- _(AlbinaUtil)_ GetPublicationDate returns Instant
- _(AlbinaUtil)_ Inline greyDarkColor
- _(AlbinaUtil)_ NewShellProcessBuilder
- _(AlbinaUtil)_ Rename getPublicationDateDirectory
- Remove unused daytime.\* strings
- Remove unused daytime.\* strings

### ⚙️ Miscellaneous Tasks

- _(AvalancheReportController)_ Tune logging
- _(AvalancheReportController)_ One transaction for saveBulletins
- _(AvalancheReportController)_ Synchronized saveBulletins
- _(AvalancheReportController)_ SaveBulletins returns all bulletins
- _(i18n)_ Update translations
- _(i18n)_ Update translations
- _(TextToSpeech)_ Tune German voices
- _(i18n)_ Update translations
- _(pom)_ Remove unused javax.mail
- _(pom)_ Update log4j2
- _(RegionService)_ Enable GET for all authenticated users
- _(pom)_ Remove c3po/HikariCP dependency from quartz
- _(bom)_ Update guava to 33.0.0
- _(bom)_ Update mysql-connector-j to 8.3.0
- _(pom)_ Update jackson to 2.16.1
- _(pom)_ Update hibernate to 5.6.15.Final
- _(pom)_ Update itext to 7.2.6
- _(pdf)_ Remove snowpack stability for gliding snow
- _(i18n)_ Update translations
- _(Regions)_ Add flags enableObservations and enableModelling
- _(i18n)_ Update translations
- _(MapUtilTest)_ Report base64-encoded images
- _(i18n)_ Update translations
- _(PublicationJob)_ Handle super regions like normal regions
- _(PdfUtil)_ Reuse fonts for header and footer
- _(PdfUtil)_ Reuse images (repeated logos)

## [6.1.9] - 2024-02-17

### 🚀 Features

- _(TextToSpeech)_ Enable for IT

### 🐛 Bug Fixes

- _(TextToSpeech)_ 2024-10-02 is spoken as "October 2nd"
- _(TextToSpeech)_ IT replacement keys
- _(TextToSpeech)_ "del limite del bosco"

### ⚙️ Miscellaneous Tasks

- _(TextToSpeech)_ Voice config for IT
- _(TextToSpeech)_ Add voice config to unit tests

## [6.1.8] - 2024-02-09

### 🐛 Bug Fixes

- _(save-bulletins)_ Use correct savedRegions

### ⚙️ Miscellaneous Tasks

- _(TextToSpeech)_ Remove validityDate from filename
- _(TextToSpeech)_ Disable for sub-regions

## [6.1.7] - 2024-02-02

### 🐛 Bug Fixes

- _(TextToSpeech)_ Add bulletinID to filename

## [6.1.6] - 2024-01-31

### 🚀 Features

- TextToSpeech
- _(TextToSpeech)_ German

### 🐛 Bug Fixes

- TextToSpeech.ENABLED
- RegionTest

### 🚜 Refactor

- _(TextToSpeech)_ No "this."
- _(TextToSpeech)_ Extract dangerPatterns
- _(TextToSpeech)_ Remove unused dp1.long strings
- _(TextToSpeech)_ Enabled languages
- _(caaml)_ AvalancheBulletinCustomData

### ⚙️ Miscellaneous Tasks

- _(bulletin-lock)_ GetLockedBulletins
- _(auto-save)_ Add method to save, update and delete single bulletins
- _(TextToSpeech)_ Audio config
- _(TextToSpeech)_ Aspects
- _(TextToSpeech)_ Reorder valid time period
- _(TextToSpeech)_ Jingle from static.avalanche.report
- _(TextToSpeech)_ Omit empty danger patterns sentence
- _(README)_ Add link to Transifex
- _(TextToSpeech)_ TextToSpeech.createAudioFile
- _(TextToSpeech)_ Tune gender
- _(TextToSpeech)_ Write <voice>
- _(TextToSpeech)_ TextToSpeech.createAudioFiles
- TextToSpeechTest.test20231201mp3
- TextToSpeechTest add 2024-01-28
- Tune valid-time-period
- Tune logging
- _(PublicationController)_ Enable text-to-speech

## [6.1.5] - 2024-01-10

### ⚙️ Miscellaneous Tasks

- _(i18n)_ Update translations

## [6.1.4] - 2024-01-04

### ⚙️ Miscellaneous Tasks

- _(i18n)_ Update translations

## [6.1.3] - 2023-12-22

### 🐛 Bug Fixes

- _(Caaml6)_ Do not merge am/pm problems to retain order
- _(PushNotificationUtil)_ No criteria query roots were specified
- _(SubscriberController)_ Removing a detached instance

### ⚙️ Miscellaneous Tasks

- _(container)_ Remove container image creation
- _(Caaml6)_ Set unscheduled flag
- _(ServerInstanceService)_ Add public /info service
- _(Caaml6)_ Set tendency valid time
- _(AuthenticationService)_ Add /test to test access token
- _(CaamlTest)_ Add 2023-12-21
- _(BlogController)_ Tune logging
- _(AuthenticationService)_ Add /test to test access token
- _(SubscriberController)_ Tune logging

## [6.1.2] - 2023-12-01

### 🐛 Bug Fixes

- _(Wordpress)_ Date parsing
- BlogControllerTest

### ⚙️ Miscellaneous Tasks

- _(change-bulletin)_ Start changeThread instantly if status is submitted or resubmitted

## [6.1.1] - 2023-11-30

### 🐛 Bug Fixes

- _(BlogController.getConfiguration)_ Might not return a result
- _(RapidMailController.getConfiguration)_ Might not return a result
- _(TelegramController.getConfiguration)_ Might not return a result
- _(Blogger)_ Deserialization of "published":"2023-11-22T08:44:00-08:00"
- Confusing warning in SubscriberController.createSubscriber
- _(MultichannelMessage)_ RapidMailController.sendEmail
- _(AvalancheBulletinService)_ Change bulletin should not yield status resubmitted

### 🚜 Refactor

- _(BlogController)_ Clearly separate config loading, post loading, sending, exception handling
- _(RapidMailController)_ Clearly separate config loading, post loading, sending, exception handling
- _(TelegramController)_ Clearly separate config loading, post loading, sending, exception handling
- _(PushNotificationUtil.getConfiguration)_ Return optional
- _(EmailUtil)_ Freemarker.template.Configuration.setClassForTemplateLoading
- Introduce new interface MultichannelMessage
- MultichannelMessage.sendToAllChannels
- Class AvalancheReportMultichannelMessage
- Class BlogItemMultichannelMessage

### ⚙️ Miscellaneous Tasks

- Add /server/health
- _(websocket)_ Log @OnError errors as debug
- _(BlogConfiguration.getConfiguration)_ Check blogApiUrl!=null
- _(BlogController)_ Tune logging
- _(pom)_ Update slf4j to 2.0.9
- _(MultichannelMessage)_ Memoize html message
- _(MultichannelMessage)_ Tune logging
- _(MultichannelMessage)_ Tune logging

## [6.1.0] - 2023-11-08

### 🚀 Features

- Caaml6.createXML using jackson-dataformat-xml

### 🚜 Refactor

- XmlUtil.convertDocToString
- Rename CaamlVersion.V6_JSON
- Change suffix \_CAAMLv6.json
- Obsolete throws TransformerException
- Add CAAMLv6_BulletinEAWS.xsd
- Delete Caaml6.java
- Rename Caaml6.toCAAML
- Change suffix \_CAAMLv6.json

### ⚙️ Miscellaneous Tasks

- _(Wordpress)_ Test featured_image_url null
- Update CaamlBulletin2022

### Wordpress

- Run StringEscapeUtils.unescapeHtml4 on blog title

## [6.0.1] - 2023-09-05

### 🚀 Features

- _(RapidMailConfiguration)_ Store mailinglistName in database

### 🐛 Bug Fixes

- _(blog)_ Use new table name in db query
- _(AvalancheReportController)_ Org.hibernate.PersistentObjectException: detached entity passed to persist
- EmailUtilTest.sendMediaEmails

### 🚜 Refactor

- _(RapidMailConfiguration)_ Use CriteriaQuery

### ⚙️ Miscellaneous Tasks

- _(test-publication)_ Remove test methods for telegram, email and push
- _(test-publication)_ Remove test methods for blog
- _(test-publication)_ Fix logging

## [6.0.0] - 2023-08-22

### 🚀 Features

- _(ci)_ Add container image creation
- _(db)_ Prepare db scripts fof liquibase
- _(pom)_ Use Java 11
- Reuse existing AvalancheReport if status does not change
- Implement Wordpress API

### 🐛 Bug Fixes

- _(readme)_ Bulletin status graph
- _(db)_ Adjust markdown syntax
- Resolve Liqubase deprecation warning

### 🚜 Refactor

- _(ci)_ Use ref slug instead on ref name
- Replace with Stream API equivalent
- _(BlogController)_ Happy path
- Rename GoogleBloggerConfiguration to BlogConfiguration
- Introduce interface BlogItem
- Extract Blogger API

### ⚙️ Miscellaneous Tasks

- _(readme)_ Update status graph
- _(db)_ Allow db migration on server start
- _(db)_ Creaet auto configuration
- _(doc)_ Adapt db migration readme
- _(db)_ Delete old database scripts
- _(db)_ Delete creation script creation
- _(pom)_ Update log4j2
- _(pom)_ Update mysql-connector-java
- Update jackson to 2.15.2
- Update io.swagger.core.v3
- Update jersey

### README

- Document bulletin status/workflow

## [5.1.15] - 2023-05-03

### 🐛 Bug Fixes

- _(statistic)_ Move deliminator

## [5.1.14] - 2023-05-03

### ⚙️ Miscellaneous Tasks

- _(statistics)_ Add matrix values

## [5.1.13] - 2023-05-02

### 🐛 Bug Fixes

- _(container)_ Map dir name adjusted to configuration
- _(container)_ Adjust file permissions

### ⚙️ Miscellaneous Tasks

- Region.toString
- _(container)_ Remove obsolete env variables
- _(container)_ Tomcat should only log to the console
- _(container)_ Avoid sentry initialize hint

### RapidMailController

- Disable FAIL_ON_UNKNOWN_PROPERTIES

### RapidMailRecipientListResponse

- @JsonIgnoreProperties(ignoreUnknown=true)

## [5.1.11] - 2023-04-18

### 🚀 Features

- Allow log configuration via env variables
- _(log4j)_ [**breaking**] Add adhoc debug logger configuration

### ⚙️ Miscellaneous Tasks

- _(statistics)_ Add new matrix information and danger rating modificator
- _(publish)_ Start threads for publication and change!
- _(publication)_ Start publication thread
- _(publication)_ Start change thread

## [5.1.10] - 2023-04-11

### 🐛 Bug Fixes

- _(AuthenticationController)_ ServerKeys constructor argument order
- Create required directory if it not exists

### 🚜 Refactor

- Eu.albina.util.DBEnvConfig.asMap
- _(tx)_ Tx migrate

### ⚙️ Miscellaneous Tasks

- _(AuthenticationController)_ Use ECDSA256 == ES256
- _(deployment)_ Create docker file
- _(BlogController)_ Store lastPublishedTimestamp

## [5.1.9] - 2023-02-02

### 🐛 Bug Fixes

- AvalancheBulletinService
- _(RssUtil)_ Public URL
- _(media-email)_ Send important media file to additional recipients

### 🚜 Refactor

- Migrate unit tests to JUnit 5
- _(PublicationJob)_ Use getClass().getSimpleName()
- AvalancheBulletinController.getAllBulletins
- _(AvalancheBulletinController)_ Do not throw AlbinaException
- Inline PublicationController.publish
- Inline AvalancheBulletinController.publishBulletins
- _(PublicationJob)_ Simplify
- _(PublicationJob)_ Simplify
- _(MediaFileService)_ Use Files.copy
- Use {} placeholders for logging
- Use String.format

### ⚙️ Miscellaneous Tasks

- Eu.albina.util.HibernateUtilTest.createSchema
- _(RssUtil)_ Add owner/name/email elements
- _(i18n)_ Update translations

## [5.1.8] - 2022-12-23

### 🚀 Features

- _(bulletins)_ Separate /caaml, /caaml/json, /json
- _(openapi)_ Rapidoc frontend

### 🐛 Bug Fixes

- _(publication)_ Check for status republished
- _(publication)_ Use getPublishedBulletinRegions()
- _(AvalancheReport)_ Set publicationTimeString from globalBulletins
- _(MapUtil)_ Daytime dependency not based on global bulletins
- _(pom)_ Update jackson
- _(change)_ Same code to publish/update/change
- _(json)_ Add regions to EUREGIO JSON
- _(change)_ Save/submit/publish bulletins
- _(update)_ Reproduce all regions
- _(publication)_ Keep already published bulletins
- _(publication)_ Load published bulletins
- _(publication)_ Bulletins for super region
- _(save)_ Set publicationTime to null
- _(change)_ Use publicationDate
- _(publication)_ Use same publication time
- _(publication)_ Use correct avalancheReport for notifications
- _(update)_ Set enabled true for manual update
- _(publication)_ PublicationTime string
- _(change)_ Use same publish method
- _(publication)_ Add sleep for change method
- _(publishBulletins)_ Set publicationDate for all bulletins
- _(publishedBulletins)_ Correct merging of bulletins
- _(publication)_ Set same publicationDate for all bulletins
- _(report)_ Always set a status
- _(PublicationController)_ Bulletins in log

### 🚜 Refactor

- _(jobs)_ Harmonize code structure
- _(jobs)_ UpdateJob extends PublicationJob
- _(jobs)_ /bulletins/publish/all use UpdateJob
- _(jobs)_ Inline AlbinaUtil.isReportSubmitted
- _(MapUtil)_ Inline DaytimeDependency.of
- _(LinkUtil)_ Use AvalancheReport
- _(AvalancheBulletinController)_ SaveBulletins
- _(saveBulletins)_ Remove publicationTime
- _(PublicationController)_ Remove obsolete code

### ⚙️ Miscellaneous Tasks

- _(regions)_ Make getActiveRegions() private
- _(AvalancheReport)_ Check that bulletins is subset of globalBulletins
- _(AvalancheReport)_ Get rid of getRegionBulletins
- _(CaamlTest)_ Add 2022-12-20
- _(CaamlTest)_ AssertStringEquals
- _(publication)_ Add logging

## [5.1.7] - 2022-12-13

### 🐛 Bug Fixes

- _(TelegramController)_ SendPhoto using multipart/form-data

### ⚙️ Miscellaneous Tasks

- Add TelegramController.getMe

## [5.1.6] - 2022-12-13

### 🐛 Bug Fixes

- _(scripts)_ Manual publication scripts
- _(update)_ Publish only submitted/resubmitted reports
- _(publish)_ Manually publish all regions

## [5.1.5] - 2022-12-12

### 🐛 Bug Fixes

- _(PublicationJob)_ Truncate publication date to seconds
- _(pdf)_ Manual pdf production
- _(pdf)_ Typo
- _(publication)_ Manual resource production

## [5.1.4] - 2022-12-12

### 🐛 Bug Fixes

- _(publication)_ Set server instance
- _(publication)_ Harmonize publish, update and change
- _(AvalancheBulletinService)_ Default to all regions
- _(AvalancheBulletinService)_ Default to language=en

## [5.1.3] - 2022-12-09

### 🐛 Bug Fixes

- _(scripts)_ Fix paths

### ⚙️ Miscellaneous Tasks

- _(publication)_ Create files without region id

## [5.1.2] - 2022-12-06

### 🚀 Features

- _(MediaFileService)_ Publish media files as Podcast RSS
- _(PdfUtil)_ Add EAWS matrix information

### 🐛 Bug Fixes

- _(Caaml6_2022)_ RegionID
- _(Caaml6_2022)_ MatrixInformation
- _(PublicationController)_ AvalancheReport.setBulletins regionBulletins
- _(SimpleHtmlUtil)_ AvalancheReport.getRegionBulletins
- _(Caaml6_2022)_ TruncatedTo(ChronoUnit.SECONDS)
- _(Caaml6_2022)_ All_day avalancheProblems
- _(PublicationJob)_ NPE in UserController.get
- _(AlbinaUtil)_ Comparator.nullsFirst
- _(AvalancheReport)_ NPE
- _(PdfUtil)_ GetPreviewPdf
- _(MapUtilTest)_ TestMapyrusMapsTyrol
- _(PublicationController)_ Use global bulletins for maps!
- _(MapUtilTest)_ Map extends of regions
- _(publication)_ Create maps and pdfs for all region on update and publish, send notifications afterwards
- _(zoneId)_ Add missing import
- _(publication)_ Set status published only for updated regions
- _(update)_ Ignore regions that were not published at all
- _(update)_ Ignore regions that were not published at all
- _(publication)_ Fix update
- _(change)_ Reproduce resources for all regions
- _(publication)_ Combine scripts for json and xml, create correct xmls on manual trigger
- _(RssUtil)_ Valid RSS feed
- _(PublicationJob)_ NPE in AvalancheBulletinController.publishBulletins

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
- _(CaamlTest)_ Extract toCAAMLv6_2022
- _(MediaFileService.saveMediaFile)_ Use getMediaPath and Files
- _(PdfUtil)_ Use lang, grayscale fields
- _(PdfUtil)_ Extract methods from createAvalancheProblem
- _(PdfUtil)_ WebColors.getRGBColor

### ⚙️ Miscellaneous Tasks

- Rebuild 2022 maps
- _(PdfUtil)_ Cache resource images for performance
- _(PdfUtil)_ Deflater.BEST_SPEED for performance
- _(rebuildMaps)_ Use ExecutorService
- _(rebuildMaps)_ AvalancheReportController is too complicated
- _(rebuildMaps)_ CompletableFuture is too complicated
- _(rebuildMaps)_ Logging
- ImageTestUtils.assertEquals w/ message
- _(rebuildMaps)_ RebuildPdfUtil
- _(rebuildMaps)_ 2022-05-02
- CaamlTest.createOldCaamlFiles2022
- CaamlTest.createOldCaamlFiles2022
- CaamlTest.createOldCaamlFiles2022
- CaamlTest.createOldCaamlFiles2022
- _(Caaml6_2022)_ DangerPattern implements CustomDatum
- _(Caaml6_2022)_ MainDate implements CustomDatum
- _(Caaml6_2022)_ Update JSON schema file
- _(PublicationController)_ Logging
- UpdateLatestFiles.sh
- _(Caaml6_2022)_ "namespace" customData
- _(RssUtil)_ Limit 10
- _(MediaFileService)_ GetMediaPath+region+language

## [5.1.1] - 2022-11-21

### 🐛 Bug Fixes

- Image paths in region_AT-07.json
- _(PdfUtil)_ RegionBulletins

### 🚜 Refactor

- _(AvalancheReport.toCAAML)_ Inline
- _(AuthorizationFilter)_ Prefer happy-path

### ⚙️ Miscellaneous Tasks

- _(AuthorizationFilter)_ Clearer exception message
- _(AuthorizationFilter)_ Clearer exception message

## [5.1.0] - 2022-11-11

### 🚀 Features

- Use eaws-regions/micro-regions_names
- Build albina-server as Docker image
- Build albina-server as Docker image
- Build CAAML v6 JSON
- Upgrade to Swagger 2
- _(DateControllerUtil)_ Parse LocalDate

### 🐛 Bug Fixes

- _(Region)_ NeighborRegions is undefined
- Lauegi_map.png logo
- RegionTest
- _(Region)_ Getter/setter name
- SimpleHtmlUtilTest
- PublicationController.sendEmails
- BulletinStatusTest.testCompare
- PushNotificationUtilTest
- _(websocket)_ Never timeout due to inactivity
- _(XmlUtil)_ Use correct map filenames
- _(AvalancheBulletin.createCAAMLv6Bulletin)_ Use correct map filenames
- _(EmailUtil)_ Use correct map filenames
- _(SimpleHtmlUtil)_ Use correct map filenames
- Missing AvalancheReport.setBulletins
- _(docker)_ Custom network
- _(docker)_ AllowPublicKeyRetrieval
- _(docker)_ Database name
- _(docker)_ Git version
- _(docker)_ No git version
- _(docker)_ Albina-admin-gui environment-relative
- Max key length is 767 bytes
- _(docker)_ Avalanche-warning-maps
- _(docker)_ Install ghostscript imagemagick webp
- _(MapUtil)_ MapProductionResource for docker
- Could not initialize proxy [eu.albina.model.Region#AT-07] - no Session
- MapUtilTest.testMapyrusMaps
- "regionId"
- Trim strings in AvalancheBulletin.getTextPartIn
- Duplicate dangerRatings in CAAML v6
- AvalancheReportTest
- @SuppressWarnings for Hibernate queries
- _(Caaml6_2022)_ NPE

### 🚜 Refactor

- Remove jts dependency
- Move constants to StatisticsController
- Move constants to AuthenticationController
- _(GlobalVariables)_ Remove unused code
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
- _(AlbinaUtil.isUpdate)_ LocalTime.of(17, 0)
- _(AlbinaUtil.hasDaytimeDependency)_ Inline

### ⚙️ Miscellaneous Tasks

- _(RegionTest)_ Test com.fasterxml.jackson
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
- _(docker)_ Add albina-admin-gui
- _(docker)_ Add avalanche-warning-maps
- _(docker)_ Add textcat-ng
- _(docker)_ Albina-admin-gui w/ envsubst
- _(docker)_ Use volumes for docker-entrypoint-initdb.d
- _(docker)_ ALBINA_DB_CONNECTION_URL
- _(docker)_ Custom log4j2.xml
- _(docker)_ Use albina.war
- _(docker)_ ALBINA_JWT_SECRET
- _(media-file)_ Add lang to path
- ImageTestUtils.assertImageEquals w/ message
- Validate CAAML v6 JSON
- Add DangerPattern name to CAAML v6
- Add avalanche problems to CAAML v6
- Danger pattern as type/id/name in CAAML v6
- _(PublicationController)_ Generate CAAML v6 JSON
- _(persistence)_ MySQL55Dialect
- _(Caaml)_ Javadoc
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
- _(AvalancheBulletinService.getPublishedXMLBulletins)_ JSON for CaamlVersion.V6_2022

### Albina_create

- Bit default false

## [5.0.1] - 2022-10-11

### 🐛 Bug Fixes

- MapUtilTest.testMapyrusMaps
- _(region)_ Update region json for test
- _(test)_ Update new regions
- Map production for preview
- _(pom)_ Update log4j2
- _(pom)_ Update log4j2
- _(MapUtilTest)_ TestMayrusBindings
- Subscribe

### 🚜 Refactor

- Use method reference

### ⚙️ Miscellaneous Tasks

- _(avalanche-problems)_ Make cornices and no distinct problem optional
- Disable MapUtilTest.testMapyrusMaps
- _(MapUtil)_ Remap regions prior to 2022-10-01
- _(pom)_ Update slf4j

## [5.0.0] - 2022-06-24

### 🐛 Bug Fixes

- _(blog-controller)_ Change method signature
- _(email)_ Fix expected result
- _(region-test)_ Move element to correct position
- _(static-content)_ Update url
- _(MapUtilTest)_ Make tests actually run, no assume
- _(regions)_ Remove server instance data from test json
- _(pdf)_ Path for images
- _(region)_ MapLogoColorPath and mapLogoBwPath
- _(region)_ Fix naming of shape files for aran
- _(tendency)_ Path!
- _(pdf)_ Footer logo
- _(regions)_ Remove empty part from test regions
- _(map-production)_ Wrong file extension
- _(region)_ To json
- _(map-production)_ Remove windows commands
- _(map-production)_ Variable name
- _(region)_ Parameter typo
- _(server-instance)_ Json export typo
- _(user)_ Update user
- _(media-file)_ Use region id for file path
- _(region)_ Map center to double
- _(hibernate)_ Pass region to query
- _(email-test)_ Fix method call
- _(test)_ Region
- _(media-file)_ Fix mp3 file url in email
- _(media-file)_ Fix mp3 file url in email
- _(listener)_ Shutdown
- _(scripts)_ Update scripts, fix /bin/sh call
- _(publication)_ Publish report only once
- _(publication)_ Create CAAMLv6 on update
- _(scripts)_ Update scripts
- _(sql)_ Use plural for table name
- _(publication)_ Do not run telegram, emails, push multiple times for one region
- _(test)_ Simple html
- _(publication)_ Use correct directory for maps, show neighbor regions in region maps, limit number of thumbnail maps
- Map tests

### ⚙️ Miscellaneous Tasks

- _(test)_ Remove hibernate from util classes, update tests
- _(resources)_ Add resource files
- _(resources)_ Update resource JSON files for regions
- _(HibernateUtil)_ No Hibernate for continuous integration
- _(test)_ Ignore blog tests due to hibernate
- _(test)_ Ignore push tests due to hibernate
- _(test)_ Ignore user tests due to hibernate
- _(static-content)_ Add url for static content
- _(email-tests)_ Ignore tests
- _(MapUtilTest)_ Clone branch region-mgmt for avalanche-warning-maps
- _(config)_ Add methods to edit server and region configs via api!
- _(map-production)_ Rename ressource files
- _(mapyrus)_ Get rid of german texts
- _(test)_ Ignore isLatest test, fails outside season
- _(debug)_ Change info to debug
- _(region-mgmt)_ Add api method to retrieve all regions, fix permissions for roles
- _(region-mgmt)_ Add full region configuration to user api call
- _(media-file)_ Add flag to region configuration
- _(region)_ Add map center lat and lng to region configuration
- _(media-email)_ Finalize media email
- _(neighbor-regions)_ Add neighbors to region object
- _(hibernate)_ Lower case table names
- _(observations)_ Change type to lob for content
- _(hibernate)_ Harmonize table names (lower case, plural)
- _(hibernate)_ Fix typo
- _(MapUtilTest)_ Clone branch master for avalanche-warning-maps
- _(avalanche-problems)_ Rename avalanche situations to avalanche problems
- _(media-file)_ Send media file to additional addresses
- _(media-file)_ Change subject for important emails
- _(matrix)_ Add new eaws matrix and corresponding enums
- _(matrix-information)_ Delete matrix information for avalanche bulletin (outside an avalanche problem)
- _(eaws-matrix)_ Add new matrix fields to avalanche problem model, add sql script
- _(user)_ Add method to retrieve users for admin gui
- _(json)_ Add avalanche problems, remove matrix information§
- _(windows)_ Check os for process builder calls
- _(publication)_ Do not publish report for super regions
- _(thumbnail-maps)_ Create for each region and add region id to filename
- _(matrix)_ Add information of new matrix to CAAMLv6
- _(matrix)_ Optional visualization of matrix
- _(matrix)_ Update test resources
- _(matrix)_ Update test resources
- _(wind_slab)_ Rename avalanche problem wind_drifted_snow to wind_slab
- _(wind_slab)_ Update resource files
- _(avalanche-problems)_ Add cornices and no distinct problem images
- _(avalanche-problems)_ Add cornices and no distinct problem
- _(danger-rating-modificator)_ Add field to store modificator for danger rating (-, =, +)
- _(matrix)_ Add values for snowpack stability, frequency and avalanche size
- _(matrix)_ Update sql script

## [4.1.8] - 2022-04-11

### 🚀 Features

- _(TelegramChannelProcessorController)_ Retry
- _(EmailUtil)_ Add Val d'Aran

### 🐛 Bug Fixes

- _(RegionConfigurationController)_ Use TypedQuery.setParameter
- _(LinkUtil)_ Trailing slash in website URL
- _(config)_ Albina.conf.publish-bulletins-aran=false

### 🚜 Refactor

- _(TelegramChannelUtil)_ Optional.orElseThrow
- _(TelegramChannelUtil)_ Throws
- Remove import
- _(HibernateUtil)_ PersistenceException
- _(EmailUtil)_ Simplify

### ⚙️ Miscellaneous Tasks

- _(external)_ Edit api path for external server instances
- _(EmailUtil)_ Aran color
- _(EmailUtil)_ Lauegi@aran.org
- _(EmailUtil)_ LanguageCode.getSocialMedia for Aran
- _(EmailUtilTest)_ SendEmailAran
- _(EmailUtil)_ Subject for Aran
- _(AuthenticationService)_ Access_token expires at 03:00
- _(test)_ Update tests

## [4.1.7] - 2022-03-07

### 🐛 Bug Fixes

- _(media-file)_ Set media file flag in avalanche report!
- _(media-file)_ Set defaut value false for new column
- _(mapyrus)_ Placement of logo
- _(albina-util)_ Remove space

### 🚜 Refactor

- _(publication-controller)_ Remove unused function
- Remove Hibernate dependencies from model

### ⚙️ Miscellaneous Tasks

- _(media-file)_ Add methods to upload media files
- _(media-file)_ Add media path to server instance
- _(media-file)_ Send media emails
- _(region-mgmt)_ Fix multiple bugs
- _(static-widget)_ Remove complete functionality
- _(static-widget)_ Add sql script to delete db columns
- _(xml)_ Use server instance name as operation in caaml
- _(xml)_ Remove todo
- _(mapyrus)_ Remove unused font definitions
- _(mapyrus)_ Allow positioning of logo
- Update test resources and more
- _(server-instance)_ Remove field regions
- _(publication)_ Set flags in database
- _(sql)_ Add script for media file columns in db

### TelegramChannelUtil

- Catch all exceptions

## [4.1.6] - 2022-02-15

### 🐛 Bug Fixes

- _(PushNotificationUtil)_ Content-Encoding

### 🚜 Refactor

- Remove unused imports

### ⚙️ Miscellaneous Tasks

- _(PushNotificationUtil)_ Error handling

## [4.1.5] - 2022-02-14

### 🐛 Bug Fixes

- _(blog-controller)_ Fix typo
- _(tests)_ Initialize hibernate
- _(subscriber)_ Typo
- _(map-type)_ Update comments
- _(statistic)_ Use all publish regions for statistic

### 🚜 Refactor

- _(pdf)_ Remove unused imports
- _(static-widget)_ Remove comments
- _(BlogController)_ JAX-RS Client
- _(RapidMailProcessorController)_ JAX-RS Client
- _(PushNotificationUtil)_ JAX-RS Client
- _(TelegramChannelProcessorController)_ JAX-RS Client
- _(ObservationLwdKipService)_ JAX-RS Client
- Remove obsolete CommonProcessor
- Use com.google.common.hash.Hashing
- Use java.util.Base64
- Remove org.apache.httpcomponents dependency
- Consolidate org.apache dependencies
- _(region)_ Rename copyright
- _(region)_ Remove comment, change copyright
- _(daytime-dependency)_ Move enum
- _(mapyrus)_ Region ID
- _(mapyrus)_ Euregio_image_file from geodata_dir
- Fix logging

### ⚙️ Miscellaneous Tasks

- _(bulletin-regions)_ Do not create regions.json
- _(region)_ Reduce region model to ID
- _(social-media)_ Refactor social media classes
- _(region-test)_ Remove obsolete tests
- _(region)_ Move configurations to region / db
- _(region)_ Add fields external and externalApiUrl
- _(user)_ Use region object
- _(region)_ Extend region service and controller
- _(regions)_ Extend class region, add class serverInstance, replace set of region ids in global variables
- _(region)_ Replace region code variables in tests
- _(avalanche-bulletin-service)_ Secure aineva endpoint
- _(region)_ Add comments
- _(region)_ Add UserRegionRole class
- _(region)_ Add endpoint to get external server instances
- _(region)_ Add todo
- _(region)_ Remove regions.json from external files in caaml
- _(region)_ Remove region specific maps and pdfs from external files in caaml
- _(region)_ Use region objects nearly everywhere
- _(region)_ Move simple html template name to region class
- _(region)_ Add subregions and superregions to region json
- _(region)_ Use color of region in pdf
- _(region)_ Use region object for subscriber
- _(region)_ Remove color codes for aran and albina (moved to region object)
- Update test
- _(region)_ Limit social media publish always to one region
- _(region)_ Move configuration parameter to server instance
- _(region)_ Use push configuration
- _(region)_ Update pom variables
- _(region)_ Remove serverMapsUrl, serverPdfUrl, serverSimpleHtmlUrl from global variables (use variables from db)
- _(region)_ Use region object in map production
- _(pdf)_ Region dependent resource strings
- _(region)_ Move parameters to region class
- _(region)_ Remove publication provider class
- _(region)_ Rename parameter for map production (logo_file)
- _(region)_ Statistic for regions
- _(region)_ Define secondary logo of static widget in region class
- _(region)_ Add MapProductionConfiguration class
- _(region)_ Extract all parameters from map production
- _(region)_ Rename field external -> externalServer
- _(i18n)_ Update translations

## [4.1.4] - 2022-01-16

### 🐛 Bug Fixes

- _(pdf)_ Fill up table rows, fixes #233
- _(email)_ Send emails only if bulletins affect region (fixes #232)
- _(imprint)_ Update url for imprint

### ⚙️ Miscellaneous Tasks

- _(blog)_ Add methods to publish latest blogs manually
- _(publication)_ Do not use threads for emails and telegram (for testing)
- _(email)_ Add logging
- _(email)_ Add even more logging
- _(email)_ Add return statement, add try catch block

## [4.1.3] - 2022-01-13

### 🐛 Bug Fixes

- _(telegram)_ Use correct chat id

## [4.1.2] - 2022-01-13

### 🐛 Bug Fixes

- _(telegram)_ Use correct chat id

## [4.1.1] - 2022-01-13

### 🐛 Bug Fixes

- _(logging)_ Non-constant string concatenation
- _(pom)_ Scope=test for test libraries
- _(log4j)_ Sentry

### 🚜 Refactor

- _(LanguageCode)_ Public static final
- _(log4j)_ Xmlns
- Extract DateControllerUtil
- _(EmailUtil)_ Extract variables
- _(EmailUtil)_ Status

### ⚙️ Miscellaneous Tasks

- _(publish-all)_ Allow manual publication of all regions without 5PM flag
- _(email-test)_ Send test emails for all regions
- _(push)_ Add API to trigger push notifications
- _(email)_ Logging
- _(email)_ Add sendEmailIssue232 test
- _(email)_ SendEmailIssue232 in test mode
- _(publication)_ Allow manual publication for specific languages
- _(test)_ Add test methods for telegram and push

## [4.1.0] - 2022-01-05

### 🚀 Features

- _(MapUtil)_ Val d'Aran
- _(PdfUtil)_ Val d'Aran
- _(PdfUtil)_ Val d'Aran color/logo
- _(AvalancheBulletinService)_ For all publish regions
- _(controllers/jobs)_ For all publish regions
- _(SimpleHtmlUtil)_ Add Val d'Aran
- _(LinkUtul)_ Override various base URLs via properties

### 🐛 Bug Fixes

- _(ImageTestUtils)_ Error message
- "UTC"
- Tests for LinkUtil changes
- _(SimpleHtmlUtil)_ Images//warning_pictos
- _(AlbinaServiceContextListener)_ Do not fail without sentry
- _(pom)_ Update log4j2
- _(bulletins)_ Return Europa/Vienna timezone for /status
- _(XmlUtil)_ Remove duplicate locRef elements in CAAMLv5
- _(test)_ Caaml v5 test
- XmlUtilTest
- _(PublicationController)_ CodeEuregio for PDF and simple HTML
- _(preview)_ CodeEuregio→region
- _(pdf-preview)_ Create all maps!
- _(pdf-preview)_ Add region string to pdf filename
- _(maps)_ Use euregio logo only on map for whole euregio
- String comparison
- _(pdf-preview)_ Load internal reports
- _(pdf-preview)_ Use bulletins with every status, return no content if no bulletin was found
- _(pdf-preview)_ Do not use bulletins without regions

### 🚜 Refactor

- _(MapUtil)_ Convert utility class to interface
- _(MapUtil)_ Bindings
- _(MapUtil)_ Extract MapyrusInterpreter
- _(MapUtil)_ Extract MapSize.of
- _(MapUtil)_ Simplify outputFile
- _(MapUtil)_ Extract DaytimeDependency.of
- _(MapUtilTest)_ Use temporary mapsPath
- _(MapUtil)_ Simplify createMayrusInput
- _(MapUtil)_ Simplify getDangerRatingString
- _(MapUtilTest)_ Compare fd_albina_thumbnail.png
- _(mapyrus)_ Remove debugging output
- _(MapUtil)_ Replace drmFile with mapyrus bindings
- _(MapUtil)_ Package eu.albina.map
- _(MapUtil)_ Extract enums and utility classes to files
- _(MapUtil)_ Extract enum MapImageFormat
- _(MapUtil)_ Move unrelated functions
- _(MapUtil)_ Remove unused working_dir binding
- _(MapUtil)_ Remove unused language binding
- _(MapUtil)_ Remove unused problem_icon_l/problem_icon_h
- _(mapyrus)_ Remove dead code
- _(MapUtil)_ Remove unused interreg logo
- _(MapUtil)_ Extract getOutputDirectory
- _(MapUtil)_ Extract AvalancheBulletin.regions
- _(MapUtil)_ Use UUID for bulletin_ids
- _(MapUtil)_ Rename map_level, use as string
- _(MapUtil)_ Remove map_xsize
- _(MapUtil)_ Separate MapType/MapLevel
- _(MapUtil)_ Inline publicationTime
- _(MapUtil)_ Extract BulletinRegions
- _(MapUtil)_ Inline outputDirectory/preview
- AvalancheBulletin.affectsRegion
- _(PdfUtil)_ Extract getFilename
- _(PdfUtil)_ Try-with-resources
- _(PdfUtil)_ Exception handling
- _(MapType)_ Rename realm()
- _(MapUtil)_ Euregio_image_file
- _(MapUtil)_ Lauegi_map.png logo
- _(PdfUtl)_ Extract color blue
- _(PdfUtl)_ Reuse colors
- Use MapUtil.getOverviewMapFilename
- _(LinkUtil)_ Move getMapsUrl and getSimpleHtmlUrl
- _(LinkUtil)_ Merge getSimpleHtmlUrl/getAvalancheReportSimpleBaseUrl
- _(LinkUtil)_ GetPdfLink using GlobalVariables
- _(LinkUtil)_ GetSocialMediaAttachmentUrl using getMapsUrl
- _(LinkUtil)_ GetWebsiteStaticFiles
- _(GlobalVariables)_ GetPublishRegions
- _(GlobalVariables)_ GetPublishBlogRegions
- _(AlbinaServiceContextListener)_ Remove Sentry.init
- Use Objects.equals
- Use Strings.isNullOrEmpty
- Package eu.albina.rest.websocket
- Inline variable

### ⚙️ Miscellaneous Tasks

- _(email)_ Ignore EmailUtilTest.sendEmail
- Update .gitignore
- _(MapUtilTest)_ Add lauegi.report-2021-12-10
- _(SimpleHtmlUtil)_ Add Val d'Aran templates from 2020/2021
- Sort config.properties
- _(bulletins)_ Make timezone configurable for /status
- _(i18n)_ Update translations
- _(hibernate)_ Use HibernatUtil.getInstance().runTransaction
- _(email)_ Use open sans font
- _(pdf-preview)_ Generate pdf for one region only

## [4.0.10] - 2021-12-15

### 🚀 Features

- _(PushNotification)_ Use ch.rasc.webpush

### 🐛 Bug Fixes

- _(pom)_ Update log4j2

### 🚜 Refactor

- _(ch.rasc.webpush)_ Simplify/adapt for albina

## [4.0.9] - 2021-12-11

### 🐛 Bug Fixes

- _(pom)_ Update log4j2 (CVE-2021-44228)

## [4.0.8] - 2021-12-06

### 🐛 Bug Fixes

- _(AlbinaUtil)_ Format publication time in UTC

### 🚜 Refactor

- _(email)_ Add logging
- _(email)_ Unused return code
- _(email)_ Simplify getRecipientsList
- _(email)_ Simplify resolveRecipientListIdByName
- _(email)_ Extract getRecipientId
- _(email)_ Use LanguageCode
- _(test)_ Rename AlbinaUtilTest

### ⚙️ Miscellaneous Tasks

- _(test-email)_ Do not use a thread
- _(email)_ Add logging
- _(email)_ Add test
- _(email)_ Enable test
- _(email)_ Link to API docs
- _(email)_ Omit send_at
- _(email)_ Ignore EmailUtilTest.sendLangEmail
- _(email)_ Logging
- _(email)_ Add java version to test mail

## [4.0.7] - 2021-12-01

### 🐛 Bug Fixes

- _(static-widgets)_ Rename latest static widget files
- _(static-widget)_ Use correct logo

### 🚜 Refactor

- Remove import

### ⚙️ Miscellaneous Tasks

- _(email)_ Test 2021-12-01 bulletin
- _(email)_ Add api method to send test email

## [4.0.6] - 2021-12-01

### 🐛 Bug Fixes

- _(static-widget)_ Date in filename
- _(publication)_ Use correct startDate and endDate
- _(publication)_ Use ZonedDateTime instead of OffsetDateTime

### 🚜 Refactor

- _(publication)_ Add debug statements

### ⚙️ Miscellaneous Tasks

- _(sentry)_ MinimumEventLevel=WARN
- _(sentry)_ Configure using system environment variable
- _(sentry)_ Update io.sentry:sentry
- _(pom)_ Downgrade jetty-maven-plugin
- _(email)_ Add test

### PushNotificationService

- GET /push/key

## [4.0.5] - 2021-11-30

### 🐛 Bug Fixes

- _(log4j2)_ Filepath/filename
- _(log4j2)_ Albina.log.prefix whitespace
- _(scripts)_ Typos in file names, add additional language files for simple htmls

## [4.0.4] - 2021-11-30

### ⚙️ Miscellaneous Tasks

- _(RapidMailProcessorController)_ Logging

## [4.0.3] - 2021-11-30

### 🐛 Bug Fixes

- _(AlbinaUtil)_ Local timezone

## [4.0.2] - 2021-11-30

### 🐛 Bug Fixes

- _(Ãpersistent-weak-layers)_ Rename icon files

## [4.0.1] - 2021-11-30

### 🐛 Bug Fixes

- _(logging)_ SEVERE -> ERROR
- _(tests)_ Do not HibernateUtil.setUp
- _(GlobalVariables)_ DirectoryOffset

### 🚜 Refactor

- LanguageCode.ENABLED
- AvalancheBulletin.getHighestDangerRating
- _(messenger-people)_ Delete GlobalVariables.targeting
- Tendency.getSymbolPath
- AvalancheSituation.getSymbolPath
- Aspect.getSymbolPath
- LinkUtil
- _(LanguageCode)_ Use EnumSet

### ⚙️ Miscellaneous Tasks

- Delete tmp files at 3 AM, closes #222
- _(EmailUtil)_ Logging
- _(sentry)_ DiagnosticLevel=warning

## [4.0.0] - 2021-08-09

### 🚀 Features

- Delete interreg logo, add euregio logo, place euregio logo in maps
- Change folder structure to support multiple versions per day, include euregio logo, fix time change bug

### 🐛 Bug Fixes

- _(simple-html)_ Fix links
- _(MapUtilTest)_ Do not modify production properties
- _(user-service)_ Fix path of new methods
- _(user-model)_ Constructor
- Use isEmpty instead of isBlank
- _(gitlab-ci)_ Do not allow errors on production build (java 8)
- _(pom)_ Delete scope test from bytebuddy and objenesis dependency
- _(hibernate-query)_ Typos
- _(test)_ Use instant instead of zoneddatetime
- _(publication-date)_ Use correct UTC time for publication datetime
- _(region-controller-test)_ Delete comment
- _(hibernate-spatial)_ Rename packages due to changes in hibernate-spatial 5.4
- _(caamlv6)_ Use report publication time for all thumbnail map urls
- _(pom)_ Do not filter binary files such as .ttf files
- _(statistics)_ Use correct number of columns
- Configure log4j2.xml
- _(maps)_ Use correct commands to convert image formats
- _(tests)_ Change publication date of test bulletin, check for null after pdf creation
- _(pdf)_ Check writer for null before closing

### 🚜 Refactor

- Delete ununsed imports and variables, fix constructor for Integer
- Add ids, remove todos
- _(string)_ Use isBlank method
- _(chat-message)_ Delete obsolete import
- _(albina-util)_ Delete obsolete import
- Delete obsolete import
- Add logging
- _(xml-util)_ Delete obsolete method
- Delete obsolete TODO
- _(persistence)_ Delete property provider_class
- _(avlanche-bulletin)_ Delete obsolete TODO
- _(MapUtil)_ Load mapyrus files as resources
- _(GlobalVariables)_ Remove unused scriptsPath
- Add missing @java.lang.Override annotations
- _(MapUtil)_ Load mapyrus fonts as resources
- _(PdfUtil)_ Use images as resources
- _(localImagesPath)_ Delete obsolete parameter localImagesPath
- _(UtilsTest)_ Use org.junit.Assert.assertTrue
- _(tmp)_ Specify tmp directory

### ⚙️ Miscellaneous Tasks

- _(vscode)_ Add config
- _(i18n)_ Add config for i18n-ally
- _(sentry)_ Only allow our own urls for errors
- _(i18n-ally)_ Delete config
- _(messenger-people)_ Delete all code for messenger people
- _(twitter)_ Delete code for twitter
- Update dependency commons-beanutils
- _(region-service)_ Remove
- _(pom.xml)_ Update apache-commons, quartz scheduler
- _(shipment)_ Delete
- Clean up social media
- _(user)_ Add user service, move methods from authentication service
- _(user-controller)_ Move method from authentication controller
- _(user-service)_ Add methods for regions and roles
- _(user)_ Create, delete user
- Update to mapyrus 2.106
- _(pom)_ Update dependencies, add min maven version, add min java version
- _(pom)_ Change required java version
- _(pom)_ Change source and target version
- _(pom)_ Target java 11
- _(pom)_ Update mockito to v3.9.0
- _(pom)_ Add dependencies byte-buddy and objenesis (for mockito), update mockito version
- _(test)_ Ignore useless tests
- _(pom)_ Update log4j2
- _(pom)_ Update hibernate to 5.5.0.Alpha1, update commons-io to 2.8.0
- _(pom)_ Update guava to v30.1.1-jre
- _(pom)_ Update freemarker to 2.3.31
- _(pom)_ Keep Java 8 support
- _(java-time)_ Remove joda time dependecy
- _(a11y)_ Add translations for aspects
- _(i18n)_ Update translations
- _(a11y)_ Set alt texts for pdf images
- _(pom)_ Update dependencies, add maven-war-plugin
- _(authentication-controller)_ Use ZonedDateTime for method getValidityDate
- _(avalanche-bulletin)_ Use ZonedDateTime for method getValidityDate
- _(bulletin)_ Remove db type for datetime
- _(instant)_ Use instant, create correct format of datetime objects for admin gui
- _(pom)_ Remove log4j-slf4j-impl dependency
- _(publication-time)_ Without nanos
- _(pom)_ Add slf4j-log4j-impl
- _(pom)_ Add hibernate-java8
- _(log4j2)_ Rename config file
- _(tests)_ Enable tests
- _(pom)_ Reset hibernate version, clean logging dependencies
- _(hibernate)_ Set up in constructor
- _(persistence)_ Add provider class c3p0
- _(pom)_ Delete entity manager
- _(test)_ Enable region controller tests
- _(jts)_ Update locationtech packages (needed for hibernate)
- _(pom)_ Delete hibernat-java8 (already included), use latest prod version for hibernate 5.4.32
- _(region-controller-test)_ Ignore tests
- _(statistic-test)_ Update resource file
- _(pom)_ Albina1rz:3306/albina_dev
- _(PdfUtil)_ Use OpenSans from resources/fonts
- _(preview)_ Allow PDF preview for bulletins
- _(i18n)_ Update translations
- _(pom)_ Albina1rz:3306/albina
- _(pom)_ /var/www/avalanche.report
- _(pom)_ Mysql://localhost:3306
- _(statistics)_ Return csv file
- _(statistics)_ Secure endpoint (admin, forecaster, foreman)
- _(statistics)_ Remove matrix information from danger rating above and below (only available for each avalanche problem)
- _(observations)_ Add csv export

### Core

- _(java-time)_ Use zoneddatetime for publication and update jobs

## [3.1.9] - 2021-03-29

### ⚙️ Miscellaneous Tasks

- _(PushNotificationUtil)_ No singleton instance

## [3.1.8] - 2021-03-29

### 🐛 Bug Fixes

- _(mapyrus)_ Fix OpenTypeFont.getFontDefinition performance
- _(mapyrus)_ Fix OpenTypeFont.getFontDefinition performance

### 🚜 Refactor

- MapUtilTest.assumeMapsPath

### ⚙️ Miscellaneous Tasks

- _(GlobalVariables)_ Local maps-path/map-production-url
- Add org.mapyrus.font.OpenTypeFont for patching
- MapUtilTest.testMapyrusMapsDaylightSavingTime
- MapUtilTest w/o Hibernate
- _(MapUtilTest)_ GlobalVariables.setMapProductionUrl
- _(MapUtilTest)_ Compare with reference image
- _(MapUtilTest)_ Only run one Mapyrus test
- _(BlogController)_ Throw exception when fetching fails
- _(BlogController)_ Tune logging
- _(BlogController)_ Tune logging
- _(PushNotificationUtil)_ Tune logging
- _(PushNotificationUtil)_ New HttpClient instance
- _(PushNotificationUtil)_ Test byte array length
- Debug logging for blogs and push notifications

## [3.1.7] - 2021-03-16

### 🐛 Bug Fixes

- _(PushSubscriptionController)_ Detached entity passed to persist
- _(PushSubscriptionController)_ AlbinaException: HTTP/1.1 201 Created

### ⚙️ Miscellaneous Tasks

- _(PushNotification)_ Send to correct regions
- _(statistics)_ Add additional avalanche problems and matrix information to statistics output
- _(PublicationController)_ Tune logging

## [3.1.6] - 2021-03-09

### ⚙️ Miscellaneous Tasks

- _(SocialMediaUtil)_ Use fd_albina_thumbnail.jpg only for Web Push

## [3.1.5] - 2021-03-09

### 🚀 Features

- _(push-notifications)_ Send new blog posts

### 🐛 Bug Fixes

- Properly install org.bouncycastle in Tomcat
- Properly install org.bouncycastle in Tomcat
- _(BlogController)_ LastFetch by blogId

### 🚜 Refactor

- Remove Hibernate ENVERS configuration
- Use Stream.anyMatch
- Use Stream.findFirst
- Use Collection.removeIf
- _(AlbinaUtil.getValidityDateString)_ Offset parameter
- _(SimpleHtmlUtilTest)_ Assert in unit test
- _(BlogController)_ Use Table for blog IDs/URLs
- Final fields
- Logging

### ⚙️ Miscellaneous Tasks

- _(SocialMediaUtil)_ Use fd_albina_thumbnail.jpg
- _(push-notifications)_ Delete subscriptions after 10 failed attempts
- _(push-notifications)_ Add bulletin URL to notification
- _(push-notifications)_ "image"

## [3.1.4] - 2021-02-16

### 🚀 Features

- _(observations)_ Proxy gis.tirol.gv.at for lwdkip

### 🐛 Bug Fixes

- _(PushNotification)_ Use correct bouncycastle version
- Update mysql-connector-java to fix timezone issue
- _(BlogController)_ FetchImages
- _(BlogController)_ BlogIdSouthTyrolIt typo

### 🚜 Refactor

- _(PushNotification)_ Exclude some unneeded dependencies from nl.martijndwars:web-push
- Migrate to json-schema-validator
- _(BlogController)_ Use Stream.findFirst

### ⚙️ Miscellaneous Tasks

- _(simple-html)_ Use webp
- _(push-notifications)_ Keys for all environments
- _(observations)_ Getenv ALBINA_JWT_SECRET
- Add version to GlobalVariables and index.jsp
- Remove GIT_VERSION from tomcat path

## [3.1.3] - 2021-02-05

### 🐛 Bug Fixes

- _(telegram-channel)_ Send blogs without image, fix #211

### Sentry

- Add missing artifact io.sentry:sentry-log4j

## [3.1.2] - 2021-02-04

### 🐛 Bug Fixes

- _(TelegramChannelProcessorController)_ Use URIBuilder

## [3.1.1] - 2021-02-04

### 🚀 Features

- _(observations)_ GET
- _(observations)_ POST
- _(observations)_ PUT
- _(observations)_ DELETE
- _(observations)_ Return observation object

### 🐛 Bug Fixes

- _(email)_ Add link to avalanche problems (pm)
- _(blogs)_ Set config for blogs
- _(AuthenticationController)_ TokenEncodingSecret

### 🚜 Refactor

- Use LocalDateTime for Observation

### ⚙️ Miscellaneous Tasks

- _(blogs)_ Check for new blogs every 10 minutes
- _(blog)_ Update comment
- _(pdf)_ Show all avalanche problems
- _(email)_ Add all avalanche problems
- _(simple-html)_ Add all avalanche problems
- _(observations)_ Add observations.sql

## [3.1.0] - 2021-01-25

### 🚀 Features

- Add support for push-notifications

### 🐛 Bug Fixes

- _(BlogController)_ Messages.getString("avalanche-report.name")
- _(BlogController)_ Object.getString("title")
- Rename SQL column KEY to P256DH
- _(pdf)_ Fix #209
- _(email)_ Add tendency text

### 🚜 Refactor

- _(BlogController)_ POJO for Blogger API
- Use LanguageCode.getBundleString
- Extract interface SocialMediaUtil

### ⚙️ Miscellaneous Tasks

- Persist push-notifications
- Remove push-notifications
- Fetch push-notifications for newsletter
- _(push)_ Send after update
- _(PushSubscription)_ Add subscribeDate
- _(PushSubscription)_ Add failedCount
- _(i18n)_ Update translations
- _(email)_ Add links to danger patterns and avalanche problems

## [3.0.12] - 2020-12-21

### 🐛 Bug Fixes

- _(social-media)_ Only publish on social media if maps were created
- _(rapidmail)_ Unsubscribe link

### 🚜 Refactor

- _(avalanche-bulletin)_ Remove obsolete comment
- _(textcat)_ Remove obsolete comment
- _(templates)_ Delete unused templates

### ⚙️ Miscellaneous Tasks

- _(notes)_ Add sql script
- _(notes)_ Make notes persistent
- _(univie)_ Delete map production code for univie
- _(import-sql)_ Delete obsolete queries

## [3.0.11] - 2020-12-12

### 🐛 Bug Fixes

- _(publication)_ Order of publication steps

## [3.0.10] - 2020-12-11

### 🐛 Bug Fixes

- _(social-media)_ Delete duplicate slashs in attachment url

## [3.0.9] - 2020-12-11

### 🐛 Bug Fixes

- _(social-media)_ Attachment url

## [3.0.8] - 2020-12-09

### 🐛 Bug Fixes

- _(social-media)_ Use avalanche.report as base url for images

## [3.0.7] - 2020-12-09

### 🐛 Bug Fixes

- _(simple-html)_ Create simple html for whole euregio

## [3.0.6] - 2020-12-09

### 🚜 Refactor

- _(junit)_ Move messenger people test to own class

### ⚙️ Miscellaneous Tasks

- _(telegram-channel)_ Add logging information on error!
- Choose file extension for map files
- _(telegram-channel-test)_ Add test
- _(messenger-people)_ Add logging in case of error

## [3.0.5] - 2020-12-07

### 🐛 Bug Fixes

- _(maps)_ Tranparency of overlays

## [3.0.4] - 2020-12-07

### 🐛 Bug Fixes

- _(i18n)_ Decreasing tendency in DE
- _(email)_ Check for null for tendency
- _(telegram-channel)_ Message for non update

### 🚜 Refactor

- _(messengerpeople)_ Add logging information
- _(telegram-channel)_ Add logging

## [3.0.3] - 2020-12-04

### 🐛 Bug Fixes

- _(map)_ Add euregio logo on overview maps
- _(map)_ Create transparency in PNG overlays
- _(messengerpeople-controller)_ Stupid lt / gt error for targeting

### ⚙️ Miscellaneous Tasks

- Add user Andrea to tests
- _(map)_ Add logging info

## [3.0.2] - 2020-12-03

### 🚀 Features

- XmlUtilTest.createOldCaamlFiles

### 🐛 Bug Fixes

- _(social-media)_ Send only in DE, IT, EN to social media channels

### ⚙️ Miscellaneous Tasks

- _(tests)_ Update junit to 4.13.1

## [3.0.1] - 2020-11-20

### 🐛 Bug Fixes

- Decode path to scripts
- Change filename of xml files in latest directory
- _(XmlUtil)_ Check datetime for null

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
- _(id)_ Add prefix to id
- Update caaml v6 example file (fix ids)
- Check avalanche situations for null
- Set complexity of bulletin in copy method
- JSON schema, JSON unit tests, drop jsonassert
- Use ID of bulletins without prefix
- _(CAAML)_ Replace xml:id, xml:lang with native types
- _(i18)_ Use correct ResourceBundle
- _(MessagesBundle_de)_ Encoding
- Persistent weak layers
- _(MessagesBundle)_ Encoding
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
- _(i18n)_ Change expected test result
- _(i18n)_ Translations for danger patterns
- _(test)_ Enumerations test

### 🚜 Refactor

- _(Regions)_ Collection class
- Use ResourceBundle for language dependent string
- _(DangerPattern)_ Use ResourceBundle
- _(DangerRating)_ Use ResourceBundle
- DangerRating#getDangerRatingColor
- Add comment
- Add new line at end of file
- _(API)_ Version parameter for CAAML v6
- LanguageCode.getBundle
- Add i18n file for region names, delete region name maps from AlbinaUtil
- Move DangerRating.getDouble
- Remove comment
- Delete comment
- Delete comments
- Move sql scripts to own folder

### ⚙️ Miscellaneous Tasks

- Add sql files
- _(JSON)_ Update json schema and example file
- _(CAAML)_ Use danger rating type in avalanche situation
- Allow definition of the direction for the danger rating of an avalanche problem
- Delete variable isManualDangerRating
- Add RegionTest
- _(Region)_ Parse from GeoJSON
- _(Region)_ Parse regions from GeoJSON
- _(MapUtil)_ Test createBulletinRegions
- Add complexity as an attribute to caaml v6
- Add transifex config
- Extend API to deliver CAAMLv6
- _(CAAML)_ Trim texts
- _(CAAML)_ Trim region name
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
- _(simple-html)_ Remove regions, add avalanche problem text
- _(i18n)_ Replace i18n property files with xml files (utf-8 support)
- _(i18n)_ Add XML support for resource bundles
- _(i18n)_ Use xml property files for translations
- _(i18n)_ Update translations

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
- _(simpleHTML)_ Add links to other languages and link to standard view
- _(TelegramChannel)_ Add telegram channels for publication

### 🐛 Bug Fixes

- _(AvalancheReportController)_ Concurrent modification of map
- _(AvalancheBulletinService)_ Return empty JSON
- Update resource files after publication finished
- _(scripts)_ Update scripts
- _(GlobalVariables)_ Fix wrong text in simple html.
- _(CAAML)_ Add srcRef to MetaData
- _(CAAML)_ LocRef belongs after bulletinResultsOf
- _(CAAML)_ Empty <AvProblem/>
- _(MessengerPeopleProcessorController)_ Add logging
- _(ExtFiles)_ Add description to ext files in CAAML
- _(AvalancheSituation)_ Initialize avalanche situations correctly
- _(AvalancheBulletin)_ LinkReferenceURI

### 🚜 Refactor

- Fix comments
- _(AvalancheReportController)_ Use putAll()
- Migrate to openjson
- _(AvalancheBulletin)_ Extract getAvProblem method
- Extract CaamlValidator.validate()
- _(CaamlVersion)_ Merge generation code
- AvalancheBulletinDaytimeDescription.getAvalancheSituations

### ⚙️ Miscellaneous Tasks

- Remove config parameter createCaaml (always create it)
- _(XmlUtilTest)_ Add createValidCaaml
- _(CAAML)_ Remove unused xmlns:app
- _(CAAML)_ Add xmlns:schemaLocation
- _(CAAMLv6)_ Allow creation of CAAMLv6
- _(MatrixInformation)_ Add matrix information to avalanche problems, allow 5 avalanche problems
- _(AvalancheBulletin)_ Add text part highlights
- _(terrainFeature)_ Add field terrain feature to avalanche situation and bulletin daytime description
- _(TelegramChannel)_ Send new blog posts to telegram channel
- _(Statistics)_ Allow AM/ṔM entries for each day in statistics

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
- _(MapUtil)_ Create regions file
- Save JSON file during publication

### 🐛 Bug Fixes

- _(pom.xml)_ Exclude slf4j-simple
- _(MapUtilTest)_ Slowness
- Create JSON at the end of the publication
- Refactor
- Create small json
- _(MapUtil)_ Fix bulletin index
- _(JsonUtil)_ Typo
- _(CAAML)_ Typo "begionPosition"
- _(CAAML)_ "uom" is required for "elevationRange"
- _(CAAML)_ Add srcRef to MetaData
- _(CAAML)_ LocRef belongs after bulletinResultsOf
- _(CAAML)_ Empty <AvProblem/>
- _(BlogController)_ NPE for blogs w/o images
- _(BlogController)_ Incorrect URL for IT-32-BZ/it
- Sort bulletins by danger rating
- _(pom)_ Fix local fonts path
- Fix util test
- Delete unused strings for fonts
- Initialize fonts only once
- Create fonts for each pdf document (necessary)
- _(StatisticsController)_ Ignore tests
- _(UtilTest)_ Define validity date and publication time in tests
- _(PdfUtil)_ Close file handlers, refactor
- Allow regions to change owner (if no microregion of original owner is present anymore)

### 🚜 Refactor

- AvalancheBulletin.readBulletin
- Use GlobalVariables.regions
- MapUtil.getOverviewMapFilename
- Pass exceptions to logging
- Remove unused imports
- Use StandardCharsets
- _(AuthenticationController)_ Add algorithm field
- _(CaamlValidatorTest)_ Rename and simplify
- _(StatisticsControllerTest)_ Simplify and assert
- _(AvalancheBulletinJsonValidatorTest)_ Simplify
- _(SnowProfileJsonValidatorTest)_ Simplify
- _(AvalancheBulletinTest)_ Simplify
- _(JsonValidator)_ Use Resources.getResource
- _(MapUtilTest)_ Use Resources.getResource
- _(AvalancheBulleton)_ Use Resources.toString
- No need to decode date parameters
- _(AvalancheBulletin)_ Extract getAvProblem method
- _(BlogController)_ Drop StringBuilder

### 🎨 Styling

- Add .editorconfig
- Re-indent using tabs
- Use editorconfig for yml
- Trim trailing whitespace

### 🧪 Testing

- MapUtil.getOverviewMapFilename

### ⚙️ Miscellaneous Tasks

- _(MapUtil)_ Always generate pdf/png/webp/jpg maps
- _(MapUtil)_ Handle daytime dependency
- _(MapUtil)_ Decrease coordinate precision
- _(scripts)_ Copy .webp files
- _(MapUtil)_ Use paths from GlobalVariables
- _(MapUtil)_ Run map production in parallel
- _(MapUtil)_ Add logging
- _(CorsFilter)_ Tune logging
- _(MapUtil)_ Add logging
- _(AlbinaUtil)_ Tune logging
- _(log4j)_ Enable Sentry
- _(MapUtil)_ Add logging
- Default to locally produced mapyrus maps
- _(build)_ Add guava dependency
- _(XmlUtilTest)_ Add createValidCaaml
- _(CAAML)_ Remove unused xmlns:app
- _(CAAML)_ Add xmlns:schemaLocation

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
