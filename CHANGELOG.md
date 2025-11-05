# Changelog

<!-- Update using `git-cliff -u -p CHANGELOG.md -t <TAG>` before creating new tag <TAG> with git. -->

## [8.0.0] - 2025-11-04

### 🚀 Features

- HibernateUtil.run without transaction
- *(whatsapp)* Use whapi.cloud
- *(general-headline)* Extended AvalancheBulletin model
- *(general-headline)* Added text part for general headline
- *(general-headline)* Retrieve general headline in AvalancheReport model
- *(general-headline)* Add custom-data -> ALBINA -> general-headline elements to bulletins
- *(general-headline)* Setting custom-data on bulletins with ALBINA/general-headline
- *(general-headline)* Add TODO for conditional general headline setting
- *(general-headline)* Conditional for setting generalHeadline only if non-empty
- *(general-headline)* Add general headline to PDF avalanche reports
- *(general-headline)* Fix null check for general headline
- *(general-headline)* Added general-headline to email template
- *(general-headline)* Add enableGeneralHeadline field to Region model and JSON serialization
- *(general-headline)* Added DB migrations for general headline
- *(enable-text-input)* Add support for ENABLE_EDITABLE_FIELDS column in regions table and Region model
- *(enable-text-input)* Renamed liquibase migration script
- *(general-headline)* Ensure consistent general headline updates across bulletins
- *(general-headline)* Add support for generalHeadlineComment in CAAML5
- *(enable-weather-text-input)* Added field ENABLE_WEATHER_TEXT_FIELD in region
- *(enable-weather-text-input)* Added SYNOPSIS_COMMENT_TEXTCAT field to bulletins
- *(enable-weather-text-input)* Add support for weather synopsis in bulletins and PDFs
- *(RegionService)* Partial update of region object
- *(Region)* ENABLED_EDITABLE_FIELDS
- *(map)* Add ES-AR
- *(map)* Add ES-AR (buffer extent)
- *(map)* Add ES-AR (bounds.geojson)
- Configure database from environment variables

### 🐛 Bug Fixes

