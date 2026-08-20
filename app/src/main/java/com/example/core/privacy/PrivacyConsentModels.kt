package com.example.core.privacy

import java.util.UUID

/**
 * EVOLUTION HUMAN AI — PRIVACY & CONSENT SYSTEM V1
 */

enum class ProcessingScope {
    LOCAL_ONLY,
    HYBRID_SYNC,
    REMOTE_ANALYSIS
}

enum class ConsentStatus {
    GRANTED,
    DENIED,
    REVOKED,
    PENDING
}

data class PrivacyConsent(
    val consentId: String = UUID.randomUUID().toString(),
    val userId: String,
    val cameraProcessing: ConsentStatus = ConsentStatus.GRANTED,
    val processingScope: ProcessingScope = ProcessingScope.LOCAL_ONLY,
    val allowAnonymousAnalytics: Boolean = false,
    val allowRawImageRetention: Boolean = false, // Strictly false by default
    val agreedAtEpoch: Long = System.currentTimeMillis(),
    val policyVersion: String = "1.0.0-privacy-v1"
)

object PrivacyPolicyManager {
    fun createDefaultLocalConsent(userId: String): PrivacyConsent {
        return PrivacyConsent(
            userId = userId,
            cameraProcessing = ConsentStatus.GRANTED,
            processingScope = ProcessingScope.LOCAL_ONLY,
            allowAnonymousAnalytics = false,
            allowRawImageRetention = false
        )
    }

    fun canProcessCameraLocally(consent: PrivacyConsent): Boolean {
        return consent.cameraProcessing == ConsentStatus.GRANTED
    }

    fun canSendTelemetryRemotely(consent: PrivacyConsent): Boolean {
        return consent.processingScope != ProcessingScope.LOCAL_ONLY && consent.allowAnonymousAnalytics
    }
}
