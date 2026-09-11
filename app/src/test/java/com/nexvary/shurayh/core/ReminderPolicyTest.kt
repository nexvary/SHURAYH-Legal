package com.nexvary.shurayh.core

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderPolicyTest {
    @Test fun prefers_twenty_four_hours_when_possible() {
        val now = LocalDateTime.of(2026, 9, 12, 10, 0)
        val hearing = LocalDateTime.of(2026, 9, 15, 10, 0)
        assertEquals(LocalDateTime.of(2026, 9, 14, 10, 0), ReminderPolicy.chooseReminderTime(hearing, now))
    }

    @Test fun falls_back_for_near_hearing() {
        val now = LocalDateTime.of(2026, 9, 12, 10, 0)
        val hearing = LocalDateTime.of(2026, 9, 12, 13, 0)
        assertEquals(LocalDateTime.of(2026, 9, 12, 11, 0), ReminderPolicy.chooseReminderTime(hearing, now))
    }

    @Test fun past_hearing_is_not_scheduled() {
        val now = LocalDateTime.of(2026, 9, 12, 10, 0)
        assertNull(ReminderPolicy.chooseReminderTime(LocalDateTime.of(2026, 9, 12, 9, 0), now))
    }
}
