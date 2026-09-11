package com.nexvary.shurayh.core

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

interface LegalPersistence {
    fun load(): PersistedLegalState?
    fun save(state: PersistedLegalState)
}

data class PersistedLegalState(
    val cases: List<LegalCase>,
    val clients: List<Client>,
    val hearings: List<Hearing>,
    val documents: List<LegalDocument>
)

object NoOpLegalPersistence : LegalPersistence {
    override fun load(): PersistedLegalState? = null
    override fun save(state: PersistedLegalState) = Unit
}

class SharedPreferencesLegalPersistence(context: Context) : LegalPersistence {
    private val prefs = context.getSharedPreferences("shurayh_legal_store", Context.MODE_PRIVATE)

    override fun load(): PersistedLegalState? {
        val raw = prefs.getString(KEY_STATE, null) ?: return null
        return runCatching {
            val root = JSONObject(raw)
            PersistedLegalState(
                cases = root.getJSONArray("cases").mapObjects { o ->
                    LegalCase(
                        id = o.getString("id"),
                        title = o.getString("title"),
                        court = o.getString("court"),
                        caseNumber = o.getString("caseNumber"),
                        clientName = o.getString("clientName"),
                        nextSession = o.optString("nextSession"),
                        status = CaseStatus.valueOf(o.optString("status", CaseStatus.ACTIVE.name))
                    )
                },
                clients = root.getJSONArray("clients").mapObjects { o ->
                    Client(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        phone = o.optString("phone"),
                        nationalId = o.optString("nationalId").takeIf { it.isNotBlank() },
                        notes = o.optString("notes")
                    )
                },
                hearings = root.getJSONArray("hearings").mapObjects { o ->
                    Hearing(
                        id = o.getString("id"),
                        caseId = o.getString("caseId"),
                        caseTitle = o.getString("caseTitle"),
                        court = o.optString("court"),
                        date = o.optString("date"),
                        time = o.optString("time"),
                        notes = o.optString("notes")
                    )
                },
                documents = root.getJSONArray("documents").mapObjects { o ->
                    LegalDocument(
                        id = o.getString("id"),
                        title = o.getString("title"),
                        type = o.optString("type"),
                        caseTitle = o.optString("caseTitle").takeIf { it.isNotBlank() },
                        updatedAt = o.optString("updatedAt")
                    )
                }
            )
        }.getOrNull()
    }

    override fun save(state: PersistedLegalState) {
        val root = JSONObject().apply {
            put("schema", 1)
            put("cases", JSONArray().apply {
                state.cases.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id); put("title", c.title); put("court", c.court)
                        put("caseNumber", c.caseNumber); put("clientName", c.clientName)
                        put("nextSession", c.nextSession); put("status", c.status.name)
                    })
                }
            })
            put("clients", JSONArray().apply {
                state.clients.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id); put("name", c.name); put("phone", c.phone)
                        put("nationalId", c.nationalId ?: ""); put("notes", c.notes)
                    })
                }
            })
            put("hearings", JSONArray().apply {
                state.hearings.forEach { h ->
                    put(JSONObject().apply {
                        put("id", h.id); put("caseId", h.caseId); put("caseTitle", h.caseTitle)
                        put("court", h.court); put("date", h.date); put("time", h.time); put("notes", h.notes)
                    })
                }
            })
            put("documents", JSONArray().apply {
                state.documents.forEach { d ->
                    put(JSONObject().apply {
                        put("id", d.id); put("title", d.title); put("type", d.type)
                        put("caseTitle", d.caseTitle ?: ""); put("updatedAt", d.updatedAt)
                    })
                }
            })
        }
        prefs.edit().putString(KEY_STATE, root.toString()).apply()
    }

    private inline fun <T> JSONArray.mapObjects(mapper: (JSONObject) -> T): List<T> =
        buildList { for (i in 0 until length()) add(mapper(getJSONObject(i))) }

    companion object { private const val KEY_STATE = "legal_state_v1" }
}
