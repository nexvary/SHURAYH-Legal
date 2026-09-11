package com.nexvary.shurayh.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HearingDateTimeParserTest {
    @Test fun strict_iso_date_and_24h_time_parse() {
        val parsed = HearingDateTimeParser.parse("2026-09-22", "10:30")
        assertTrue(parsed.isSuccess)
        assertEquals(22, parsed.getOrThrow().dayOfMonth)
        assertEquals(10, parsed.getOrThrow().hour)
    }

    @Test fun ambiguous_or_invalid_dates_are_rejected() {
        assertFalse(HearingDateTimeParser.isValid("22/09/2026", "10:30"))
        assertFalse(HearingDateTimeParser.isValid("2026-09-22", "25:00"))
        assertFalse(HearingDateTimeParser.isValid("غدًا", "العاشرة"))
    }
}
