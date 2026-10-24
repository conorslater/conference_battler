package dev.conferencebattler.ui.fighter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RagdollTest {

    private val finishers = listOf(Moves.uppercut, Moves.roundhouse)

    private fun throwFrom(strike: Strike) = Ragdoll(Skeleton(Guard), strike.launch!!)

    private fun frames(r: Ragdoll) = generateSequence(0f) { it + 1 / 60f }.takeWhile { it <= r.end }.map(r::at)

    @Test
    fun fliesThenLandsFlatAndStops() {
        for (strike in finishers) {
            val r = throwFrom(strike)
            val peak = frames(r).maxOf { it.hip.y }
            assertTrue("peak $peak", peak > Skeleton(Guard).hip.y + 0.2f)
            assertTrue("end ${r.end}", r.end in 0.8f..3f)
            // Lying down: the head is near the floor, and nothing is under it.
            assertTrue("head ${r.rest.head}", r.rest.head.y < 0.2f)
            assertTrue(r.rest.joints().all { it.y >= 0f })
        }
    }

    @Test
    fun staysOnTheStage() {
        for (strike in finishers) {
            for (sk in frames(throwFrom(strike))) {
                assertTrue(sk.joints().all { it.x >= -1.45f })
                assertTrue(sk.head.x >= -1.45f)
                assertTrue(sk.joints().all { it.y >= 0f })
            }
        }
    }

    @Test
    fun bonesKeepTheirLength() {
        for (strike in finishers) {
            for (sk in frames(throwFrom(strike))) {
                assertEquals(Skeleton.TORSO, (sk.neck - sk.hip).length, 0.01f)
                assertEquals(Skeleton.THIGH, (sk.frontKnee - sk.hip).length, 0.01f)
                assertEquals(Skeleton.SHIN, (sk.backFoot - sk.backKnee).length, 0.01f)
                assertEquals(Skeleton.FOREARM, (sk.frontHand - sk.frontElbow).length, 0.01f)
            }
        }
    }

    @Test
    fun getsBackUpIntoGuard() {
        for (strike in finishers) {
            val up = Moves.getUp(throwFrom(strike).rest)
            val end = up.at(up.end)
            Skeleton(Guard).joints().zip(end.joints()).forEach { (e, a) ->
                assertEquals(e.x, a.x, 1e-3f)
                assertEquals(e.y, a.y, 1e-3f)
            }
        }
    }
}