- *(danger-sources)* Check for null
- *(danger-sources)* Check for null
- *(danger-sources)* Test
- *(danger-sources)* Fix replacement key
- *(danger-sources)* Check treeline for null
- *(region)* Set server instance for new region definitions
- Failed test after whatsapp introduction
- *(danger-sources)* Check for null for treeline elevations
- *(MapUtilTest)* Update test resources
- *(danger-sources)* Check for null
- *(aspects)* Sorting, add tests
- *(aspects)* Fix text replacements for danger source variants
- *(general-headline)* Update column type and default value for ENABLE_GENERAL_HEADLINE
- *(danger-sources)* Text creation for slabs with daytime dependency
- *(danger-sources)* Text generation for slabs and daytime dependency
- *(danger-sources)* Replacements for "Alarmzeichen" phrase
- *(danger-sources)* Concat add ons
- *(danger-sources)* String concat with semi colons
- *(danger-sources)* Omit useless replacements
- *(danger-sources)* Typo in add on sentence
- *(danger-sources)* Check for null in replacements
- *(general-headline)* Remove redundant null check for generalHeadlineComment
- *(Caaml6)* Unit tests
- *(EmailUtil)* Unit tests
- *(EmailUtil)* Unit tests
- Eu.albina.model.User.toMediumJSON
- *(danger-sources)* Typo in text replacements
- *(RegionLanguageConfiguration)* Add missing no-arg constructor
- *(SimpleHtmlUtilTest)* URLs contain publication timestamp
- *(JsonUtilProvider)* Priority
- StatisticsControllerTest
- *(websockets)* Explicit encoder/decoder needed
- Serialization errors when calling /bulletin/status/publication
- Serialization errors when calling /bulletin/status/publication
- *(AvalancheReportController)* NPE
- *(tests)* Update test resources
- *(test)* Use correct translations in DE
- *(test)* Translations in ssml generation
- *(test)* Use new micro-regions for map production test
- *(test)* Mapyrus bindings
- *(test)* Adopt to new micro regions and micro region names
- *(test)* Update resource image
- *(email)* Add synopsis and general headline only if activated for region
- *(mapyrus)* Typo in comment
- *(mapyrus)* Change order of feature drawing
- *(test)* Update test resources
- *(tests)* Update test resource
- *(test)* Update test resource
- *(test)* Update test resource
- *(test)* Update test resources
- *(enable-weather-text-input)* Correct condition for weather text field inclusion in bulletins
- *(enable-weather-text-input)* Add condition to show weather synopsis in CAAML v6 bulletins
- *(enable-weather-text-input)* Injecting region model to resolve region specific JSON output like weather synopsis
- *(Caaml6)* Strings.isNullOrEmpty
- *(text-substitutions)* Use Pattern.quote
- *(text-substitutions)* Remove Pattern.quote
- *(danger-sources)* Replacement for steepness
- *(danger-sources)* Placeholder for Gefahrenstellen05§an_Expositionen
- *(danger-sources)* Db entries containing Gefahrenstellen05§an_Expositionen
- *(danger-sources)* Remove Expo1 part in SQL statements
- *(danger-sources)* Remove Expo1 part from placeholder comment
- *(danger-sources)* Remove "wieviele" from "möglich." replacements
- Use map_level instead of level
- *(danger-sources)* Remove closing bracket from placeholder text for danger peak
- *(danger-sources)* Danger signs placeholder text
- *(danger-sources)* Use add on sentence for all options of remote triggering
- *(danger-sources)* Replace terrain types correct
- *(danger-sources)* Check terrain types for null
- *(danger-sources)* Add on for natural avalanches unlikely
- *(HibernateUtil)* User.deleted is boolean
- *(tests)* Fix danger source variant json
- *(tests)* Update map image
- *(tests)* Update map image
- *(sql)* Use correct table name
- *(variant)* Column name for weak layer grain shape
- *(modify_danger_source_variants.sql)* Capitalization of table name
- RegionService.updateRegion with languageConfiguration
- RegionService.createRegion with languageConfiguration
- RegionTest
- *(RegionService)* `save` -> `update`
- *(AvalancheBulletinService)* JsonView only works with direct return
- CaamlValidatorTest

### 🚜 Refactor

