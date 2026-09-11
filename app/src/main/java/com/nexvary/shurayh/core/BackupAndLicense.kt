package com.nexvary.shurayh.core

import java.security.MessageDigest

object BackupCodec {
    data class Envelope(val payload: String, val checksum: String)

    fun wrap(payload: String): Envelope = Envelope(payload, sha256(payload))

    fun verify(envelope: Envelope): Boolean = sha256(envelope.payload) == envelope.checksum

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }
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
