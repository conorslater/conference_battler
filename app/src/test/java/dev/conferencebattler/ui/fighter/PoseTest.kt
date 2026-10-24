package dev.conferencebattler.ui.fighter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PoseTest {

    private fun lowestFoot(p: Pose) = Skeleton(p).let { minOf(it.frontFoot.y, it.backFoot.y) }

    private fun assertSame(expected: Skeleton, actual: Skeleton) {
        expected.joints().zip(actual.joints()).forEach { (e, a) ->
            assertEquals(e.x, a.x, 1e-3f)
            assertEquals(e.y, a.y, 1e-3f)
        }
    }

    @Test
    fun feetStayOnTheFloor() {
        val poses = listOf(Guard, Kneel, Moves.idle(0.5f)) +
            listOf(Moves.jab, Moves.slap, Moves.bodyKick, Moves.roundhouse).map { it.move.pose(it.move.impact) }
        for (pose in poses) assertEquals(Skeleton.SHOE, lowestFoot(pose), 1e-4f)
    }

    @Test
    fun strikesReachTheOpponent() {
        // The defender stands DISTANCE away, facing back, with the front of their body and face nearer.
        for (s in listOf(Moves.jab, Moves.bodyKick)) assertEquals(Moves.DISTANCE - Moves.BODY_FRONT, s.point.x, 1e-4f)
        for (s in listOf(Moves.slap, Moves.uppercut, Moves.roundhouse)) {
            assertEquals(Moves.DISTANCE - Moves.HEAD_FRONT, s.point.x, 1e-4f)
            val head = Skeleton(Guard).head.y
            assertTrue("${s.point.y} vs head at $head", s.point.y in head - 0.12f..head + 0.12f)
        }
    }

    @Test
    fun movesStartAndEndInPlace() {
        for (s in Moves.combo) {
            assertEquals(Moves.close, s.move.pose(0f))
            assertEquals(Moves.close, s.move.pose(s.move.end))
        }
        for (s in listOf(Moves.uppercut, Moves.roundhouse)) {
            assertEquals(Moves.close, s.move.pose(0f))
            assertEquals(Guard, s.move.pose(s.move.end))
        }
        for (move in listOf(Moves.flinch, Moves.dodge)) {
            assertEquals(Guard, move.pose(0f))
            assertEquals(Guard, move.pose(move.end))
        }
        assertEquals(Guard, Moves.victory.pose(0f))
        assertEquals(Moves.close, Moves.stepIn.pose(Moves.stepIn.end))
    }

    @Test
    fun skeletonLerpReachesBothEnds() {
        val a = Skeleton(Guard)
        val b = Skeleton(Kneel.copy(x = -0.7f))
        assertSame(a, a.lerp(b, 0f))
        assertSame(b, a.lerp(b, 1f))
    }
}
