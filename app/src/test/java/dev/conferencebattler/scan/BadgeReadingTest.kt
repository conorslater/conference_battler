package dev.conferencebattler.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BadgeReadingTest {

    private val badge = listOf(
        OcrLine("Speaker", top = 400, left = 10, height = 20),
        OcrLine("Ada Lovelace", top = 200, left = 10, height = 80),
        OcrLine("DevConf 2026", top = 50, left = 10, height = 30),
        OcrLine("•", top = 500, left = 10, height = 10),
    )

    @Test
    fun readsAllLinesInReadingOrderAndTallestAsName() {
        val reading = readBadge(badge)!!
        assertEquals("DevConf 2026\nAda Lovelace\nSpeaker", reading.text)
        assertEquals("Ada Lovelace", reading.name)
    }

    @Test
    fun emptyFrameGivesNothing() {
        assertNull(readBadge(emptyList()))
        assertNull(readBadge(listOf(OcrLine("--", 0, 0, 10))))
    }

    @Test
    fun acceptsOnlyAfterConsecutiveAgreement() {
        val stable = StableReading(framesNeeded = 3)
        val good = readBadge(badge)
        val misread = readBadge(badge.map { if (it.text == "Speaker") it.copy(text = "Speaksr") else it })
        assertNull(stable.offer(good))
        assertNull(stable.offer(good))
        assertNull(stable.offer(misread))
        assertNull(stable.offer(good))
        assertNull(stable.offer(null))
        assertNull(stable.offer(good))
        assertNull(stable.offer(good))
        assertEquals(good, stable.offer(good))
    }

    @Test
    fun lineOrderDoesNotBreakAgreement() {
        val stable = StableReading(framesNeeded = 2)
        val tilted = badge.map { if (it.text == "Speaker") it.copy(top = 0) else it }
        assertNull(stable.offer(readBadge(badge)))
        assertEquals(readBadge(tilted), stable.offer(readBadge(tilted)))
    }
}
