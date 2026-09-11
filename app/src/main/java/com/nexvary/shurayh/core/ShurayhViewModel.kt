package com.nexvary.shurayh.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ShurayhUiState(
    val cases: List<LegalCase> = LegalRepository.cases,
    val clients: List<Client> = LegalRepository.clients,
    val hearings: List<Hearing> = LegalRepository.hearings,
    val lawBooks: List<LawBook> = LegalRepository.lawBooks,
    val documents: List<LegalDocument> = LegalRepository.documents,
    val searchQuery: String = ""
)

class ShurayhViewModel(
    private val persistence: LegalPersistence = NoOpLegalPersistence
) : ViewModel() {
    private val initial = persistence.load()
    private val _uiState = MutableStateFlow(
        ShurayhUiState(
            cases = initial?.cases ?: LegalRepository.cases,
            clients = initial?.clients ?: LegalRepository.clients,
            hearings = initial?.hearings ?: LegalRepository.hearings,
            lawBooks = LegalRepository.lawBooks,
            documents = initial?.documents ?: LegalRepository.documents
        )
    )
    val uiState: StateFlow<ShurayhUiState> = _uiState.asStateFlow()

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun addClient(name: String, phone: String, notes: String, nationalId: String = ""): Boolean {
        if (name.isBlank() || phone.isBlank()) return false
        val item = Client(
            UUID.randomUUID().toString(), name.trim(), phone.trim(),
            nationalId.trim().takeIf { it.isNotBlank() }, notes.trim()
        )
        commit(_uiState.value.copy(clients = listOf(item) + _uiState.value.clients))
        return true
    }

    fun updateClient(item: Client): Boolean {
        if (item.name.isBlank() || item.phone.isBlank()) return false
        commit(_uiState.value.copy(clients = _uiState.value.clients.map { if (it.id == item.id) item else it }))
        return true
    }

    fun deleteClient(id: String) {
        commit(_uiState.value.copy(clients = _uiState.value.clients.filterNot { it.id == id }))
    }

    fun addCase(title: String, court: String, caseNumber: String, clientName: String, nextSession: String): Boolean {
        if (title.isBlank() || court.isBlank() || caseNumber.isBlank() || clientName.isBlank()) return false
        val item = LegalCase(
            id = UUID.randomUUID().toString(), title = title.trim(), court = court.trim(),
            caseNumber = caseNumber.trim(), clientName = clientName.trim(),
            nextSession = nextSession.ifBlank { "غير محدد" }.trim(), status = CaseStatus.ACTIVE
        )
        commit(_uiState.value.copy(cases = listOf(item) + _uiState.value.cases))
        return true
    }

    fun updateCase(item: LegalCase): Boolean {
        if (item.title.isBlank() || item.court.isBlank() || item.caseNumber.isBlank() || item.clientName.isBlank()) return false
        commit(_uiState.value.copy(cases = _uiState.value.cases.map { if (it.id == item.id) item else it }))
        return true
    }

    fun cycleCaseStatus(id: String) {
        val items = _uiState.value.cases.map { c ->
            if (c.id != id) c else c.copy(status = when (c.status) {
                CaseStatus.ACTIVE -> CaseStatus.PENDING
                CaseStatus.PENDING -> CaseStatus.CLOSED
                CaseStatus.CLOSED -> CaseStatus.ACTIVE
            })
        }
        commit(_uiState.value.copy(cases = items))
    }

    fun deleteCase(id: String) {
        val target = _uiState.value.cases.firstOrNull { it.id == id }
        commit(_uiState.value.copy(
            cases = _uiState.value.cases.filterNot { it.id == id },
            hearings = _uiState.value.hearings.filterNot { it.caseId == id },
            documents = _uiState.value.documents.filterNot { it.caseTitle == target?.title }
        ))
    }

    fun addHearing(caseId: String, court: String, date: String, time: String, notes: String): Boolean =
        addHearingRecord(caseId, court, date, time, notes) != null

    fun addHearingRecord(caseId: String, court: String, date: String, time: String, notes: String): Hearing? {
        val legalCase = _uiState.value.cases.firstOrNull { it.id == caseId } ?: return null
        if (!HearingDateTimeParser.isValid(date, time)) return null
        val hearing = Hearing(
            id = UUID.randomUUID().toString(), caseId = legalCase.id, caseTitle = legalCase.title,
            court = court.ifBlank { legalCase.court }.trim(), date = date.trim(), time = time.trim(), notes = notes.trim()
        )
        val cases = _uiState.value.cases.map { if (it.id == caseId) it.copy(nextSession = "${date.trim()} ${time.trim()}") else it }
        commit(_uiState.value.copy(cases = cases, hearings = listOf(hearing) + _uiState.value.hearings))
        return hearing
    }

    fun deleteHearing(id: String) {
        commit(_uiState.value.copy(hearings = _uiState.value.hearings.filterNot { it.id == id }))
    }

    fun addDocument(title: String, type: String, caseTitle: String?, updatedAt: String): Boolean {
        if (title.isBlank() || type.isBlank()) return false
        val doc = LegalDocument(
            id = UUID.randomUUID().toString(), title = title.trim(), type = type.trim(),
            caseTitle = caseTitle?.trim()?.takeIf { it.isNotBlank() }, updatedAt = updatedAt.ifBlank { "اليوم" }.trim()
        )
        commit(_uiState.value.copy(documents = listOf(doc) + _uiState.value.documents))
        return true
    }

    fun deleteDocument(id: String) {
        commit(_uiState.value.copy(documents = _uiState.value.documents.filterNot { it.id == id }))
    }

    fun replaceWorkspace(state: PersistedLegalState) {
        commit(
            _uiState.value.copy(
                cases = state.cases,
                clients = state.clients,
                hearings = state.hearings,
                documents = state.documents,
                searchQuery = ""
            )
        )
    }

    fun resetDemoData() {
        commit(ShurayhUiState())
    }

    private fun commit(state: ShurayhUiState) {
        _uiState.value = state
        persistence.save(PersistedLegalState(state.cases, state.clients, state.hearings, state.documents))
    }
}

class ShurayhViewModelFactory(private val persistence: LegalPersistence) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ShurayhViewModel(persistence) as T
}
