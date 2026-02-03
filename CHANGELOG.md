# Changelog

<!-- Update using `git-cliff -u -p CHANGELOG.md -t <TAG>` before creating new tag <TAG> with git. -->

## [8.1.0] - 2026-02-02

### 🚀 Features

- Add status check for Telegram, WhatsApp and Email publication
- Retry email publication
- Add configuration parameter to enable LINEA export per province

### 🐛 Bug Fixes

- Fix url endpoint to create PDF
- Update micro region names

### ⚙️ Miscellaneous Tasks

- Do not store super-region reports in database

## [8.0.11] - 2026-01-05

### 🚀 Features

- New StatusService: determine API status for WhatsApp, Telegram, blog

### 🐛 Bug Fixes

- Publication: Republish super regions even if published region is not part of the super region

### ⚙️ Miscellaneous Tasks

- Update micronaut to 4.10.6

## [8.0.10] - 2025-12-10

### 🚀 Features

- matrix-parameter: show parameter in pdf only of avalanche type is slab

## [8.0.9] - 2025-12-05

### 🐛 Bug Fixes

- DangerSourceService: remove @Transactional for all GET endpoints
- AvalancheBulletinService: remove unwanted @Transactional
- DangerSourceService: queryParam is single region

## [8.0.8] - 2025-12-02

### 🐛 Bug Fixes

- TextToSpeech: omit region ID in filename
- Restrict overlay map coloring to publication region
- Filter bulletins for super regions in PublicationJob

## [8.0.7] - 2025-11-28

### 🐛 Bug Fixes

- PublicationJob: filter bulletins for super regions
- Fix typo "UPDATE"
- Fix AvalancheReportController.setAvalancheReportFlag

## [8.0.6] - 2025-11-28
- Fix CaamlTest

## [8.0.5] - 2025-11-18

### 🐛 Bug Fixes

- BlogControllerTest: move regionOverride to BlogController
- BlogJob: move regionOverride to BlogController

## [8.0.4] - 2025-11-18

### 🐛 Bug Fixes

- Return empty list instead of null (blogger)
- Danger sources: ON DELETE CASCADE (allow deleting variants)


## [8.0.3] - 2025-11-17

### 🐛 Bug Fixes

- Fix DangerSourceService.replaceVariants


## [8.0.2] - 2025-11-10

### 🐛 Bug Fixes

- Fix publication: getPublicReport in same thread
- Fix NPE in Region.getLanguageConfiguration
- Fix publication of tech blog

### 🚜 Refactor

- Refactor PublicationJob
- Fix RapidMailController (status scheduled)
- Remove com.google.auth dependency


## [8.0.1] - 2025-11-05

### 🐛 Bug Fixes

- BlogController: Fix updating lastPublishedTimestamp

### 🚜 Refactor

- Implement saveOrUpdate method in eu.albina.controller.CrudRepository

### 📚 Documentation

- Remove liquibase specific readme
- Add documentation for deployment (database)
- Document micronaut deployment


## [8.0.0] - 2025-11-04

### Breaking Changes

- This project is now licensed under the GNU Affero General Public License v3.0

### 🚀 Features

- Integrate Liquibase for database migrations
- Add a weather section to bulletins
- Add a general headline to bulletins
- Enable support for WhatsApp channels
- Introduce updated micro-regions
- Add new regions: ES-AR and AT-02
- Migrate to the Micronaut framework (Tomcat no longer required)
- Extend region configuration (languages, website name)
- Allow database configuration via environment variables
- Implement health checks for Telegram, WhatsApp, and Blog integrations
- Improve and extend danger sources
- Add bulletin text generation from danger sources

### 🚜 Refactor

- Remove WebSocket support
- Use Jackson for JSON serialization and deserialization
- Make map production fully generic
- Improve map generation process

### ⚙️ Miscellaneous Tasks

- Upgrade all dependencies to the latest versions
- Add documentation for deployment and database migration

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
