package com.nexvary.shurayh.core

import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

data class SignedActivation(
    val installationId: String,
    val serial: String,
    val issuedAtEpochSeconds: Long,
    val signatureBase64: String
) {
    fun canonicalPayload(): ByteArray =
        "$installationId|$serial|$issuedAtEpochSeconds".toByteArray(Charsets.UTF_8)
}

sealed interface ActivationVerificationResult {
    data object Valid : ActivationVerificationResult
    data class Invalid(val reason: String) : ActivationVerificationResult
}

class ActivationTokenVerifier(publicKeyX509Base64: String) {
    private val publicKey: PublicKey = KeyFactory.getInstance("EC").generatePublic(
        X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyX509Base64))
    )

    fun verify(
        activation: SignedActivation,
        expectedInstallationId: String,
        expectedSerial: String,
        nowEpochSeconds: Long,
        maxAgeSeconds: Long = 7L * 24L * 60L * 60L
    ): ActivationVerificationResult {
        if (activation.installationId != expectedInstallationId) {
            return ActivationVerificationResult.Invalid("التفعيل لا يخص هذا التثبيت")
        }
        if (activation.serial != expectedSerial) {
            return ActivationVerificationResult.Invalid("الرقم التسلسلي لا يطابق التفعيل")
        }
        if (activation.issuedAtEpochSeconds > nowEpochSeconds + 300) {
            return ActivationVerificationResult.Invalid("وقت إصدار التفعيل غير صالح")
        }
        if (nowEpochSeconds - activation.issuedAtEpochSeconds > maxAgeSeconds) {
            return ActivationVerificationResult.Invalid("استجابة التفعيل قديمة")
        }
        val signatureBytes = runCatching { Base64.getDecoder().decode(activation.signatureBase64) }
            .getOrElse { return ActivationVerificationResult.Invalid("توقيع التفعيل غير صالح") }
        val signature = Signature.getInstance("SHA256withECDSA").apply {
            initVerify(publicKey)
            update(activation.canonicalPayload())
        }
        return if (signature.verify(signatureBytes)) {
            ActivationVerificationResult.Valid
        } else {
            ActivationVerificationResult.Invalid("تعذر التحقق من توقيع خادم التفعيل")
        }
    }
}

/**
 * Production activation transport boundary. No endpoint or private signing secret is embedded here.
 */
interface ActivationService {
    suspend fun activate(installationId: String, serial: String, confirmationCode: String): Result<SignedActivation>
}

object UnconfiguredActivationService : ActivationService {
    override suspend fun activate(installationId: String, serial: String, confirmationCode: String): Result<SignedActivation> =
        Result.failure(IllegalStateException("Production activation service is not configured"))
}
