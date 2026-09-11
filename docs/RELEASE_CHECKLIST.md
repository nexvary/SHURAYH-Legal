# SHURAYH Release Candidate Checklist

A build may be called a SHURAYH Release Candidate only when every mandatory item below has evidence in CI, source code, or an attached test report.

## Build & code quality
- [ ] Unit tests pass on the latest commit.
- [ ] Android lint passes on the latest commit.
- [ ] Debug/release candidate APK assembles on the latest commit.
- [x] Navigation routes are implemented as distinct destinations rather than same-screen placeholders.
- [x] App-level BackHandler/back navigation foundation exists for top-level pages.
- [ ] Arabic RTL visual regression proves no clipped/overlapping controls on compact and large phones.

## Legal workspace
- [x] Cases, clients, hearings and documents have distinct models.
- [x] Local create/delete workflows exist.
- [x] Hearings can be linked to cases.
- [x] Documents can be linked to cases.
- [x] Case detail surfaces linked records.
- [x] Search covers core workspace records.
- [ ] Edit forms are complete for every record type.
- [ ] Backup import/export UI round-trip is implemented and verified.

## Legal content
- [x] Legal content schema includes jurisdiction, source, version and verification metadata.
- [x] Unverified content is visibly warned.
- [ ] Official Egyptian-law corpus is ingested from authoritative/licensed sources.
- [ ] Update/version reconciliation is implemented.

## Document intelligence
- [x] Local summarization/review-gate foundation exists.
- [x] OCR provider boundary exists and defaults to no network transfer.
- [ ] Camera/gallery multi-page scan flow is implemented.
- [ ] A real OCR engine is integrated and benchmarked on Arabic legal pages.
- [ ] Original evidence is preserved when OCR/analysis is performed.
- [x] Forgery-detection claims remain disabled until backed by a validated model and confidence reporting.

## Security & privacy
- [x] Local-first behavior documented.
- [x] Core persisted workspace uses Android Keystore-backed AES-256-GCM encryption at rest.
- [x] Legacy plaintext workspace data has an automatic encrypted migration path.
- [x] Backup integrity/checksum foundation exists.
- [x] Password-derived PBKDF2-SHA256 + AES-GCM encrypted backup codec is implemented and unit-tested.
- [x] Two-step serial format validation exists with no trial workflow.
- [ ] Imported attachments/scans use encrypted-at-rest storage.
- [ ] Encrypted backup export/import UI is connected to Android document picker.
- [ ] Production activation service verification is implemented.
- [ ] Privacy notice is finalized.

## Hearings & reminders
- [x] Inexact AlarmManager reminder scheduler exists without exact-alarm privilege.
- [x] Reminder BroadcastReceiver is non-exported.
- [x] Android 13+ notification denial is handled without crashing.
- [ ] Structured hearing date/time input is wired to scheduler.
- [ ] Runtime notification permission UX is completed.

## Release engineering
- [ ] Android 15 device/emulator smoke suite passes.
- [ ] Accessibility/RTL regression suite passes.
- [ ] Signed release build is configured outside source control.
- [ ] Versioning/changelog/release notes are finalized.

Until all mandatory unchecked production items are resolved, CI-successful APKs are development builds rather than production releases.