- *(liquibase)* Move changesets into folders
- *(liquibase)* Rename legacy -> initial
- *(liquibase)* Changesets for v6
- *(liquibase)* Changesets for v7
- *(liquibase)* Remove unused properties file
- *(liquibase)* Hibernate annotations for consistency
- *(danger-sources)* Text concat
- *(Region)* Use jackson (de)serialization
- *(Region)* Generic region sets from JSON
- *(Region)* Remove remaining openjson
- Introduce eu.albina.rest.JsonUtilProvider
- Use Files.writeString
- Use Path.resolve
- *(SimpleHtmlUtil)* Unused return value
- Simplify RegionController
- Eu.albina.RegionTestUtils.readRegion
- *(ServerInstance)* Use automatic JSON serialization
- Use region from JSON for tests
- *(LinkUtil)* Move functions to Region.java
- *(PushNotification)* Use websiteName of region
- *(LinkUtil)* Inline and remove class
- Remove unused AlbinaException.toXML
- *(AvalancheBulletinStatusService)* Use automatic JSON serialization
- *(websocket)* Use automatic JSON serialization
- *(XMLResourceBundleControl)* Use jackson
- *(PushNotificationUtil)* Use jackson
- *(DangerSourceVariant)* Use jackson
- *(MediaFileService)* Use jackson
- *(RegionService)* Use jackson
- *(SubscriptionService)* Use jackson
- *(AvalancheBulletinTest)* Use jsonassert
- *(websocket)* Use automatic JSON serialization
- *(AvalancheBulletin)* Test automatic JSON serialization using jackson
- *(AvalancheBulletin)* Use automatic JSON serialization using jackson
- *(AvalancheBulletin)* Remove obsolete AvalancheBulletin.toSmallJSON
- *(AvalancheBulletin)* Remove obsolete AvalancheBulletin.toJSON
- *(AvalancheBulletin)* Test automatic JSON deserialization using jackson
- *(User)* Prepare automatic JSON serialization using jackson
- *(AvalancheBulletin)* Use automatic JSON deserialization using jackson
- *(AvalancheBulletin)* Use automatic JSON deserialization using jackson
- *(AvalancheBulletin)* Use automatic JSON deserialization using jackson
- *(AvalancheBulletin)* Use automatic JSON deserialization using jackson
- *(AvalancheBulletin)* Remove obsolete openjson code
- AlbinaException.toJSON
- Delete unused imports
- AvalancheBulletinController.checkBulletins
- AvalancheBulletinService.getLockedBulletins
- Update json-schema-validator to 1.5.8
- *(Subscriber)* Remove unused Subscriber.toJSON
- *(User)* [**breaking**] Use automatic JSON deserialization using jackson
- *(pom)* Remove now obsolete com.github.openjson dependency
- Replace Gson usages with JsonUtil (jackson)
- Migrate tests to net.javacrumbs.json-unit
- *(websocket)* Remove unused region endpoint
- *(websocket)* Remove unused chat endpoint
- *(PdfUtil)* Extract variables
- *(TelegramController)* Jakarta.ws.rs.core.EntityPart
- *(MapUtil)* Use bounds from avalanche-warning-maps
- Prefer Region.ttsLanguages over Region.isCreateAudioFiles
- RegionController.tryGetRegion
- LanguageCodeConverter.COLUMN_DEFINITION
- LanguageCode.Converter.class
- *(Region)* Use map center from eaws-regions
- *(PdfUtil)* Integer division in floating-point context
- Use LocalDate in LanguageCode.getDate
- *(mapyrus)* Compute ALB_ID
- MediaType.APPLICATION_PDF
- *(RegionService)* Use same endpoint for create and update
- *(ServerInstanceService)* Use same endpoint for create and update
- *(UserService)* Combine endpoints
- Consistent naming of functions
- Remove obsolete JSON schemas avalancheBulletin/avalancheIncident/news/snowProfile
- Replace slf4j-simple with java.util.logging
- Java.util.logging configuration examples

### 📚 Documentation

- *(liquibase)* Update README
- *(liquibase)* Add some tips

### ⚙️ Miscellaneous Tasks

