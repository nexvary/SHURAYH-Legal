package com.nexvary.shurayh.core

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object HearingDateTimeParser {
    private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * Release-safe format is intentionally strict: yyyy-MM-dd + HH:mm.
     * We do not guess ambiguous human-entered dates for court reminders.
     */
    fun parse(date: String, time: String): Result<LocalDateTime> = runCatching {
        val parsedDate = LocalDate.parse(date.trim(), dateFormat)
        val parsedTime = LocalTime.parse(time.trim(), timeFormat)
        LocalDateTime.of(parsedDate, parsedTime)
    }.recoverCatching { error ->
        if (error is DateTimeParseException) {
            throw IllegalArgumentException("صيغة الموعد المطلوبة: YYYY-MM-DD والوقت HH:mm", error)
        }
        throw error
    }

    fun isValid(date: String, time: String): Boolean = parse(date, time).isSuccess
}
