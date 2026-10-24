package dev.conferencebattler.battle

import dev.conferencebattler.gen.roundDown100
import dev.conferencebattler.model.Character
import dev.conferencebattler.rng.DiabloRandom

enum class Side {
    LEFT, RIGHT;

    val other: Side get() = if (this == LEFT) RIGHT else LEFT
}

/** A character in battle, with its current HP. */
data class Fighter(val character: Character, val hp: Int) {
    val isDown: Boolean get() = hp <= 0
}

/** What happened in one attack, for the UI to animate. */
data class TurnResult(
    val attacker: Side,
    val hit: Boolean,
    val damage: Int,
    /** The ×2 bonus that stands in for Barcode Battler's rhythm-timed button press. */
    val timingBonus: Boolean,
    /** Barcode Battler's "CRITICAL" label: damage above 1.5 × ST. It doesn't change the damage. */
    val critical: Boolean,
)

data class BattleState(
    val left: Fighter,
    val right: Fighter,
    val turn: Side,
    val turnNumber: Int = 1,
    val winner: Side? = null,
    val last: TurnResult? = null,
) {
    fun fighter(side: Side): Fighter = if (side == Side.LEFT) left else right

    internal fun with(side: Side, fighter: Fighter): BattleState =
        if (side == Side.LEFT) copy(left = fighter) else copy(right = fighter)
}

/**
 * Turn-by-turn combat using Barcode Battler II's damage rules, as reverse-engineered by fans
 * (finalfighter's BB2 simulator, FightingCalc.as, and barcodebattler.net pages 12/13). Speed is
 * left out to keep the stats to HP/ST/DF.
 *
 * - First attacker: flipCoin().
 * - Hit: generateRnd(256) < 204 (~80%).
 * - Damage: ST includes the equipped item. a = 2*ST + ST*r/10 with r from {0,2,4,6,8}. If
 *   a < DF, damage is 100. Otherwise it is (a - DF)/2 + 100. Then ×2 for the timing bonus,
 *   rounded down to the nearest 100.
 *
 * Battle randomness is meant to vary between rematches, so seed [rng] from the clock. Character
 * and item generation stay deterministic.
 */
class BattleEngine(private val rng: DiabloRandom) {

    fun start(left: Character, right: Character): BattleState = BattleState(
        left = Fighter(left, left.hp),
        right = Fighter(right, right.hp),
        turn = if (rng.flipCoin()) Side.LEFT else Side.RIGHT,
    )

    /**
     * The side whose turn it is attacks.
     *
     * @param timing true/false if the UI measured the timing tap, or null to roll it
     *   (1 time in [TIMING_CHANCE]).
     */
    fun attack(state: BattleState, timing: Boolean? = null): BattleState {
        check(state.winner == null) { "Battle is over" }
        val side = state.turn
        val defender = state.fighter(side.other)
        val st = state.fighter(side).character.totalSt
        val df = defender.character.df

        val hit = rng.generateRnd(256) < HIT_CHANCE
        var damage = 0
        var timingBonus = false
        if (hit) {
            val r = rng.pickRandomlyAmong(0, 2, 4, 6, 8)
            val a = 2 * st + st * r / 10
            damage = if (a < df) 100 else (a - df) / 2 + 100
            timingBonus = timing ?: rng.flipCoin(TIMING_CHANCE)
            if (timingBonus) damage *= 2
            damage = maxOf(100, roundDown100(damage))
        }

        val newDefender = defender.copy(hp = maxOf(0, defender.hp - damage))
        return state.with(side.other, newDefender).copy(
            turn = side.other,
            turnNumber = state.turnNumber + 1,
            winner = if (newDefender.isDown) side else null,
            last = TurnResult(
                attacker = side,
                hit = hit,
                damage = damage,
                timingBonus = timingBonus,
                critical = damage > st * 3 / 2,
            ),
        )
    }

    companion object {
        const val HIT_CHANCE = 204
        const val TIMING_CHANCE = 4
    }
}
