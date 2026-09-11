package com.nexvary.shurayh.core

import org.json.JSONArray
import org.json.JSONObject

object WorkspaceBackupSerializer {
    fun serialize(state: ShurayhUiState): String = JSONObject().apply {
        put("format", "SHURAYH-WORKSPACE")
        put("version", 1)
        put("cases", JSONArray().apply {
            state.cases.forEach { c -> put(JSONObject().apply {
                put("id", c.id); put("title", c.title); put("court", c.court)
                put("caseNumber", c.caseNumber); put("clientName", c.clientName)
                put("nextSession", c.nextSession); put("status", c.status.name)
            }) }
        })
        put("clients", JSONArray().apply {
            state.clients.forEach { c -> put(JSONObject().apply {
                put("id", c.id); put("name", c.name); put("phone", c.phone)
                put("nationalId", c.nationalId ?: ""); put("notes", c.notes)
            }) }
        })
        put("hearings", JSONArray().apply {
            state.hearings.forEach { h -> put(JSONObject().apply {
                put("id", h.id); put("caseId", h.caseId); put("caseTitle", h.caseTitle)
                put("court", h.court); put("date", h.date); put("time", h.time); put("notes", h.notes)
            }) }
        })
        put("documents", JSONArray().apply {
            state.documents.forEach { d -> put(JSONObject().apply {
                put("id", d.id); put("title", d.title); put("type", d.type)
                put("caseTitle", d.caseTitle ?: ""); put("updatedAt", d.updatedAt)
            }) }
        })
    }.toString()

    fun deserialize(payload: String): Result<PersistedLegalState> = runCatching {
        val root = JSONObject(payload)
        require(root.optString("format") == "SHURAYH-WORKSPACE") { "Invalid SHURAYH backup format" }
        require(root.optInt("version") == 1) { "Unsupported SHURAYH backup version" }
        PersistedLegalState(
            cases = root.getJSONArray("cases").mapObjects { o ->
                LegalCase(
                    id = o.getString("id"), title = o.getString("title"), court = o.getString("court"),
                    caseNumber = o.getString("caseNumber"), clientName = o.getString("clientName"),
                    nextSession = o.optString("nextSession"),
                    status = runCatching { CaseStatus.valueOf(o.optString("status", CaseStatus.ACTIVE.name)) }.getOrDefault(CaseStatus.ACTIVE)
                )
            },
            clients = root.getJSONArray("clients").mapObjects { o ->
                Client(o.getString("id"), o.getString("name"), o.optString("phone"), o.optString("nationalId").takeIf(String::isNotBlank), o.optString("notes"))
            },
            hearings = root.getJSONArray("hearings").mapObjects { o ->
                Hearing(o.getString("id"), o.getString("caseId"), o.getString("caseTitle"), o.optString("court"), o.optString("date"), o.optString("time"), o.optString("notes"))
            },
            documents = root.getJSONArray("documents").mapObjects { o ->
                LegalDocument(o.getString("id"), o.getString("title"), o.optString("type"), o.optString("caseTitle").takeIf(String::isNotBlank), o.optString("updatedAt"))
            }
        )
    }

    fun encryptedEnvelopeToJson(envelope: EncryptedBackupEnvelope): String = JSONObject().apply {
        put("format", "SHURAYH-ENCRYPTED-BACKUP")
        put("version", envelope.version)
        put("iterations", envelope.iterations)
        put("salt", envelope.saltBase64)
        put("iv", envelope.ivBase64)
        put("ciphertext", envelope.ciphertextBase64)
    }.toString()

    fun encryptedEnvelopeFromJson(json: String): Result<EncryptedBackupEnvelope> = runCatching {
        val root = JSONObject(json)
        require(root.optString("format") == "SHURAYH-ENCRYPTED-BACKUP") { "Invalid encrypted backup format" }
        EncryptedBackupEnvelope(
            version = root.getInt("version"),
            iterations = root.getInt("iterations"),
            saltBase64 = root.getString("salt"),
            ivBase64 = root.getString("iv"),
            ciphertextBase64 = root.getString("ciphertext")
        )
    }

    private inline fun <T> JSONArray.mapObjects(mapper: (JSONObject) -> T): List<T> =
        buildList { for (i in 0 until length()) add(mapper(getJSONObject(i))) }
}
