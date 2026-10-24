package dev.conferencebattler.scan

import dev.conferencebattler.gen.Seeds

/** One line of OCR output, in upright image coordinates. Kept free of ML Kit types for testing. */
data class OcrLine(val text: String, val top: Int, val left: Int, val height: Int)

/**
 * What a badge scan produced.
 *
 * @param text every line OCR found, in reading order. All of it is hashed.
 * @param name the tallest line, which on a nametag is usually the person's name. Display only.
 */
data class BadgeReading(val text: String, val name: String) {
    val key: String get() = Seeds.badgeKey(text)
}

/** Builds a [BadgeReading] from one frame, or returns null if the frame has no usable text. */
fun readBadge(lines: List<OcrLine>): BadgeReading? {
    val kept = lines.filter { Seeds.normalizeBadge(it.text).isNotEmpty() }
    if (kept.isEmpty()) return null
    val ordered = kept.sortedWith(compareBy({ it.top }, { it.left }))
    return BadgeReading(
        text = ordered.joinToString("\n") { it.text },
        name = ordered.maxBy { it.height }.text,
    )
}

/**
 * Accepts a reading once the same [BadgeReading.key] has come back in [framesNeeded] frames in a
 * row. OCR misreads a character now and then (O/0, I/1), and a single misread would give the
 * player a different character, so one frame isn't enough.
 */
class StableReading(private val framesNeeded: Int = 3) {
    private var key: String? = null

    /** How many frames in a row have agreed so far. */
    var count: Int = 0
        private set

    fun offer(reading: BadgeReading?): BadgeReading? {
        val k = reading?.key?.takeIf { it.isNotEmpty() }
        count = if (k != null && k == key) count + 1 else if (k != null) 1 else 0
        key = k
        return reading.takeIf { count >= framesNeeded }
    }

    val progress: Float get() = minOf(count, framesNeeded).toFloat() / framesNeeded
}
