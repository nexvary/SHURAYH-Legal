package com.nexvary.shurayh.core

import android.content.Context
import java.time.LocalDateTime

interface HearingReminderCoordinator {
    fun schedule(hearing: Hearing): Boolean
    fun cancel(hearingId: String)
}

object NoOpHearingReminderCoordinator : HearingReminderCoordinator {
    override fun schedule(hearing: Hearing): Boolean = false
    override fun cancel(hearingId: String) = Unit
}

class AndroidHearingReminderCoordinator(
    private val context: Context,
    private val now: () -> LocalDateTime = LocalDateTime::now
) : HearingReminderCoordinator {
    override fun schedule(hearing: Hearing): Boolean {
        val hearingAt = HearingDateTimeParser.parse(hearing.date, hearing.time).getOrNull() ?: return false
        val current = now()
        val reminderAt = ReminderPolicy.chooseReminderTime(hearingAt, current) ?: return false
        return HearingReminderScheduler.schedule(
            context = context,
            hearingId = hearing.id,
            caseTitle = hearing.caseTitle,
            court = hearing.court,
            reminderAt = reminderAt
        )
    }

    override fun cancel(hearingId: String) {
        HearingReminderScheduler.cancel(context, hearingId)
    }
}

object ReminderPolicy {
    /**
     * Prefer a 24-hour warning; if the hearing was entered less than 24 hours ahead,
     * fall back to 2 hours, then 30 minutes. Never schedule after the hearing itself.
     */
    fun chooseReminderTime(hearingAt: LocalDateTime, current: LocalDateTime): LocalDateTime? {
        if (!hearingAt.isAfter(current)) return null
        val candidates = listOf(
            hearingAt.minusHours(24),
            hearingAt.minusHours(2),
            hearingAt.minusMinutes(30)
        )
        return candidates.firstOrNull { it.isAfter(current) }
    }
}
