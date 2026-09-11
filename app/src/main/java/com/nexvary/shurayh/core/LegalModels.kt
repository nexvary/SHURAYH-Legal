package com.nexvary.shurayh.core

data class LegalCase(
    val id: String,
    val title: String,
    val court: String,
    val caseNumber: String,
    val clientName: String,
    val nextSession: String,
    val status: CaseStatus
)

enum class CaseStatus { ACTIVE, PENDING, CLOSED }

data class Client(
    val id: String,
    val name: String,
    val phone: String,
    val nationalId: String? = null,
    val notes: String = ""
)

data class Hearing(
    val id: String,
    val caseId: String,
    val caseTitle: String,
    val court: String,
    val date: String,
    val time: String,
    val notes: String = ""
)

data class LawBook(
    val id: String,
    val title: String,
    val category: String,
    val sourceNote: String
)

data class LegalDocument(
    val id: String,
    val title: String,
    val type: String,
    val caseTitle: String? = null,
    val updatedAt: String
)
