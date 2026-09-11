package com.nexvary.shurayh.core

enum class LegalAuthorityLevel { OFFICIAL, GOVERNMENT_PORTAL, LICENSED_PROVIDER, UNVERIFIED }

data class LegalSourceRegistryEntry(
    val id: String,
    val nameAr: String,
    val baseUrl: String,
    val authority: LegalAuthorityLevel,
    val supportsLegislation: Boolean,
    val supportsCaseLaw: Boolean,
    val accessNote: String
)

object OfficialLegalSourceRegistry {
    /**
     * Sources are registry metadata only. SHURAYH must respect each source's access terms,
     * authentication/subscription requirements and redistribution rights.
     */
    val entries = listOf(
        LegalSourceRegistryEntry(
            id = "egypt-court-of-cassation",
            nameAr = "محكمة النقض المصرية",
            baseUrl = "https://cc.gov.eg/",
            authority = LegalAuthorityLevel.OFFICIAL,
            supportsLegislation = true,
            supportsCaseLaw = true,
            accessNote = "مصدر رسمي؛ بعض المحتوى الكامل قد يتطلب حسابًا/اشتراكًا. لا يعاد توزيع محتوى مقيد دون ترخيص."
        )
    )

    fun trustedForProfessionalDisplay(entry: LegalSourceRegistryEntry): Boolean =
        entry.authority == LegalAuthorityLevel.OFFICIAL || entry.authority == LegalAuthorityLevel.GOVERNMENT_PORTAL
}
