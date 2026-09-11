# SHURAYH Release Candidate Checklist

A build may be called a SHURAYH Release Candidate only when every mandatory item below has evidence in CI, source code, or an attached test report.

## Build & code quality
- [ ] Unit tests pass.
- [ ] Android lint passes.
- [ ] Debug/release candidate APK assembles.
- [ ] No dead navigation routes.
- [ ] Back navigation is verified for all top-level screens.
- [ ] Arabic RTL layout has no clipped or overlapping controls on compact and large phones.

## Legal workspace
- [x] Cases, clients, hearings and documents have distinct models.
- [x] Local create/delete workflows exist.
- [x] Hearings can be linked to cases.
- [x] Documents can be linked to cases.
- [x] Case detail surfaces linked records.
- [x] Search covers core workspace records.
- [ ] Edit forms are complete for every record type.
- [ ] Import/export round-trip is implemented and verified.

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
- [ ] Forgery-detection claims are disabled unless backed by a validated model and confidence reporting.

## Security & privacy
- [x] Local-first behavior documented.
- [x] Backup integrity/checksum foundation exists.
- [x] Two-step serial format validation exists with no trial workflow.
- [ ] Sensitive fields use Keystore-backed encryption at rest.
- [ ] Exported backups use authenticated encryption.
- [ ] Production activation service verification is implemented.
- [ ] Privacy notice is finalized.

## Release engineering
- [ ] Android 15 device/emulator smoke suite passes.
- [ ] Accessibility/RTL regression suite passes.
- [ ] Signed release build is configured outside source control.
- [ ] Versioning/changelog/release notes are finalized.

Until all mandatory unchecked production items are resolved, CI-successful APKs are development builds rather than production releases.
