package com.nexvary.shurayh.core

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
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

/**
 * Stores the legal workspace in app-private SharedPreferences, while encrypting the complete
 * serialized payload with a non-exportable AES key held by Android Keystore.
 *
 * Legacy plaintext v1 payloads remain readable and are migrated to encrypted v2 on next load.
 */
class SharedPreferencesLegalPersistence(context: Context) : LegalPersistence {
    private val prefs = context.getSharedPreferences("shurayh_legal_store", Context.MODE_PRIVATE)

    override fun load(): PersistedLegalState? {
        val stored = prefs.getString(KEY_STATE, null) ?: return null
        return runCatching {
            val wasLegacyPlaintext = !stored.startsWith(ENCRYPTED_PREFIX)
            val raw = if (wasLegacyPlaintext) stored else decrypt(stored.removePrefix(ENCRYPTED_PREFIX))
            val state = decode(JSONObject(raw))
            if (wasLegacyPlaintext) save(state)
            state
        }.getOrNull()
    }

    override fun save(state: PersistedLegalState) {
        val raw = encode(state).toString()
        val encrypted = ENCRYPTED_PREFIX + encrypt(raw)
        prefs.edit().putString(KEY_STATE, encrypted).apply()
    }

    private fun encode(state: PersistedLegalState): JSONObject = JSONObject().apply {
        put("schema", 2)
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

    private fun decode(root: JSONObject): PersistedLegalState = PersistedLegalState(
        cases = root.getJSONArray("cases").mapObjects { o ->
            LegalCase(
                id = o.getString("id"),
                title = o.getString("title"),
                court = o.getString("court"),
                caseNumber = o.getString("caseNumber"),
                clientName = o.getString("clientName"),
                nextSession = o.optString("nextSession"),
                status = runCatching { CaseStatus.valueOf(o.optString("status", CaseStatus.ACTIVE.name)) }
                    .getOrDefault(CaseStatus.ACTIVE)
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

    private fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val envelope = ByteArray(1 + cipher.iv.size + ciphertext.size)
        envelope[0] = cipher.iv.size.toByte()
        cipher.iv.copyInto(envelope, destinationOffset = 1)
        ciphertext.copyInto(envelope, destinationOffset = 1 + cipher.iv.size)
        return Base64.encodeToString(envelope, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val envelope = Base64.decode(encoded, Base64.NO_WRAP)
        require(envelope.isNotEmpty()) { "Encrypted legal store is empty" }
        val ivLength = envelope[0].toInt() and 0xFF
        require(ivLength in 12..16 && envelope.size > 1 + ivLength) { "Invalid encrypted legal store" }
        val iv = envelope.copyOfRange(1, 1 + ivLength)
        val ciphertext = envelope.copyOfRange(1 + ivLength, envelope.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            generateKey()
        }
    }

    private inline fun <T> JSONArray.mapObjects(mapper: (JSONObject) -> T): List<T> =
        buildList { for (i in 0 until length()) add(mapper(getJSONObject(i))) }

    companion object {
        private const val KEY_STATE = "legal_state_v1"
        private const val KEY_ALIAS = "shurayh_legal_store_aes_v2"
        private const val ENCRYPTED_PREFIX = "v2:"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