- *(danger-sources)* Update textcat suggestions for danger source variants
- *(danger-sources)* Update text replacements depending on danger source variant
- *(images)* Add AT-02 logo
- *(logo)* Crop AT-02 logo
- *(i18n)* Add AT-02 strings
- *(multimedia-message)* Region dependent url formats
- *(MapUtilTest)* Write to target/test-results for easier debugging
- *(liquibase)* Use hibernate-liquibase plugin
- *(liquibase)* Fix v5 changesets
- *(liquibase)* More consistent db schemas
- *(liquibase)* Use diffTypes
- *(liquibase)* V7 changesets
- *(liquibase)* Annotate columns for automatic diff
- *(liquibase)* Remove table comment
- *(liquibase)* Align dev_db with liquibase/hibernate model
- Add missing dependency due to liquibase version upgrade
- *(liquibase)* Align dev_db with liquibase/hibernate model
- *(liquibase)* -- changeset albina:
- *(liquibase)* Danger_source_variant_regions
- *(liquibase)* Keep generic_observations
- *(liquibase)* Readme
- Move enabledLanguages to region
- Move TTS languages to region
- *(Region)* Fix tests
- *(Region)* Update columnDefinition
- *(danger-sources)* Implement add-on sentences
- *(danger-sources)* Add tests for text creation
- *(danger-sources)* Derive avalanche problem type from danger source variant
- *(danger-sources)* Disable tests
- *(general-headline)* Rename liquibase migration scripts
- *(liquibase)* Changeset id
- *(enable-weather-text-input)* Add avalanche service logos for Geosphere
- *(.gitlab-ci)* Enable merge request pipelines
- *(PublicationController)* Tune logging
- *(PdfUtil)* If synopsisComment
- *(Region)* Move i18n strings to database table
- Use website name from database
- Fix tests by mocking RegionController
- *(RegionLanguageConfiguration)* Avoid id column, use getters+setters
- *(Region)* LanguageConfiguration as part of Region.java
- Use RegionLanguageConfiguration instead of MessageBundle
- Logo+logoBw from database instead of MessageBundle
- *(EmailUtil)* Use websiteUrl of region object
- Use websiteName of region object
- Default values for updated region configuration
- *(Region)* Move staticUrl
- *(RegionLanguageConfiguration)* Liquibase changesets for initial data
- *(PublicationController)* Tune logging
- *(license)* Add SPDX header to all source files
- *(email)* Use warning service name as sender name
- *(publication)* Use 'avalanche forecast' in email subject and social media text
- *(i18n)* Update translations
- *(micro-region-names)* Update translations
- AvalancheBulletinTest.testCreateObjectFromJSONAndBack
- AvalancheBulletinTest.testCreateObjectFromJSONAndBack (JsonUtil.createJSONString)
- *(pom)* Update jackson to 2.19.2
- *(pom)* Update jetty-maven-plugin
- *(license)* Missing SPDX headers
- *(micro-region-names)* Update
- *(mapyrus)* Remove EUREGIO clauses from map production
- *(danger-sources)* Update texts for variants
- *(danger-sources)* Update substitution texts
- *(danger-sources)* Add grassy slopes
- *(mapyrus)* Use simplified passe partout for thumbnail maps
- *(CaamlTest)* Fix tests
- *(Caaml)* Skip empty bulletin texts
- *(EmailUtil)* Skip empty bulletin texts
- *(TextToSpeech)* Fix tests
- *(pom)* Upgrade slf4j 2.0.17
- *(tests)* Disable danger source text tests
- *(mapyrus)* Add micro region mapping
- *(AT-02)* Prepare test resources
- *(variant)* Allow multiple grain shapes for weak layer, closes #345
- *(db)* Add folder to db changelog
- *(danger-source)* Add glide_cracks to danger signs
- *(eaws-regions)* Update region names
- *(danger-source)* Add ownerRegion to dangerSource, adopt rest endpoints to load danger sources only for one region
- *(HealthCheckJob)* Checks Telegram, WhatsApp, Blog
- *(eaws-regions)* Update region names
- *(bom)* Update guava to 33.5.0
- *(pom)* Update mockito to 5.20.0
- *(pom)* Remove obsolete commons-lang3
- *(pom)* Remove unused micronaut-jaxrs-processor
- *(pom)* Update mariadb-java-client to 3.5.6
- *(pom)* Update google-auth-library-oauth2-http to 1.40.0
- *(pom)* Exclude bouncycastle for itextpdf
- *(pom)* Update itext to 9.3.0
- *(pom)* Use project.version as albina.conf.git.version
- *(pom)* Write version to MANIFEST.MF
- Application/rss+xml
- *(i18n)* Update translations

### @License

- GNU Affero General Public License

### Application

- DBMigration is automatic
- Org.jboss.logging.provider=slf4j

### AvalancheBulletin

- Remove FetchType.LAZY

### AvalancheBulletinDaytimeDescription

- Missing @JsonIgnore

### AvalancheBulletinPublishService

- No HttpResponse

### AvalancheBulletinService

- No JsonUtil.parseUsingJackson
- @Secured
- No HttpResponse
- SystemFile, no HttpResponse

