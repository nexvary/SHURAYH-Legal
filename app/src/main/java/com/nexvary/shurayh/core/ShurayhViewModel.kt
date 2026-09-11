package com.nexvary.shurayh.core

import androidx.lifecycle.ViewModel
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

class ShurayhViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ShurayhUiState())
    val uiState: StateFlow<ShurayhUiState> = _uiState.asStateFlow()

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}
