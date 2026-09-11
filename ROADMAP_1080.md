# SHURAYH — Roadmap to Stage 1080

This document turns the requested stage count into auditable engineering milestones. A stage number is considered completed only when its corresponding code, test, or documentation exists in the repository.

## 001–120 — Foundation
- Android/Kotlin/Compose project
- SHURAYH identity and package
- Arabic-first RTL
- Android 15 target
- CI baseline

## 121–240 — Navigation and UX structure
- Real navigation graph
- Dedicated Cases, Clients, Hearings, Laws, Documents, Office, Search, Courts, About screens
- Correct system/app back behavior
- No same-screen placeholder routing

## 241–360 — Legal domain layer
- Case, Client, Hearing, LawBook, LegalDocument models
- Repository abstraction foundation
- Dashboard operational counts
- Case detail screen

## 361–480 — Search and information architecture
- Unified local search across cases, clients and legal references
- Structured office dashboard
- Status representation for cases
- Empty-state handling

## 481–600 — Quality gates
- Unit tests for unique IDs and cross-record integrity
- Legal reference verification-warning test
- Android lint in CI
- Debug APK generation in CI
- APK artifact publishing

## 601–720 — Persistent data track
Planned next engineering block:
- Room database
- DAOs for cases/clients/hearings/documents
- Migrations and seed import
- CRUD screens and validation
- encrypted storage policy for sensitive fields

## 721–840 — Legal content track
Planned next engineering block:
- Egyptian law content ingestion format
- version/source/update metadata per statute
- full-text legal search index
- bookmarks, annotations and cross-references
- explicit stale-law warnings

## 841–960 — Document workspace track
Planned next engineering block:
- multi-page scan intake
- OCR adapter interface
- document linking to cases/clients
- local export/report pipeline
- drafting workspace with lawyer-review gates

## 961–1080 — Release-hardening track
Planned next engineering block:
- notification/reminder architecture
- licensing/serial workflow
- backup/restore design
- accessibility and RTL regression suite
- release build hardening
- privacy/security review
- signed-release checklist

## Current verified implementation boundary
The repository now contains code corresponding to the foundation, navigation, domain, local search and first quality-gate blocks. The later blocks remain explicitly marked as planned until their code and tests are committed and CI-verified.
