package dev.conferencebattler.gen

import dev.conferencebattler.rng.SplitMix32
import java.security.MessageDigest
import java.text.Normalizer
import java.util.Locale

/**
 * Turns scanned text into the 32-bit seed that drives a generator:
 *   text -> normalize -> SHA-256("v<version>:<domain>:<text>") -> first 4 bytes -> SplitMix32.
 *
 * The domain keeps badges and QR codes apart: a QR code that encodes "ALICE" does not give the
 * same seed as the badge "ALICE". The version lets us rebalance later without silently changing
 * existing stats: bump [SEED_VERSION] and every badge rerolls on purpose.
 */
object Seeds {
    const val SEED_VERSION = 2
    const val BADGE = "BADGE"
    const val ITEM = "ITEM"

    private val combiningMarks = Regex("\\p{M}+")
    private val whitespace = Regex("\\s+")
    private val notAlphanumericOrSpace = Regex("[^A-Z0-9 ]")

    /**
     * Normalizes OCR'd badge text so that small differences between reads give the same name:
     * case, accents, punctuation and spacing are all ignored.
     * "  Zoë  O'Brien\n" -> "ZOE OBRIEN".
     */
    fun normalizeBadge(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFKD)
            .replace(combiningMarks, "")
            .uppercase(Locale.ROOT)
            .replace(whitespace, " ")
            .replace(notAlphanumericOrSpace, "")
            .replace(whitespace, " ")
            .trim()

    fun seedFor(domain: String, text: String): UInt {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("v$SEED_VERSION:$domain:$text".toByteArray(Charsets.UTF_8))
        val prefix = (digest[0].toUInt() and 0xFFu) or
            ((digest[1].toUInt() and 0xFFu) shl 8) or
            ((digest[2].toUInt() and 0xFFu) shl 16) or
            ((digest[3].toUInt() and 0xFFu) shl 24)
        return SplitMix32(prefix).next()
    }

    /**
     * The text a badge is hashed from: every word OCR found, normalized and sorted. Sorting makes
     * the key independent of the order OCR returns lines in, which changes with camera angle.
     * "DevConf 2026\nAda Lovelace" -> "2026 ADA DEVCONF LOVELACE".
     */
    fun badgeKey(text: String): String =
        normalizeBadge(text).split(' ').filter { it.isNotEmpty() }.sorted().joinToString(" ")

    /** Seed for a badge. [key] must come from [badgeKey]. */
    fun badgeSeed(key: String): UInt = seedFor(BADGE, key)

    /** Seed for a QR code. The payload is used exactly as decoded, since QR decoding is lossless. */
    fun itemSeed(payload: String): UInt = seedFor(ITEM, payload)
}
