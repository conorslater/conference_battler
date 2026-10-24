package dev.conferencebattler.gen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SeedsTest {

    @Test
    fun normalizationIgnoresCaseSpacingAccentsAndPunctuation() {
        assertEquals("ADA LOVELACE", Seeds.normalizeBadge("  Ada  Lovelace "))
        assertEquals("ADA LOVELACE", Seeds.normalizeBadge("ada\nlovelace"))
        assertEquals("ZOE OBRIEN", Seeds.normalizeBadge("Zoë O'Brien"))
        assertEquals("JEANLUC", Seeds.normalizeBadge("Jean-Luc"))
        assertEquals("A B", Seeds.normalizeBadge("A - B"))
    }

    @Test
    fun badgeKeyIgnoresLineOrder() {
        assertEquals("2026 ADA DEVCONF LOVELACE", Seeds.badgeKey("DevConf 2026\nAda Lovelace"))
        assertEquals(Seeds.badgeKey("Ada Lovelace\nDevConf 2026"), Seeds.badgeKey("DevConf 2026\nAda Lovelace"))
    }

    @Test
    fun sameTextGivesSameSeed() {
        assertEquals(Seeds.badgeSeed("ADA LOVELACE"), Seeds.badgeSeed("ADA LOVELACE"))
    }

    @Test
    fun domainsAreSeparated() {
        assertNotEquals(Seeds.badgeSeed("ALICE"), Seeds.itemSeed("ALICE"))
    }

    @Test
    fun differentTextGivesDifferentSeed() {
        assertNotEquals(Seeds.badgeSeed("ADA LOVELACE"), Seeds.badgeSeed("ALAN TURING"))
    }
}