### AvalancheBulletinService.deleteJSONBulletin

- Try Isolation.SERIALIZABLE

### AvalancheBulletinStatusService

- No HttpResponse

### AvalancheBulletinTest.readBulletinsUsingJackson

- Use io.micronaut.serde.ObjectMapper

### AvalancheReport.createJsonFile

- Use io.micronaut.serde.ObjectMapper

### AvalancheReport.getPublishedBulletins

- Use io.micronaut.serde.ObjectMapper

### AvalancheReportController

- Use io.micronaut.serde.ObjectMapper

### BlogJob

- Fix regionController

### BlogService

- No HttpResponse

### Blogger+Wordpress

- Use io.micronaut.serde.ObjectMapper
- Use record

### DangerSourceController

- @Singleton and @Inject

### DangerSourceService

- No JsonUtil.writeValueUsingJackson
- Use HttpResponse.ok
- No @OpenAPIDefinition
- No HttpResponse
- DangerSourceRepository.save
- @Transactional
- Get rid of `Range<Instant>`

### DangerSourceService.saveDangerSource

- No {id} in path, return dangerSource

### DangerSourceVariantRepository

- Fix EntityExistsException: detached entity passed to persist: eu.albina.model.DangerSourceVariant
- Use Range<Instant>
- Fix validFrom (instead of creationDate)
- Try @Join
- Fix @Join
- Do not use @Join (reduce complexity)

### DangerSourceVariantsStatus

- Convert to record

### JsonUtil

- Remove all obsolete functions

### LoggingHeadersFilter

- Parse username from token
- Parse username from token

### MediaFileService

- No HttpResponse

### OpenApiService

- Fix openapi.json url

### PublicationController

- @Singleton and @Inject

### PushNotificationService

- No HttpResponse

### PushNotificationUtil

- Use io.micronaut.serde.ObjectMapper

### PushNotificationUtilTest

- Use io.micronaut.serde.ObjectMapper

### README

- Document micronaut deployment
- Journalctl
- JDBC_URL
- Format markdown
- Fix WorkingDirectory in systemd
- Deployment

### RapidMailController

- Use io.micronaut.serde.ObjectMapper
- SubjectMatter

### Region

- :new: use io.micronaut.serde.ObjectMapper
- Remove constructor involving ObjectMapper

### RegionController

- @Singleton and @Inject

### RegionService

- No HttpResponse

### RegionTestUtils

- @Singleton

### RssUtil

- Use try-with-resources

### SchedulerUtil

- @io.micronaut.scheduling.annotation.Scheduled

### ServerInstanceController

- @Singleton and @Inject

### ServerInstanceService

- No HttpResponse

### StatisticsControllerTest

- Use io.micronaut.serde.ObjectMapper

### StatisticsService

- StreamedFile, no HttpResponse

### StressLevelService

- No HttpResponse

### StressLevelTest

- Use io.micronaut.serde.ObjectMapper

### SubscriptionService

- Remove dead code
- Remove dead code
- Fix duplicate entry for email address
- No HttpResponse

### TelegramController

- Send photo via HTTP URL instead of multipart/form-data

### Tests

- Use io.micronaut.serde.ObjectMapper

### TextToSpeech

- Use io.micronaut.serde.ObjectMapper

### UserController

- @Singleton and @Inject

### UserRepository.authenticate

- Do not expose too much details
- Do not print full error log

### UserService

- No HttpResponse
- HTTP 403 FORBIDDEN

### Websockets

- AI assisted migration to micronaut

### WhatsAppController

- Use io.micronaut.serde.ObjectMapper

### Eu.albina.caaml.Caaml6

- @Singleton for io.micronaut.serde.ObjectMapper

### Org.caaml.v6

- @Serdeable

### Pom.xml

- Project.build.sourceEncoding
- Maven.compiler.release

## [7.1.11] - 2025-04-17

