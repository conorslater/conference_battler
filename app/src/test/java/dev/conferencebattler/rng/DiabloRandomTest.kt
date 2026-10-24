package dev.conferencebattler.rng

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Port of DevilutionX test/random_test.cpp. `SetRndSeed(x)` becomes `DiabloRandom(x)`. The
 * vectors are copied unchanged, so a pass means the port matches vanilla Diablo exactly.
 */
class DiabloRandomTest {

    private fun rnd(seed: UInt) = DiabloRandom(seed)

    @Test
    fun randomEngineParams() {
        val r = rnd(0u)
        assertEquals("Increment factor is incorrect", 1u, r.generateRandomNumber())
        assertEquals("Multiplicand factor is incorrect", 22695477u * 1u + 1u, r.generateRandomNumber())
        r.discardRandomValues(9997)
        assertEquals("Wrong engine state after 9999 invocations", 3495122800u, r.generateRandomNumber())
        assertEquals("Wrong engine state after 10000 invocations", 3007658545u, r.generateRandomNumber())
    }

    @Test
    fun absDistribution() {
        val cases = listOf(
            1457187811u to Int.MIN_VALUE,
            3604671459u to 0,
            0u to 1,
            2914375622u to 1,
            3604671460u to 22695477,
            3604671458u to 22695477,
            1902003768u to 429496729,
            1012371854u to 429496729,
            189776845u to 1212022642,
            2724598777u to 1212022642,
            76596137u to 2147483646,
            2837779485u to 2147483646,
            766891974u to 2147483647,
            2147483648u to 2147483647,
        )
        for ((seed, expected) in cases) {
            assertEquals("seed $seed", expected, rnd(seed).advanceRndSeed())
        }
    }

    @Test
    fun modDistributionInvalidRange() {
        val r = rnd(44444444u)
        for (bound in listOf(0, -1, Int.MIN_VALUE)) {
            assertEquals(0, r.generateRnd(bound))
            assertEquals("Invalid range must not advance the engine", 44444444u, r.state)
        }
    }

    @Test
    fun shiftModDistributionSingleRange() {
        val r = rnd(12345u)
        repeat(100) { assertEquals(0, r.generateRnd(1)) }
        assertNotEquals(12345u, r.state)
        assertEquals(0, rnd(1457187811u).generateRnd(1))
    }

    @Test
    fun shiftModDistributionSignCarry() {
        assertEquals(-1, rnd(1457187811u).generateRnd(32767))
    }

    @Test
    fun shiftModDistributionLowBits() {
        val maxBound = 32767
        val seeds = listOf(
            3604671459u, 0u, 2914375622u, 538964771u, 2375410851u, 1229260608u, 1685115014u,
            1768225379u, 1146150243u, 1480523688u, 1433851934u, 2382565573u, 531810049u,
            1625910243u, 1288465379u, 3251820486u, 3957522432u,
        )
        for (seed in seeds) assertEquals("seed $seed", 0, rnd(seed).generateRnd(maxBound))
        assertEquals(0, rnd(2561524649u).generateRnd(maxBound - 1))
        assertEquals(0, rnd(352850973u).generateRnd(maxBound - 1))
    }

    @Test
    fun shiftModDistributionHighBits() {
        val maxBound = 32767
        val cases = listOf(
            3267226595u to 1, 3942116323u to 1,
            4279561187u to 2, 2929781731u to 2,
            659483619u to 4, 2254892003u to 4,
            3604671458u to 346, 3604671460u to 346,
            1012371854u to 6553, 1902003768u to 6553,
            189776845u to 18493, 2724598777u to 18493,
            76596137u to 0, 2837779485u to 0,
            766891974u to 0, 2147483648u to 0,
        )
        for ((seed, expected) in cases) assertEquals("seed $seed", expected, rnd(seed).generateRnd(maxBound))
    }

    @Test
    fun modDistributionSignPreserving() {
        assertEquals(0, rnd(1457187811u).generateRnd(32768))
        assertEquals(-2, rnd(1457187811u).generateRnd(32769))
        assertEquals(-32768, rnd(1457187811u).generateRnd(65535))
        assertEquals(-1, rnd(1457187811u).generateRnd(Int.MAX_VALUE))
    }

    @Test
    fun negativeReturnValues() {
        val cases = mutableListOf(
            3 to -2, 5 to -3, 6 to -2, 7 to -1, 11 to -10, 17 to -9, 19 to -12, 22 to -10,
            23 to -16, 25 to -18, 27 to -17, 29 to -27, 31 to -1,
            37 to -23, 38 to -12, 40 to -8, 41 to -9, 52 to -8, 80 to -48, 100 to -68,
        )
        listOf(9, 10, 12, 13, 14, 15, 18, 20, 21, 24, 26, 28, 30).forEach { cases += it to -8 }
        generateSequence(1) { it * 2 }.takeWhile { it < 32768 }.forEach { cases += it to 0 }
        for ((bound, expected) in cases) {
            assertEquals("limit $bound", expected, rnd(1457187811u).generateRnd(bound))
        }
    }

    @Test
    fun randomIntLessThanNeverNegative() {
        assertEquals(0, rnd(1457187811u).randomIntLessThan(100))
    }
}
