package com.nexvary.shurayh.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceBackupSerializerTest {
    @Test fun workspace_round_trip_survives_encryption() {
        val original = ShurayhUiState(
            cases = listOf(LegalCase("c1", "قضية نسخة", "محكمة القاهرة", "55/2026", "موكل تجريبي", "2026-10-01 09:30", CaseStatus.ACTIVE)),
            clients = listOf(Client("p1", "موكل تجريبي", "TEST-PHONE", null, "ملاحظة اختبار")),
            hearings = listOf(Hearing("h1", "c1", "قضية نسخة", "محكمة القاهرة", "2026-10-01", "09:30", "جلسة")),
            documents = listOf(LegalDocument("d1", "مذكرة", "مذكرة دفاع", "قضية نسخة", "2026-09-12")),
            lawBooks = emptyList()
        )
        val payload = WorkspaceBackupSerializer.serialize(original)
        val password = "BackupPassword-1080".toCharArray()
        val encrypted = EncryptedBackupCodec.encrypt(payload, password)
        val envelopeJson = WorkspaceBackupSerializer.encryptedEnvelopeToJson(encrypted)
        val decodedEnvelope = WorkspaceBackupSerializer.encryptedEnvelopeFromJson(envelopeJson).getOrThrow()
        val decrypted = EncryptedBackupCodec.decrypt(decodedEnvelope, password).getOrThrow()
        val restored = WorkspaceBackupSerializer.deserialize(decrypted).getOrThrow()

        assertEquals(original.cases, restored.cases)
        assertEquals(original.clients, restored.clients)
        assertEquals(original.hearings, restored.hearings)
        assertEquals(original.documents, restored.documents)
        assertTrue(envelopeJson.contains("SHURAYH-ENCRYPTED-BACKUP"))
        assertTrue(!envelopeJson.contains("ملاحظة اختبار"))
    }

    @Test fun foreign_backup_format_is_rejected() {
        val result = WorkspaceBackupSerializer.deserialize("{\"format\":\"OTHER\",\"version\":1,\"cases\":[],\"clients\":[],\"hearings\":[],\"documents\":[]}")
        assertTrue(result.isFailure)
    }
}