### Bulletins

- Synchronize publication process
- Export draft bulletins with regions on authenticated endpoint

## [7.1.10] - 2025-04-09

### Danger Sources

- Create textcat from danger sources

### Bulletins

- Allow to publish all regions without messages (admin)

## [7.1.9] - 2025-03-18

### Danger Sources

- Add endpoint to export statistics

## [7.1.8] - 2025-03-17

### Bulletins

- Add authenticated endpoint to retrieve non published bulletins in CAAMLv6 format

### 🐛 Bug Fixes

- Set timeout for TTS API calls in addition to connectTimeout
- Do not create a report if no changes happened at 8AM

## [7.1.7] - 2025-03-03

### 🐛 Bug Fixes

- CorsFilter: Allow x-client-version
- Bulletin: Set status from DRAFT to MISSING if no bulletin
- Bulletin: Set status null if status was DRAFT and all bulletins were deleted
- Report: Set report status to missing if all bulletins were deleted

### Danger Sources

- Add average snow height for gliding snow
- Allow partly crusts above/below weak layer
- Extend terrain-types enum
- Extend danger-signs enum
- Extend creation-process enum
- Remove duplicate terrain types

## [7.1.6] - 2025-02-17

### 🐛 Bug Fixes

- Create avalanche reports always at 5pm and 8am regardless of status
- Fix upload of media file

## [7.1.5] - 2025-02-10

### 🚀 Features

- Danger level: add config for elevation dependent danger level (update database: `005_000150_extend_server_instances.sql`)

## [7.1.4] - 2025-01-27

### 🚀 Features

- API: create region bulletin PDF on demand

### ⚙️ Miscellaneous Tasks

- CorsFilter: allow if-none-match
- Update translations

## [7.1.3] - 2025-01-08

### 🐛 Bug Fixes

- Override existing mp3 file

### 📚 Documentation

- Use git-cliff to generate a changelog
- Document and set up workflow for changelog

### ⚙️ Miscellaneous Tasks

- Add region name to email subject
- Update translations

## [7.1.2] - 2024-12-19

### 🐛 Bug Fixes

- Handle super regions only once during publication

### ⚙️ Miscellaneous Tasks

- Update quartz scheduler
- Add HikariCP configuration for connection pooling.

## [7.1.1] - 2024-12-17

### 🐛 Bug Fixes

- Fix issues with publication and bulletin updates involving super regions

## [7.1.0] - 2024-12-17

### 🐛 Bug Fixes

- Synchronize create/delete/update operations

### ⚙️ Miscellaneous Tasks

- Team stress level per region
- Use git-cliff to generate a changelog

## [7.0.6] - 2024-12-09

### 🐛 Bug Fixes

- Fix database tables for DANGER_SIGN/TERRAIN_TYPE of danger sources
- Send notifications (Telegram, etc. ) **after** bulletin is published on website

## [7.0.5] - 2024-12-05

### 🐛 Bug Fixes

- Fix publication date for PDF preview of unpublished bulletins

## [7.0.4] - 2024-12-05 (and [7.0.3] - 2024-12-05)

### 🐛 Bug Fixes

- Fix problems related to HikariPool connections resulting from introduction of tech blog in v7.0.2

## [7.0.2] - 2024-12-02

### 🚀 Features

- Send new tech blogs via email
- Migrate textToSpeech to Google REST API

### ⚙️ Miscellaneous Tasks

- Remove PDF link in email due to dynamic pdf creation

## [7.0.1] - 2024-11-13

### 🐛 Bug Fixes

- Fix PDF preview of bulletins

## [7.0.0] - 2024-11-04

### 🚀 Features

- Enable textToSpeech for ES and CA
- Parallelize publication by region
- Add endpoint to store VR statistics
- Add team stress levels (update database: `005_000100_stress_levels.sql`)
- Create bulletin PDF on demand

<!-- generated by git-cliff -->
