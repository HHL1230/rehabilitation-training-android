package com.example.rehabilitationtraining.reminder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class ReminderSchedulerTest {
    @Test
    fun nextDelayMillisUsesSameDayWhenTimeIsStillAhead() {
        val now = LocalDateTime.of(2026, 6, 27, 8, 0)

        val delay = ReminderScheduler.nextDelayMillis(hour = 9, minute = 30, now = now)

        assertEquals(Duration.ofMinutes(90).toMillis(), delay)
    }

    @Test
    fun nextDelayMillisUsesNextDayWhenTimeHasPassed() {
        val now = LocalDateTime.of(2026, 6, 27, 10, 0)

        val delay = ReminderScheduler.nextDelayMillis(hour = 9, minute = 30, now = now)

        assertEquals(Duration.ofHours(23).plusMinutes(30).toMillis(), delay)
    }
}
