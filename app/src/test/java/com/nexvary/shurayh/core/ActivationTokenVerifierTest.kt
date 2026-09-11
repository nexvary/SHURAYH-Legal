package com.nexvary.shurayh.core

import java.security.KeyPairGenerator
import java.security.Signature
import java.util.Base64
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivationTokenVerifierTest {
    @Test fun valid_server_signature_is_accepted_and_tampering_is_rejected() {
        val pair = KeyPairGenerator.getInstance("EC").apply { initialize(256) }.generateKeyPair()
        val publicKeyBase64 = Base64.getEncoder().encodeToString(pair.public.encoded)
        val now = 1_800_000_000L
        val unsigned = SignedActivation("install-test", "SHU-AB12-CD34-EF56", now - 60, "")
        val sig = Signature.getInstance("SHA256withECDSA").apply {
            initSign(pair.private)
            update(unsigned.canonicalPayload())
        }.sign()
        val activation = unsigned.copy(signatureBase64 = Base64.getEncoder().encodeToString(sig))
        val verifier = ActivationTokenVerifier(publicKeyBase64)

        assertTrue(
            verifier.verify(activation, "install-test", "SHU-AB12-CD34-EF56", now) is ActivationVerificationResult.Valid
        )
        assertTrue(
            verifier.verify(activation.copy(serial = "SHU-XXXX-XXXX-XXXX"), "install-test", "SHU-XXXX-XXXX-XXXX", now) is ActivationVerificationResult.Invalid
        )
    }

    @Test fun activation_for_another_installation_is_rejected() {
        val pair = KeyPairGenerator.getInstance("EC").apply { initialize(256) }.generateKeyPair()
        val publicKeyBase64 = Base64.getEncoder().encodeToString(pair.public.encoded)
        val verifier = ActivationTokenVerifier(publicKeyBase64)
        val unsigned = SignedActivation("install-a", "SHU-AB12-CD34-EF56", 1000, "invalid")
        assertTrue(
            verifier.verify(unsigned, "install-b", "SHU-AB12-CD34-EF56", 1100) is ActivationVerificationResult.Invalid
        )
    }
}
