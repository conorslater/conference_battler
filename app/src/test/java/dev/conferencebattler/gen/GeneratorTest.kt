package dev.conferencebattler.gen

import dev.conferencebattler.gen.data.Roles
import dev.conferencebattler.model.ItemKind
import dev.conferencebattler.model.Rarity
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
    fun roleWeightsMatch() {
        val counts = names.groupingBy { CharacterGenerator.generate(it).role }.eachCount()
        val total = Roles.sumOf { it.weight }
        for (role in Roles) {
            val expected = names.size * role.weight / total
            val actual = counts[role.name] ?: 0
            assertTrue("${role.name}=$actual, expected ~$expected", actual in expected * 6 / 10..expected * 14 / 10 + 20)
        }
    }

    @Test
    fun lowLevelsAreMostCommon() {
        val counts = names.groupingBy { CharacterGenerator.generate(it).level }.eachCount()
        assertEquals((1..CharacterGenerator.MAX_LEVEL).toSet(), counts.keys)
        // Level 1 has weight 20 out of 210 (~950 in 10k), level 20 has weight 1 (~48).
        assertTrue("level1=${counts[1]}", counts.getValue(1) in 800..1_100)
        assertTrue("level20=${counts[20]}", counts.getValue(20) in 15..100)
    }

    @Test
    fun characterStatsStayInRange() {
        for (name in names) {
            val c = CharacterGenerator.generate(name)
            assertTrue(c.toString(), c.hp in 100..50_000 && c.hp % 100 == 0)
            assertTrue(c.toString(), c.st in 100..19_900 && c.st % 100 == 0)
            assertTrue(c.toString(), c.df in 0..19_900 && c.df % 100 == 0)
            assertTrue(c.toString(), c.title.isNotBlank())
        }
    }

    @Test
    fun itemsCoverBothKindsAndStayInRange() {
        val items = payloads.map { ItemGenerator.generate(it) }
        val attacks = items.count { it.kind == ItemKind.ATTACK }
        assertTrue("attacks=$attacks", attacks in 4_500..5_500)
        assertTrue(items.any { it.rarity == Rarity.MAGIC })
        assertTrue(items.any { it.rarity == Rarity.NORMAL })
        for (item in items) assertTrue(item.toString(), item.bonus in 0..9_900 && item.bonus % 100 == 0)
    }

    @Test
    fun uniquesAreRare() {
        val uniques = payloads.count { ItemGenerator.generate(it).rarity == Rarity.UNIQUE }
        assertTrue("uniques=$uniques", uniques in 20..100)
    }
}
