package com.nexvary.shurayh

import com.nexvary.shurayh.core.CaseStatus
import com.nexvary.shurayh.core.LegalRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegalRepositoryTest {
    @Test
    fun caseIdsAreUnique() {
        val ids = LegalRepository.cases.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun hearingsPointToExistingCases() {
        val caseIds = LegalRepository.cases.map { it.id }.toSet()
        assertTrue(LegalRepository.hearings.all { it.caseId in caseIds })
    }

    @Test
    fun dashboardHasActiveCases() {
        assertTrue(LegalRepository.cases.any { it.status == CaseStatus.ACTIVE })
    }

    @Test
    fun lawReferencesCarryVerificationWarning() {
        assertTrue(LegalRepository.lawBooks.all { it.sourceNote.contains("التحقق") })
    }
}
