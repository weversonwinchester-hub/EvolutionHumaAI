package com.example.core.progressionengine.policy

import com.example.core.progressionengine.model.ProgressionTimePolicy

/**
 * PERFORMAI PROGRESSION POLICY REGISTRY
 *
 * Registrador central e imutável de políticas de tempo e requisitos longitudinais
 * para progressão entre classes.
 *
 * Princípio: A progressão não é linear e impede evolução instantânea ou "speedrun".
 * Valores científicos não homologados possuem status de metodologia PENDING_VALIDATION.
 */
object ProgressionPolicyRegistry {

    private val policies: Map<String, ProgressionTimePolicy> = mapOf(
        "CLASS_01" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-01-V1",
            classId = "CLASS_01",
            minimumTimeInClassDays = 7,
            minimumEvidenceSpanDays = 7,
            minimumObservationCount = 5,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_02" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-02-V1",
            classId = "CLASS_02",
            minimumTimeInClassDays = 14,
            minimumEvidenceSpanDays = 14,
            minimumObservationCount = 8,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_03" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-03-V1",
            classId = "CLASS_03",
            minimumTimeInClassDays = 21,
            minimumEvidenceSpanDays = 21,
            minimumObservationCount = 10,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_04" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-04-V1",
            classId = "CLASS_04",
            minimumTimeInClassDays = 28,
            minimumEvidenceSpanDays = 28,
            minimumObservationCount = 12,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_05" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-05-V1",
            classId = "CLASS_05",
            minimumTimeInClassDays = 35,
            minimumEvidenceSpanDays = 35,
            minimumObservationCount = 15,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_06" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-06-V1",
            classId = "CLASS_06",
            minimumTimeInClassDays = 42,
            minimumEvidenceSpanDays = 42,
            minimumObservationCount = 18,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_07" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-07-V1",
            classId = "CLASS_07",
            minimumTimeInClassDays = 49,
            minimumEvidenceSpanDays = 49,
            minimumObservationCount = 20,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_08" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-08-V1",
            classId = "CLASS_08",
            minimumTimeInClassDays = 60,
            minimumEvidenceSpanDays = 60,
            minimumObservationCount = 24,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_09" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-09-V1",
            classId = "CLASS_09",
            minimumTimeInClassDays = 60,
            minimumEvidenceSpanDays = 60,
            minimumObservationCount = 24,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_10" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-10-V1",
            classId = "CLASS_10",
            minimumTimeInClassDays = 75,
            minimumEvidenceSpanDays = 75,
            minimumObservationCount = 30,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_11" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-11-V1",
            classId = "CLASS_11",
            minimumTimeInClassDays = 75,
            minimumEvidenceSpanDays = 75,
            minimumObservationCount = 30,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_12" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-12-V1",
            classId = "CLASS_12",
            minimumTimeInClassDays = 90,
            minimumEvidenceSpanDays = 90,
            minimumObservationCount = 36,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_13" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-13-V1",
            classId = "CLASS_13",
            minimumTimeInClassDays = 90,
            minimumEvidenceSpanDays = 90,
            minimumObservationCount = 36,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_14" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-14-V1",
            classId = "CLASS_14",
            minimumTimeInClassDays = 120,
            minimumEvidenceSpanDays = 120,
            minimumObservationCount = 45,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_15" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-15-V1",
            classId = "CLASS_15",
            minimumTimeInClassDays = 120,
            minimumEvidenceSpanDays = 120,
            minimumObservationCount = 45,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_16" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-16-V1",
            classId = "CLASS_16",
            minimumTimeInClassDays = 150,
            minimumEvidenceSpanDays = 150,
            minimumObservationCount = 50,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_17" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-17-V1",
            classId = "CLASS_17",
            minimumTimeInClassDays = 150,
            minimumEvidenceSpanDays = 150,
            minimumObservationCount = 50,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_18" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-18-V1",
            classId = "CLASS_18",
            minimumTimeInClassDays = 180,
            minimumEvidenceSpanDays = 180,
            minimumObservationCount = 60,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_19" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-19-V1",
            classId = "CLASS_19",
            minimumTimeInClassDays = 180,
            minimumEvidenceSpanDays = 180,
            minimumObservationCount = 60,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_20" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-20-V1",
            classId = "CLASS_20",
            minimumTimeInClassDays = 210,
            minimumEvidenceSpanDays = 210,
            minimumObservationCount = 75,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_21" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-21-V1",
            classId = "CLASS_21",
            minimumTimeInClassDays = 240,
            minimumEvidenceSpanDays = 240,
            minimumObservationCount = 90,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        ),
        "CLASS_22" to ProgressionTimePolicy(
            policyId = "TIME-POL-CLASS-22-V1",
            classId = "CLASS_22",
            minimumTimeInClassDays = 365,
            minimumEvidenceSpanDays = 365,
            minimumObservationCount = 120,
            methodologyVersion = "1.0.0",
            status = "ACTIVE"
        )
    )

    fun getPolicyForClass(classId: String): ProgressionTimePolicy? {
        return policies[classId] ?: policies.entries.find { it.key.startsWith(classId) || classId.startsWith(it.key) }?.value
    }

    fun getAllPolicies(): List<ProgressionTimePolicy> {
        return policies.values.toList()
    }
}
