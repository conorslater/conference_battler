package dev.conferencebattler.battle

import dev.conferencebattler.gen.CharacterGenerator
import dev.conferencebattler.gen.ItemGenerator
import dev.conferencebattler.model.Character
import dev.conferencebattler.model.ItemKind
import dev.conferencebattler.rng.DiabloRandom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleEngineTest {

    private val characters = (0 until 200).map { CharacterGenerator.generate("FIGHTER $it") }
    private val items = (0 until 2_000).map { ItemGenerator.generate("qr $it") }

    private fun armed(c: Character): Character = c.equip(items[c.seed.toInt().mod(items.size)])

    private fun play(engine: BattleEngine, left: Character, right: Character): BattleState {
        var state = engine.start(left, right)
        while (state.winner == null) {
            state = engine.attack(state)
            assertTrue("battle too long: $left vs $right", state.turnNumber < 200)
        }
        return state
    }

    @Test
    fun battlesAlwaysEnd() {
        val engine = BattleEngine(DiabloRandom(42u))
        for (i in characters.indices step 2) {
            val result = play(engine, armed(characters[i]), armed(characters[i + 1]))
            assertNotNull(result.winner)
            assertTrue(result.fighter(result.winner!!.other).isDown)
        }
    }

    @Test
    fun unarmedBattlesAlwaysEnd() {
        val engine = BattleEngine(DiabloRandom(7u))
        for (i in characters.indices step 2) assertNotNull(play(engine, characters[i], characters[i + 1]).winner)
    }

    @Test
    fun hitsDealAtLeast100() {
        val engine = BattleEngine(DiabloRandom(1u))
        var state = engine.start(characters[0], characters[1])
        repeat(20) {
            if (state.winner != null) return
            state = engine.attack(state)
            val last = state.last!!
            if (last.hit) assertTrue(last.damage >= 100 && last.damage % 100 == 0)
            else assertEquals(0, last.damage)
        }
    }

    @Test
    fun turnsAlternate() {
        val engine = BattleEngine(DiabloRandom(3u))
        val start = engine.start(characters[2], characters[3])
        val next = engine.attack(start)
        if (next.winner == null) assertEquals(start.turn.other, next.turn)
    }

    @Test
    fun attackItemAddsToSt() {
        val c = characters[6]
        val item = items.first { it.kind == ItemKind.ATTACK && it.bonus > 0 }
        assertEquals(c.st + item.bonus, c.equip(item).totalSt)
        assertEquals(c.df, c.equip(item).totalDf)
    }

    @Test
    fun defenceItemAddsToDf() {
        val c = characters[6]
        val item = items.first { it.kind == ItemKind.DEFENCE && it.bonus > 0 }
        assertEquals(c.df + item.bonus, c.equip(item).totalDf)
        assertEquals(c.st, c.equip(item).totalSt)
    }
}
