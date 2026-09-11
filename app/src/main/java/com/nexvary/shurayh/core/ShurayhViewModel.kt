package com.nexvary.shurayh.core

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class ShurayhUiState(
    val cases: List<LegalCase> = LegalRepository.cases,
    val clients: List<Client> = LegalRepository.clients,
    val hearings: List<Hearing> = LegalRepository.hearings,
    val lawBooks: List<LawBook> = LegalRepository.lawBooks,
    val documents: List<LegalDocument> = LegalRepository.documents,
    val searchQuery: String = ""
)

class ShurayhViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ShurayhUiState())
    val uiState: StateFlow<ShurayhUiState> = _uiState.asStateFlow()

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun addClient(name: String, phone: String, notes: String): Boolean {
        if (name.isBlank() || phone.isBlank()) return false
        val item = Client(UUID.randomUUID().toString(), name.trim(), phone.trim(), notes = notes.trim())
        _uiState.value = _uiState.value.copy(clients = listOf(item) + _uiState.value.clients)
        return true
    }

    fun addCase(title: String, court: String, caseNumber: String, clientName: String, nextSession: String): Boolean {
        if (title.isBlank() || court.isBlank() || caseNumber.isBlank() || clientName.isBlank()) return false
        val item = LegalCase(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            court = court.trim(),
            caseNumber = caseNumber.trim(),
            clientName = clientName.trim(),
            nextSession = nextSession.ifBlank { "غير محدد" }.trim(),
            status = CaseStatus.ACTIVE
        )
        _uiState.value = _uiState.value.copy(cases = listOf(item) + _uiState.value.cases)
        return true
    }
}
