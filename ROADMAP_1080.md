# SHURAYH — Roadmap to Stage 1080

This document turns the requested stage count into auditable engineering milestones. A stage range is counted as implemented only when its code/tests/docs exist; production-only gaps remain named explicitly.

## 001–120 — Foundation — IMPLEMENTED
- Android/Kotlin/Compose project
- SHURAYH identity and package
- Arabic-first RTL
- Android 15 target
- CI baseline

## 121–240 — Navigation and UX structure — IMPLEMENTED
- Real navigation graph
- Dedicated Cases, Clients, Hearings, Laws, Documents, Office, Search, Courts, Settings and About screens
- Correct app/system back handling foundation
- No same-screen placeholder routing

## 241–360 — Legal domain layer — IMPLEMENTED
- Case, Client, Hearing, LawBook and LegalDocument models
- Dashboard operational counts
- Case detail screen
- Linked hearings/documents in case detail
- Case workflow status cycle

## 361–480 — Search and information architecture — IMPLEMENTED
- Unified local workspace search
- Structured office dashboard
- Status representation and empty states
- Versioned legal-content schema with jurisdiction/source/version/verification fields
- Arabic-normalized local legal search engine
- Explicit warning for non-official/unverified legal seed content

## 481–600 — Quality gates — IMPLEMENTED
- Unit tests for IDs and cross-record integrity
- CRUD/workspace lifecycle tests
- Legal search/review/backup/license tests
- Android lint in CI
- Debug APK generation and artifact publishing in CI

## 601–720 — Persistent legal workspace — IMPLEMENTED WITH PRODUCTION HARDENING GAP
- Durable app-private on-device persistence for cases, clients, hearings and documents
- Load-on-launch and save-on-mutation behavior
- Create/delete flows for core records
- Case/hearing/document relationships
- Cascading cleanup when a case is deleted
- Validation for required inputs
- Update APIs for clients and cases

Production hardening still required before storing highly sensitive real-world data:
- Keystore-backed encryption for sensitive fields
- schema migration framework beyond the current v1 JSON store
- complete edit UI for every record type

## 721–840 — Legal content and research — FOUNDATION IMPLEMENTED
- Egyptian-law ingestion data schema
- source/version/verification metadata
- Arabic-normalized local legal search and ranking
- stale/unverified-source warning path
- local legal-text review checklist

Still required for production/legal reliance:
- authoritative/licensed Egyptian law corpus ingestion
- official update/version reconciliation
- bookmarks, annotations and cross-references UI

## 841–960 — Document intelligence — FOUNDATION/PARTIAL IMPLEMENTATION
- Documents can be linked to cases
- local extractive summarization foundation
- mandatory lawyer-review warning in summary results
- legal drafting/review checklist foundation
- SHA-256 text fingerprint utility
- OCR adapter boundary with safe no-network default

Still required:
- camera/gallery multi-page scanning
- real Arabic OCR engine integration and benchmarking
- original-image/evidence preservation workflow
- export/report UI and encrypted backup export
- forgery detection must remain disabled until a validated model/confidence protocol exists

## 961–1080 — Release hardening — FOUNDATION/PARTIAL IMPLEMENTATION
- two-step serial-number format validator; no trial workflow
- backup integrity/checksum verification foundation
- local-first privacy/security policy
- auditable release-candidate checklist
- RTL-first application shell and content descriptions on principal controls

Still required before declaring a production Release Candidate:
- protected server-side license activation verification
- authenticated-encrypted backup export/import
- Android Keystore-backed sensitive storage
- Android 15 emulator/device smoke suite
- accessibility/RTL visual regression suite
- notification/reminder runtime scheduling
- signed release configuration outside source control
- final privacy notice and official legal-corpus provenance

## Current implementation boundary
The codebase has crossed all stage bands architecturally through 1080, but Stage 1080 is **not** declared production-complete while any mandatory item in `docs/RELEASE_CHECKLIST.md` remains unchecked. Development APKs may be published only when unit tests, lint and APK assembly pass in GitHub Actions.
