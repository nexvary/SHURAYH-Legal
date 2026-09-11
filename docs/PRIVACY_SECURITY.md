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
The current workspace persistence encrypts the complete serialized cases/clients/hearings/documents payload with AES-256-GCM. The AES key is generated and held by Android Keystore and is not embedded in source code. Existing plaintext v1 local data is readable once and migrated to encrypted v2 storage on load.

The current model protects the persisted workspace payload at rest. Future imported attachments and scan-image files require the same encryption policy before they are allowed to contain real client evidence.

## Backups
Backup integrity uses SHA-256 checksums to detect accidental or malicious modification. Integrity alone is not confidentiality. Production exported backups containing client data still require authenticated encryption before export.

## Licensing
The two-step serial validator currently validates structure only. Production activation must be verified by a protected NEXVARY activation service and must not embed signing secrets in the Android application.

## Release blockers
A public production release remains blocked until encrypted attachment/backup export, signed-release configuration, final privacy notice, official legal-content provenance, production activation verification, and device-level regression testing are complete.
