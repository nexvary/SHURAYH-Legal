package com.nexvary.shurayh.core

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class StoredAttachment(
    val id: String,
    val encryptedFileName: String,
    val originalName: String,
    val mimeType: String,
    val originalSha256: String,
    val originalSize: Long
)

class EncryptedAttachmentStore(context: Context) {
    private val directory = File(context.filesDir, "legal_attachments_v1").apply { mkdirs() }

    fun store(originalName: String, mimeType: String, bytes: ByteArray): StoredAttachment {
        require(bytes.isNotEmpty()) { "Attachment is empty" }
        require(bytes.size <= MAX_ATTACHMENT_BYTES) { "Attachment exceeds local safety limit" }
        val id = UUID.randomUUID().toString()
        val fileName = "$id.enc"
        val file = File(directory, fileName)
        file.writeBytes(encrypt(bytes))
        return StoredAttachment(
            id = id,
            encryptedFileName = fileName,
            originalName = originalName.take(180),
            mimeType = mimeType.take(120),
            originalSha256 = sha256(bytes),
            originalSize = bytes.size.toLong()
        )
    }

    fun read(attachment: StoredAttachment): Result<ByteArray> = runCatching {
        validateFileName(attachment.encryptedFileName)
        val encrypted = File(directory, attachment.encryptedFileName).readBytes()
        val plain = decrypt(encrypted)
        check(plain.size.toLong() == attachment.originalSize) { "Attachment size integrity check failed" }
        check(sha256(plain) == attachment.originalSha256) { "Attachment SHA-256 integrity check failed" }
        plain
    }

    fun delete(attachment: StoredAttachment): Boolean {
        validateFileName(attachment.encryptedFileName)
        val file = File(directory, attachment.encryptedFileName)
        if (!file.exists()) return true
        return file.delete()
    }

    private fun validateFileName(name: String) {
        require(name.matches(Regex("[0-9a-fA-F-]{36}\\.enc"))) { "Unsafe attachment filename" }
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val ciphertext = cipher.doFinal(plain)
        return byteArrayOf(cipher.iv.size.toByte()) + cipher.iv + ciphertext
    }

    private fun decrypt(envelope: ByteArray): ByteArray {
        require(envelope.isNotEmpty()) { "Encrypted attachment is empty" }
        val ivLength = envelope[0].toInt() and 0xFF
        require(ivLength in 12..16 && envelope.size > 1 + ivLength) { "Invalid encrypted attachment" }
        val iv = envelope.copyOfRange(1, 1 + ivLength)
        val ciphertext = envelope.copyOfRange(1 + ivLength, envelope.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            generateKey()
        }
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }

    companion object {
        private const val KEY_ALIAS = "shurayh_attachment_aes_v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val MAX_ATTACHMENT_BYTES = 50 * 1024 * 1024
    }
}
