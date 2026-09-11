package com.nexvary.shurayh.core

import java.text.Normalizer

data class LegalSourceMeta(
    val title: String,
    val jurisdiction: String = "مصر",
    val sourceUrl: String? = null,
    val versionLabel: String,
    val lastVerified: String,
    val isOfficialSource: Boolean
)

data class LawArticle(
    val id: String,
    val lawId: String,
    val articleNumber: String,
    val heading: String,
    val text: String,
    val source: LegalSourceMeta,
    val tags: List<String> = emptyList()
)

data class LegalSearchHit(
    val article: LawArticle,
    val score: Int,
    val warning: String? = null
)

object LegalContentRepository {
    // Seed excerpts are intentionally descriptive/demo content, not a substitute for official law text.
    val articles: List<LawArticle> = listOf(
        LawArticle(
            id = "civil-demo-1",
            lawId = "civil",
            articleNumber = "مرجع تجريبي",
            heading = "القانون المدني — العقود",
            text = "مرجع تجريبي لتصنيف موضوعات العقود والالتزامات. يجب إدخال النص الرسمي الموثق قبل الاستخدام المهني.",
            source = LegalSourceMeta("القانون المدني المصري", versionLabel = "seed-demo", lastVerified = "غير موثق", isOfficialSource = false),
            tags = listOf("مدني", "عقود", "التزامات")
        ),
        LawArticle(
            id = "procedure-demo-1",
            lawId = "procedure",
            articleNumber = "مرجع تجريبي",
            heading = "قانون المرافعات — الإجراءات",
            text = "مرجع تجريبي لفهرسة إجراءات التقاضي والمواعيد. لا يمثل نص مادة قانونية رسمية.",
            source = LegalSourceMeta("قانون المرافعات", versionLabel = "seed-demo", lastVerified = "غير موثق", isOfficialSource = false),
            tags = listOf("مرافعات", "إجراءات", "مواعيد")
        )
    )
}

object LegalSearchEngine {
    fun search(query: String, corpus: List<LawArticle> = LegalContentRepository.articles): List<LegalSearchHit> {
        val tokens = normalize(query).split(' ').filter { it.length > 1 }
        if (tokens.isEmpty()) return emptyList()
        return corpus.mapNotNull { article ->
            val haystack = normalize(listOf(article.heading, article.text, article.articleNumber, article.tags.joinToString(" ")).joinToString(" "))
            val score = tokens.sumOf { token ->
                when {
                    haystack.contains(" $token ") -> 4
                    haystack.contains(token) -> 2
                    else -> 0
                }
            }
            if (score == 0) null else LegalSearchHit(
                article = article,
                score = score,
                warning = if (!article.source.isOfficialSource) "مصدر غير رسمي/غير موثق — راجع النص الرسمي" else null
            )
        }.sortedByDescending { it.score }
    }

    internal fun normalize(input: String): String {
        val noMarks = Normalizer.normalize(input, Normalizer.Form.NFD).replace("\\p{M}+".toRegex(), "")
        return noMarks
            .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
            .replace('ى', 'ي').replace('ة', 'ه')
            .replace("[^\\p{L}\\p{N}]+".toRegex(), " ")
            .trim().lowercase()
    }
}
