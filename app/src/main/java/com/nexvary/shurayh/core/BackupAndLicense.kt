package com.nexvary.shurayh.core

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupCodec {
    data class Envelope(val payload: String, val checksum: String)

    fun wrap(payload: String): Envelope = Envelope(payload, sha256(payload))

    fun verify(envelope: Envelope): Boolean = sha256(envelope.payload) == envelope.checksum

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

data class EncryptedBackupEnvelope(
    val version: Int = 1,
    val iterations: Int,
    val saltBase64: String,
    val ivBase64: String,
    val ciphertextBase64: String
)

object EncryptedBackupCodec {
    private const val DEFAULT_ITERATIONS = 210_000
    private const val KEY_BITS = 256
    private const val GCM_TAG_BITS = 128
    private val random = SecureRandom()

    fun encrypt(payload: String, password: CharArray, iterations: Int = DEFAULT_ITERATIONS): EncryptedBackupEnvelope {
        require(password.size >= 10) { "Backup password must contain at least 10 characters" }
        require(iterations >= 100_000) { "PBKDF2 iteration count is too low" }
        val salt = ByteArray(16).also(random::nextBytes)
        val key = deriveKey(password, salt, iterations)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(payload.toByteArray(Charsets.UTF_8))
        return EncryptedBackupEnvelope(
            iterations = iterations,
            saltBase64 = Base64.getEncoder().encodeToString(salt),
            ivBase64 = Base64.getEncoder().encodeToString(cipher.iv),
            ciphertextBase64 = Base64.getEncoder().encodeToString(encrypted)
        )
    }

    fun decrypt(envelope: EncryptedBackupEnvelope, password: CharArray): Result<String> = runCatching {
        require(envelope.version == 1) { "Unsupported backup version" }
        require(envelope.iterations >= 100_000) { "Invalid backup KDF parameters" }
        val salt = Base64.getDecoder().decode(envelope.saltBase64)
        val iv = Base64.getDecoder().decode(envelope.ivBase64)
        val ciphertext = Base64.getDecoder().decode(envelope.ciphertextBase64)
        require(salt.size >= 16 && iv.size in 12..16 && ciphertext.isNotEmpty()) { "Invalid encrypted backup envelope" }
        val key = deriveKey(password, salt, envelope.iterations)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
    }

    private fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, iterations, KEY_BITS)
        return try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            SecretKeySpec(bytes, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}

data class LicenseIdentity(
    val installationId: String,
    val serial: String,
    val confirmationCode: String
)

sealed interface LicenseCheck {
    data object ValidFormat : LicenseCheck
    data class Invalid(val reason: String) : LicenseCheck
}

object TwoStepSerialValidator {
    // Local structural validation only. Production activation should be verified against a protected company service.
    fun validate(identity: LicenseIdentity): LicenseCheck {
        if (identity.installationId.length !in 8..64) return LicenseCheck.Invalid("معرف التثبيت غير صالح")
        if (!identity.serial.matches(Regex("SHU-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}"))) {
            return LicenseCheck.Invalid("صيغة الرقم التسلسلي غير صحيحة")
        }
        if (!identity.confirmationCode.matches(Regex("[0-9]{6}"))) {
            return LicenseCheck.Invalid("رمز التأكيد يجب أن يتكون من 6 أرقام")
        }
        return LicenseCheck.ValidFormat
    }
}
