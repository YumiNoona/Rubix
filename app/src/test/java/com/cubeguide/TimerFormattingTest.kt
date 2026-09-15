package com.cubeguide

import com.cubeguide.ui.formatTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TimerFormattingTest {
    @Test fun timerUsesMinutesSecondsAndHundredths() {
        assertEquals("00:00.00", formatTime(0))
        assertEquals("00:09.87", formatTime(9_876))
        assertEquals("01:02.34", formatTime(62_349))
    }
}
