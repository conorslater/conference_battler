package dev.conferencebattler.ui.fighter

import dev.conferencebattler.gen.ItemGenerator
import dev.conferencebattler.gen.data.AttackBases
import dev.conferencebattler.gen.data.DefenceBases
import dev.conferencebattler.gen.data.uniques
import dev.conferencebattler.model.Item
import dev.conferencebattler.model.ItemKind
import dev.conferencebattler.model.Rarity
import org.junit.Assert.assertNotNull
import org.junit.Test

class GearTest {

    @Test
    fun everyItemHasAProp() {
        val bases = (AttackBases + DefenceBases).map { Item(ItemKind.ATTACK, it.name, 1u, 1, Rarity.NORMAL, 0) }
        val unique = ItemKind.entries.flatMap { uniques(it, 1u) }
        for (item in bases + unique) assertNotNull(item.name, gearFor(item))
        // Magic names wrap the base in affixes.
        for (i in 0 until 500) assertNotNull(gearFor(ItemGenerator.generate("qr-$i")))
    }
}
