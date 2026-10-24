package dev.conferencebattler.ui.fighter

import dev.conferencebattler.ui.fighter.Skeleton.Companion.FOREARM
import dev.conferencebattler.ui.fighter.Skeleton.Companion.HEAD_RADIUS
import dev.conferencebattler.ui.fighter.Skeleton.Companion.HEAD_REACH
import dev.conferencebattler.ui.fighter.Skeleton.Companion.SHIN
import dev.conferencebattler.ui.fighter.Skeleton.Companion.SHOE
import dev.conferencebattler.ui.fighter.Skeleton.Companion.THIGH
import dev.conferencebattler.ui.fighter.Skeleton.Companion.TORSO
import dev.conferencebattler.ui.fighter.Skeleton.Companion.UPPER_ARM
import kotlin.math.floor
import kotlin.math.max

/**
 * How a finisher throws the defender, in figure heights per second with x towards the attacker.
 * [spin] is in radians per second, anticlockwise: positive tips the head back, away from the
 * attacker.
 */
data class Launch(val velocity: Vec, val spin: Float)

/**
 * A fighter thrown by a [Launch]. The joints are point masses on stiff bones (Verlet
 * integration): they fly, fall under gravity, bounce and slide along the floor until they stop.
 * The whole flight is worked out up front, so it plays the same every time and [at] is a lookup.
 * Time is in seconds.
 */
class Ragdoll(start: Skeleton, launch: Launch) : Motion {
    private val pos = start.joints().toTypedArray()
    private val old: Array<Vec>
    /** Each joint's velocity before the floor stopped it, for the bounce. */
    private val falling = FloatArray(pos.size)
    private val frames = mutableListOf(start)

    override val end: Float get() = (frames.size - 1) * FRAME

    /** Where the fighter came to rest. */
    val rest: Skeleton get() = frames.last()

    init {
        old = Array(pos.size) { i ->
            val r = pos[i] - start.hip
            pos[i] - (launch.velocity + Vec(-r.y, r.x) * launch.spin) * DT
        }
        var step = 0
        var still = 0
        while (step < MAX_STEPS && still < STILL_FRAMES) {
            step++
            advance()
            if (step % SUBSTEPS == 0) {
                frames += skeleton()
                val moving = pos.indices.any { (pos[it] - old[it]).length > STILL_SPEED * DT }
                still = if (moving || step * DT < MIN_SECONDS) 0 else still + 1
            }
        }
    }

    override fun at(time: Float): Skeleton {
        val f = (time / FRAME).coerceIn(0f, (frames.size - 1).toFloat())
        val i = floor(f).toInt().coerceAtMost(frames.size - 2)
        return frames[i].lerp(frames[i + 1], f - i)
    }

    private fun skeleton() = Skeleton(pos[0], pos[1], pos[2], pos[3], pos[4], pos[5], pos[6], pos[7], pos[8], pos[9])

    private fun advance() {
        for (i in pos.indices) {
            val v = (pos[i] - old[i]) * AIR_DRAG
            old[i] = pos[i]
            pos[i] = pos[i] + v + Vec(0f, -GRAVITY * DT * DT)
            falling[i] = v.y
        }
        repeat(ITERATIONS) {
            bones()
            bounds()
        }
        // Whatever touches the floor drags along it and bounces a little.
        for (i in pos.indices) if (pos[i].y <= RADIUS[i] + EPSILON) rub(i)
        if (head().y <= HEAD_RADIUS + EPSILON) rub(NECK)
    }

    private fun rub(i: Int) {
        val v = pos[i] - old[i]
        val vy = if (falling[i] < 0f) -falling[i] * BOUNCE else v.y
        old[i] = pos[i] - Vec(v.x * FRICTION, vy)
        falling[i] = 0f
    }

    private fun head() = pos[HIP] + (pos[NECK] - pos[HIP]) * (HEAD_REACH / TORSO)

    private fun shoulder() = pos[HIP] + (pos[NECK] - pos[HIP]) * ((TORSO - 0.04f) / TORSO)

