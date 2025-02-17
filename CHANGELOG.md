# Changelog

<!-- Update using `git-cliff -u -p CHANGELOG.md -t <TAG>` before creating new tag <TAG> with git. -->

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
