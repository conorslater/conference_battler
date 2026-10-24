package dev.conferencebattler.gen

import dev.conferencebattler.model.CharClass
import dev.conferencebattler.model.Rarity
import dev.conferencebattler.model.ItemKind
import dev.conferencebattler.model.Tier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratorTest {

    private val names = (0 until 10_000).map { "PLAYER $it" }
    private val payloads = (0 until 10_000).map { "https://example.com/talk/$it" }

    @Test
    fun badgeIsDeterministicAcrossOcrVariants() {
        val a = CharacterGenerator.generate("Ada Lovelace")
        val b = CharacterGenerator.generate("  ADA   lovelace\n")
        assertEquals(a, b)
    }

    @Test
    fun itemIsDeterministic() {
        assertEquals(ItemGenerator.generate("WIFI:S:conf;;"), ItemGenerator.generate("WIFI:S:conf;;"))
    }

    @Test
    fun classSplitIsRoughlyEven() {
        val warriors = names.count { CharacterGenerator.generate(it).charClass == CharClass.WARRIOR }
        assertTrue("warriors=$warriors", warriors in 4_500..5_500)
    }

    @Test
    fun tierWeightsMatch() {
        val counts = names.groupingBy { CharacterGenerator.generate(it).tier }.eachCount()
        assertTrue("common=${counts[Tier.COMMON]}", counts.getValue(Tier.COMMON) in 5_500..6_500)
        assertTrue("veteran=${counts[Tier.VETERAN]}", counts.getValue(Tier.VETERAN) in 2_600..3_400)
        assertTrue("champion=${counts[Tier.CHAMPION]}", counts.getValue(Tier.CHAMPION) in 650..1_150)
        assertTrue("legend=${counts[Tier.LEGEND]}", counts.getValue(Tier.LEGEND) in 40..180)
    }

    @Test
    fun characterStatsStayInRange() {
        for (name in names) {
            val c = CharacterGenerator.generate(name)
            assertTrue(c.toString(), c.hp in 100..40_000 && c.hp % 100 == 0)
            assertTrue(c.toString(), c.st in 100..9_900 && c.st % 100 == 0)
            assertTrue(c.toString(), c.df in 0..9_900 && c.df % 100 == 0)
            assertTrue(c.toString(), c.title.isNotBlank())
        }
    }

    @Test
    fun itemsCoverBothKindsAndStayInRange() {
        val items = payloads.map { ItemGenerator.generate(it) }
        val weapons = items.count { it.kind == ItemKind.WEAPON }
        assertTrue("weapons=$weapons", weapons in 4_500..5_500)
        assertTrue(items.any { it.rarity == Rarity.MAGIC })
        assertTrue(items.any { it.rarity == Rarity.NORMAL })
        for (item in items) assertTrue(item.toString(), item.stBonus in 0..9_900 && item.stBonus % 100 == 0)
    }

    @Test
    fun uniquesAreRare() {
        val uniques = payloads.count { ItemGenerator.generate(it).rarity == Rarity.UNIQUE }
        assertTrue("uniques=$uniques", uniques in 20..100)
    }
}
