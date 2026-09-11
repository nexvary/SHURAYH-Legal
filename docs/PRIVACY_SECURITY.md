# SHURAYH Privacy & Security Baseline

SHURAYH is designed as a local-first legal workspace. Client, case, hearing and document metadata is stored on-device and is not uploaded automatically.

## Data handling rules
- Minimize permissions and request only capabilities required by an explicit user action.
- Never transmit case files, scans, national IDs, phone numbers, notes or legal drafts to a remote service without an explicit feature, disclosure and consent path.
- OCR adapters are opt-in components. The default adapter is unconfigured and performs no network transfer.
- Any future cloud AI/OCR integration must document provider, data retention, jurisdiction, transport encryption and user consent before enablement.
- Legal references carry source/version/verification metadata. Unverified demo references must display a warning.
- Generated summaries and drafting assistance require lawyer review before professional use.

## Sensitive storage
The current implementation persists workspace data using app-private Android SharedPreferences. This is durable local storage but must not be described as encrypted-at-rest. Before a production release containing real client secrets or national IDs, migrate sensitive values to an Android Keystore-backed encrypted data layer and include migration tests.

## Backups
Backup integrity uses SHA-256 checksums to detect accidental or malicious modification. Integrity alone is not confidentiality. Production backups containing client data require authenticated encryption before export.

## Licensing
The two-step serial validator currently validates structure only. Production activation must be verified by a protected NEXVARY activation service and must not embed signing secrets in the Android application.

## Release blockers
A public production release is blocked until encrypted sensitive storage, signed-release configuration, privacy notice, official legal-content provenance, and device-level regression testing are complete.