    private fun bones() {
        link(HIP, NECK, TORSO)
        hang(FRONT_ELBOW, UPPER_ARM)
        hang(BACK_ELBOW, UPPER_ARM)
        link(FRONT_ELBOW, FRONT_HAND, FOREARM)
        link(BACK_ELBOW, BACK_HAND, FOREARM)
        link(HIP, FRONT_KNEE, THIGH)
        link(HIP, BACK_KNEE, THIGH)
        link(FRONT_KNEE, FRONT_FOOT, SHIN)
        link(BACK_KNEE, BACK_FOOT, SHIN)
        // Elbows and knees can't fold flat.
        link(HIP, FRONT_FOOT, 0.3f, atLeast = true)
        link(HIP, BACK_FOOT, 0.3f, atLeast = true)
        hang(FRONT_HAND, 0.15f, atLeast = true)
        hang(BACK_HAND, 0.15f, atLeast = true)
    }

    /** Pulls joints [a] and [b] to [length] apart, the lighter one moving more. */
    private fun link(a: Int, b: Int, length: Float, atLeast: Boolean = false) {
        val d = pos[b] - pos[a]
        val dist = d.length
        if (dist == 0f || (atLeast && dist >= length)) return
        val wa = 1 / MASS[a]
        val wb = 1 / MASS[b]
        val k = (dist - length) / dist / (wa + wb)
        pos[a] = pos[a] + d * (k * wa)
        pos[b] = pos[b] - d * (k * wb)
    }

    /** Holds joint [i] at [length] from the shoulder. The arms are light, so only they move. */
    private fun hang(i: Int, length: Float, atLeast: Boolean = false) {
        val s = shoulder()
        val d = pos[i] - s
        if (atLeast && d.length >= length) return
        pos[i] = s + d.unit() * length
    }

    /** Keeps everything above the floor and in front of the edge of the stage behind. */
    private fun bounds() {
        for (i in pos.indices) pos[i] = Vec(max(pos[i].x, WALL + RADIUS[i]), max(pos[i].y, RADIUS[i]))
        // The head is on the end of the spine, so lift the neck to keep it clear.
        val head = head()
        val lever = TORSO / HEAD_REACH
        if (head.y < HEAD_RADIUS) pos[NECK] = pos[NECK] + Vec(0f, (HEAD_RADIUS - head.y) * lever)
        if (head.x < WALL + HEAD_RADIUS) pos[NECK] = pos[NECK] + Vec((WALL + HEAD_RADIUS - head.x) * lever, 0f)
    }

    private companion object {
        const val HIP = 0
        const val NECK = 1
        const val FRONT_ELBOW = 2
        const val FRONT_HAND = 3
        const val BACK_ELBOW = 4
        const val BACK_HAND = 5
        const val FRONT_KNEE = 6
        const val FRONT_FOOT = 7
        const val BACK_KNEE = 8
        const val BACK_FOOT = 9

        val MASS = floatArrayOf(4f, 3f, 1f, 0.7f, 1f, 0.7f, 1.5f, 1f, 1.5f, 1f)
        /** How far each joint's outline sticks out, so it rests on the floor rather than in it. */
        val RADIUS = floatArrayOf(0.075f, 0.085f, 0.035f, 0.034f, 0.035f, 0.034f, 0.04f, SHOE, 0.04f, SHOE)

        /** In figure heights per second², a little floatier than real life. */
        const val GRAVITY = 7f
        const val DT = 1f / 240
        const val SUBSTEPS = 4
        const val FRAME = DT * SUBSTEPS
        const val ITERATIONS = 8
        const val AIR_DRAG = 0.999f
        /** How much sideways speed a joint keeps per step on the floor. */
        const val FRICTION = 0.97f
        const val BOUNCE = 0.3f
        const val EPSILON = 1e-4f

        /** The edge of the stage behind the defender: past this they'd land under the buttons. */
        const val WALL = -1.45f

        const val MAX_STEPS = 240 * 3
        const val MIN_SECONDS = 0.5f
        const val STILL_SPEED = 0.05f
        const val STILL_FRAMES = 6
    }
}
