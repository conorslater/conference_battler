package dev.conferencebattler.ui.fighter

import org.junit.Assert.assertEquals
import org.junit.Test

class PoseTest {

    private fun lowestFoot(p: Pose) = Skeleton(p).let { minOf(it.frontFoot.y, it.backFoot.y) }

    @Test
    fun feetStayOnTheFloor() {
        for (pose in listOf(Guard, Moves.idle(0.5f), Moves.punch.at(1f), Moves.kick.at(1f))) {
            assertEquals(Skeleton.SHOE, lowestFoot(pose), 1e-4f)
        }
    }

    @Test
    fun strikesReachTheOpponent() {
        // The defender stands DISTANCE away, facing back, with the front of their body 0.1 nearer.
        for (kick in listOf(false, true)) {
            assertEquals(Moves.DISTANCE - 0.1f, Moves.strikePoint(kick).x, 1e-4f)
        }
    }

    @Test
    fun movesStartAndEndInGuard() {
        for (move in listOf(Moves.punch, Moves.kick, Moves.hit, Moves.dodge, Moves.victory, Moves.knockout)) {
            assertEquals(Guard, move.at(0f))
        }
        for (move in listOf(Moves.punch, Moves.kick, Moves.hit, Moves.dodge)) {
            assertEquals(Guard, move.at(move.end))
        }
    }
}
