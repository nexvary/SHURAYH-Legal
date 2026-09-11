package com.nexvary.shurayh.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShurayhPersistenceFlowTest {
    private class MemoryPersistence : LegalPersistence {
        var saved: PersistedLegalState? = null
        override fun load(): PersistedLegalState? = saved
        override fun save(state: PersistedLegalState) { saved = state }
    }

    @Test fun client_case_hearing_document_flow_is_persisted() {
        val store = MemoryPersistence()
        val vm = ShurayhViewModel(store)

        assertTrue(vm.addClient("موكل جديد", "01012345678", "ملاحظة", "12345678901234"))
        assertTrue(vm.addCase("دعوى اختبار", "محكمة القاهرة", "1/2026", "موكل جديد", "20 سبتمبر 2026"))
        val legalCase = vm.uiState.value.cases.first { it.title == "دعوى اختبار" }
        assertTrue(vm.addHearing(legalCase.id, "", "22 سبتمبر 2026", "10:30", "جلسة اختبار"))
        assertTrue(vm.addDocument("مذكرة اختبار", "مذكرة", legalCase.title, "12 سبتمبر 2026"))

        val saved = requireNotNull(store.saved)
        assertTrue(saved.clients.any { it.name == "موكل جديد" })
        assertTrue(saved.cases.any { it.id == legalCase.id })
        assertTrue(saved.hearings.any { it.caseId == legalCase.id })
        assertTrue(saved.documents.any { it.caseTitle == legalCase.title })
        assertEquals("22 سبتمبر 2026", vm.uiState.value.cases.first { it.id == legalCase.id }.nextSession)
    }

    @Test fun deleting_case_cleans_linked_hearings_and_documents() {
        val store = MemoryPersistence()
        val vm = ShurayhViewModel(store)
        assertTrue(vm.addCase("قضية للحذف", "محكمة", "2/2026", "موكل", "غدًا"))
        val legalCase = vm.uiState.value.cases.first { it.title == "قضية للحذف" }
        assertTrue(vm.addHearing(legalCase.id, "", "غدًا", "09:00", ""))
        assertTrue(vm.addDocument("مستند مرتبط", "مذكرة", legalCase.title, "اليوم"))

        vm.deleteCase(legalCase.id)

        assertFalse(vm.uiState.value.cases.any { it.id == legalCase.id })
        assertFalse(vm.uiState.value.hearings.any { it.caseId == legalCase.id })
        assertFalse(vm.uiState.value.documents.any { it.caseTitle == legalCase.title })
    }

    @Test fun case_status_cycles_through_workflow() {
        val vm = ShurayhViewModel()
        val id = vm.uiState.value.cases.first().id
        val before = vm.uiState.value.cases.first { it.id == id }.status
        vm.cycleCaseStatus(id)
        val after = vm.uiState.value.cases.first { it.id == id }.status
        assertTrue(before != after)
    }
}
