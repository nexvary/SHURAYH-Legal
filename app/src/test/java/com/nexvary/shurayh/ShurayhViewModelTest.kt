package com.nexvary.shurayh

import com.nexvary.shurayh.core.ShurayhViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShurayhViewModelTest {
    @Test
    fun addClientRequiresNameAndPhone() {
        val vm = ShurayhViewModel()
        val initial = vm.uiState.value.clients.size
        assertFalse(vm.addClient("", "01000000000", ""))
        assertFalse(vm.addClient("عميل", "", ""))
        assertEquals(initial, vm.uiState.value.clients.size)
    }

    @Test
    fun validClientIsAdded() {
        val vm = ShurayhViewModel()
        val initial = vm.uiState.value.clients.size
        assertTrue(vm.addClient("عميل اختبار", "01012345678", "ملاحظة"))
        assertEquals(initial + 1, vm.uiState.value.clients.size)
    }

    @Test
    fun validCaseIsAddedAsActive() {
        val vm = ShurayhViewModel()
        val initial = vm.uiState.value.cases.size
        assertTrue(vm.addCase("قضية اختبار", "محكمة اختبار", "1/2026", "عميل اختبار", "20 سبتمبر 2026"))
        assertEquals(initial + 1, vm.uiState.value.cases.size)
        assertEquals("قضية اختبار", vm.uiState.value.cases.first().title)
    }
}
