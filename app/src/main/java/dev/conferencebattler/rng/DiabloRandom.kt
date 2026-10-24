package dev.conferencebattler.rng

import kotlin.math.max

/**
 * Port of DevilutionX's `DiabloGenerator` (Source/engine/random.hpp).
 *
 * The engine is the Borland C/C++ LCG, `state = state * 0x015A4E35 + 1 (mod 2^32)`. The helpers
 * keep vanilla Diablo's quirks, including the abs() distribution and the `>> 16` for small
 * bounds, so results can be checked against DevilutionX's own test vectors
 * (test/random_test.cpp).
 *
 * Diablo regenerates an item from its `_iSeed` by seeding this engine and replaying a fixed
 * sequence of calls. Every generator in this app works the same way: the same seed and the same
 * call order always give the same character or item.
 */
class DiabloRandom(seed: UInt) {
    /** Current engine state, the equivalent of `GetLCGEngineState()`. */
    var state: UInt = seed
        private set

    /** Advances the engine and returns the raw new state. */
    fun generateRandomNumber(): UInt {
        state = state * MULTIPLIER + INCREMENT
        return state
    }

    /** Advances the engine by [count] rounds, discarding the results. */
    fun discardRandomValues(count: Int) {
        repeat(count) { generateRandomNumber() }
    }

    /**
     * Advances the engine and returns the new state, read as a signed value, passed through abs().
     *
     * Usually non-negative, but returns Int.MIN_VALUE when the state is 0x80000000, the same as
     * vanilla.
     */
    fun advanceRndSeed(): Int {
        val seed = generateRandomNumber().toInt()
        return if (seed == Int.MIN_VALUE) Int.MIN_VALUE else kotlin.math.abs(seed)
    }

    /**
     * Returns a value in [0, v), or 0 without advancing the engine if v <= 0.
     *
     * Bounds up to 0x7FFF use the high bits (`>> 16`), the same as vanilla. Very rarely this
     * returns a negative value in (-v, -1], because of the bug in [advanceRndSeed]. Use
     * [randomIntLessThan] when indexing.
     */
    fun generateRnd(v: Int): Int {
        if (v <= 0) return 0
        if (v <= 0x7FFF) return (advanceRndSeed() shr 16) % v
        return advanceRndSeed() % v
    }

    /** Returns true one time in [frequency]. */
    fun flipCoin(frequency: Int = 2): Boolean = generateRnd(frequency) == 0

    /** Same as [generateRnd], but never returns a negative value. */
    fun randomIntLessThan(v: Int): Int = max(generateRnd(v), 0)

    /** Returns a value in [min, max], or [min, max) when [halfOpen] is true. */
    fun randomIntBetween(min: Int, max: Int, halfOpen: Boolean = false): Int =
        randomIntLessThan(max - min + if (halfOpen) 0 else 1) + min

    /** Picks one of [values] at random. */
    fun <T> pickRandomlyAmong(values: List<T>): T = values[randomIntLessThan(values.size)]

    fun <T> pickRandomlyAmong(vararg values: T): T = pickRandomlyAmong(values.asList())

    /** Port of `RndPL` (items.cpp): a value in [param1, param2]. */
    fun rndPL(param1: Int, param2: Int): Int = param1 + generateRnd(param2 - param1 + 1)

    private companion object {
        const val MULTIPLIER = 0x015A4E35u
        const val INCREMENT = 1u
    }
}
