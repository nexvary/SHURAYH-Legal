package com.nexvary.shurayh.core

import java.security.MessageDigest

object LocalLegalTextTools {
    data class SummaryResult(
        val summary: String,
        val sourceLength: Int,
        val warning: String = "تلخيص آلي محلي أولي — يجب على المحامي مراجعة الأصل قبل الاستخدام."
    )

    fun summarize(text: String, maxSentences: Int = 5): SummaryResult {
        val clean = text.trim().replace("\\s+".toRegex(), " ")
        if (clean.isBlank()) return SummaryResult("", 0)
        val sentences = clean.split(Regex("(?<=[.!؟])\\s+|\\n+")).filter { it.isNotBlank() }
        if (sentences.size <= maxSentences) return SummaryResult(clean, clean.length)

        val words = clean.lowercase().split(Regex("[^\\p{L}\\p{N}]+" )).filter { it.length > 2 }
        val freq = words.groupingBy { it }.eachCount()
        val ranked = sentences.mapIndexed { index, sentence ->
            val score = sentence.lowercase().split(Regex("[^\\p{L}\\p{N}]+")).sumOf { freq[it] ?: 0 }
            Triple(index, sentence.trim(), score)
        }.sortedByDescending { it.third }.take(maxSentences).sortedBy { it.first }
        return SummaryResult(ranked.joinToString(" ") { it.second }, clean.length)
    }

    fun buildReviewChecklist(text: String): List<String> {
        val out = mutableListOf<String>()
        if (!text.contains("تاريخ") && !text.contains("/")) out += "راجع التاريخ أو تاريخ الواقعة/المستند."
        if (!text.contains("محكمة")) out += "تحقق من تحديد المحكمة أو الجهة المختصة عند الحاجة."
        if (!text.contains("طلب") && !text.contains("يلتمس")) out += "راجع صياغة الطلبات الختامية بوضوح."
        if (text.length < 250) out += "النص قصير؛ تأكد من اكتمال الوقائع والأسانيد والطلبات."
        out += "طابق كل استناد قانوني مع النص الرسمي وآخر تعديل منشور."
        out += "راجع الأسماء والأرقام وأرقام القضايا والمستندات قبل الإيداع."
        return out
    }

    fun fingerprint(text: String): String = MessageDigest.getInstance("SHA-256")
        .digest(text.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

interface OcrAdapter {
    val engineName: String
    fun isAvailable(): Boolean
    suspend fun recognize(imageBytes: ByteArray): OcrResult
}

data class OcrResult(
    val text: String,
    val confidence: Float? = null,
    val warning: String? = null
)

object UnconfiguredOcrAdapter : OcrAdapter {
    override val engineName: String = "Not configured"
    override fun isAvailable(): Boolean = false
    override suspend fun recognize(imageBytes: ByteArray): OcrResult = OcrResult(
        text = "",
        warning = "لم يتم دمج محرك OCR بعد. لا يتم إرسال الصورة إلى خدمة خارجية تلقائيًا."
    )
}
