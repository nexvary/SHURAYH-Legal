package com.nexvary.shurayh.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegalToolsTest {
    @Test fun legal_search_returns_ranked_warning_for_unverified_seed() {
        val hits = LegalSearchEngine.search("عقود التزامات")
        assertTrue(hits.isNotEmpty())
        assertEquals("civil-demo-1", hits.first().article.id)
        assertTrue(hits.first().warning?.contains("غير موثق") == true)
    }

    @Test fun local_summary_is_shorter_and_review_gated() {
        val text = "الجملة الأولى عن القضية. الجملة الثانية عن المحكمة. الجملة الثالثة عن المستندات. الجملة الرابعة عن الدفاع. الجملة الخامسة عن الطلبات. الجملة السادسة عن الجلسة. الجملة السابعة عن المستندات مرة أخرى."
        val result = LocalLegalTextTools.summarize(text, 3)
        assertTrue(result.summary.length < text.length)
        assertTrue(result.warning.contains("المحامي"))
        assertTrue(LocalLegalTextTools.buildReviewChecklist(result.summary).isNotEmpty())
    }

    @Test fun backup_checksum_detects_tampering() {
        val envelope = BackupCodec.wrap("legal-data")
        assertTrue(BackupCodec.verify(envelope))
        assertFalse(BackupCodec.verify(envelope.copy(payload = "changed")))
    }

    @Test fun encrypted_backup_round_trip_and_wrong_password_fails() {
        val password = "StrongLegalBackup-2026".toCharArray()
        val envelope = EncryptedBackupCodec.encrypt("قضايا سرية", password)
        val restored = EncryptedBackupCodec.decrypt(envelope, password)
        assertTrue(restored.isSuccess)
        assertEquals("قضايا سرية", restored.getOrNull())
        assertTrue(EncryptedBackupCodec.decrypt(envelope, "WrongPassword-000".toCharArray()).isFailure)
    }

    @Test fun encrypted_backup_rejects_short_password() {
        val result = runCatching { EncryptedBackupCodec.encrypt("data", "short".toCharArray()) }
        assertTrue(result.isFailure)
    }

    @Test fun two_step_serial_requires_both_serial_and_confirmation() {
        val valid = TwoStepSerialValidator.validate(LicenseIdentity("install-1234", "SHU-AB12-CD34-EF56", "123456"))
        assertTrue(valid is LicenseCheck.ValidFormat)
        val invalid = TwoStepSerialValidator.validate(LicenseIdentity("install-1234", "BAD", "12"))
        assertTrue(invalid is LicenseCheck.Invalid)
    }
}
