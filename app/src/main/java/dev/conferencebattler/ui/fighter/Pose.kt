package dev.conferencebattler.ui.fighter

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A fighter's stance as joint angles, in degrees. Limb angles are measured from straight down,
 * positive towards the opponent. Arms are relative to the torso and forearms/shins relative to
 * the bone above them, so leaning carries the arms with it.
 *
 * Distances are in figure heights (a standing fighter is about 1.0 tall).
 *
 * @param x step towards the opponent (negative = pushed back).
 * @param lift height of the feet off the floor (a hop).
 * @param lean torso angle from upright, positive leaning towards the opponent.
 * @param fall 0 = standing, 1 = flat on the back. Rotates the whole figure about its feet.
 */
data class Pose(
    val x: Float = 0f,
    val lift: Float = 0f,
    val lean: Float = 0f,
    val frontArm: Float = 0f,
    val frontForearm: Float = 0f,
    val backArm: Float = 0f,
    val backForearm: Float = 0f,
    val frontThigh: Float = 0f,
    val frontShin: Float = 0f,
    val backThigh: Float = 0f,
    val backShin: Float = 0f,
    val fall: Float = 0f,
) {
    fun lerp(to: Pose, t: Float): Pose {
        fun l(a: Float, b: Float) = a + (b - a) * t
        return Pose(
            l(x, to.x), l(lift, to.lift), l(lean, to.lean),
            l(frontArm, to.frontArm), l(frontForearm, to.frontForearm),
            l(backArm, to.backArm), l(backForearm, to.backForearm),
            l(frontThigh, to.frontThigh), l(frontShin, to.frontShin),
            l(backThigh, to.backThigh), l(backShin, to.backShin),
            l(fall, to.fall),
        )
    }
}

data class Vec(val x: Float, val y: Float) {
    operator fun plus(o: Vec) = Vec(x + o.x, y + o.y)
    operator fun minus(o: Vec) = Vec(x - o.x, y - o.y)
    operator fun times(k: Float) = Vec(x * k, y * k)

    /** Rotated 90° anticlockwise: "forward" for a bone pointing down. */
    val normal: Vec get() = Vec(-y, x)

    /** Rotated 90° clockwise: "forward" for a bone pointing up. */
    val across: Vec get() = Vec(y, -x)

    fun unit(): Vec = sqrt(x * x + y * y).let { if (it == 0f) this else Vec(x / it, y / it) }

    /** Rotated [deg] anticlockwise. */
    fun rotate(deg: Float): Vec {
        val r = (deg * PI / 180).toFloat()
        return Vec(x * cos(r) - y * sin(r), x * sin(r) + y * cos(r))
    }
}

/**
 * Joint positions for a [Pose], in figure heights. x points towards the opponent and y points up,
 * with the feet's starting point at the origin. [Pose.fall] isn't applied here; the renderer
 * rotates the whole figure about [pivot].
 */
class Skeleton(val pose: Pose) {
    val hip: Vec
    val neck: Vec
    val shoulder: Vec
    val head: Vec
    val frontElbow: Vec
    val frontHand: Vec
    val backElbow: Vec
    val backHand: Vec
    val frontKnee: Vec
    val frontFoot: Vec
    val backKnee: Vec
    val backFoot: Vec

    /** The point on the floor below the fighter, for the shadow and the fall rotation. */
    val pivot: Vec get() = Vec(pose.x, 0f)

    init {
        val p = pose
        // Stand the hip on whichever leg reaches lower, so the feet stay on the floor.
        val drop = max(legDrop(p.frontThigh, p.frontShin), legDrop(p.backThigh, p.backShin))
        hip = Vec(p.x, drop + SHOE + p.lift)
        val up = Vec(sin(rad(p.lean)), cos(rad(p.lean)))
        neck = hip + up * TORSO
        shoulder = hip + up * (TORSO - 0.04f)
        head = hip + up * (TORSO + 0.11f)

        frontElbow = shoulder + down(p.lean + p.frontArm) * UPPER_ARM
        frontHand = frontElbow + down(p.lean + p.frontArm + p.frontForearm) * FOREARM
        backElbow = shoulder + down(p.lean + p.backArm) * UPPER_ARM
        backHand = backElbow + down(p.lean + p.backArm + p.backForearm) * FOREARM

        frontKnee = hip + down(p.frontThigh) * THIGH
        frontFoot = frontKnee + down(p.frontThigh + p.frontShin) * SHIN
        backKnee = hip + down(p.backThigh) * THIGH
        backFoot = backKnee + down(p.backThigh + p.backShin) * SHIN
    }

    companion object {
        const val THIGH = 0.25f
        const val SHIN = 0.24f
        const val TORSO = 0.32f
        const val UPPER_ARM = 0.17f
        const val FOREARM = 0.16f
        const val HEAD_RADIUS = 0.085f
        const val SHOE = 0.025f

        /** Unit vector at [deg] from straight down, positive towards the opponent. */
        fun down(deg: Float) = Vec(sin(rad(deg)), -cos(rad(deg)))

        private fun legDrop(thigh: Float, shin: Float) =
            THIGH * cos(rad(thigh)) + SHIN * cos(rad(thigh + shin))

        private fun rad(deg: Float) = (deg * PI / 180).toFloat()
    }
}

/** Fists up, knees soft, leaning in a little. */
val Guard = Pose(
    lean = 8f,
    frontArm = 50f, frontForearm = 100f,
    backArm = 25f, backForearm = 115f,
    frontThigh = 22f, frontShin = -14f,
    backThigh = -18f, backShin = -10f,
)

/** One step of a move: reach [pose] at [time], easing in and out from the previous key. */
data class Key(val time: Float, val pose: Pose)

/**
 * A move as keyframes over time 0..[end]. [impact] is when a strike lands; the screen runs the
 * move to [impact], applies the hit, then runs on to [end].
 */
class Move(val keys: List<Key>, val impact: Float = keys.last().time) {
    val end: Float get() = keys.last().time

    fun at(time: Float): Pose {
        if (time <= keys.first().time) return keys.first().pose
        val i = keys.indexOfFirst { it.time >= time }
        if (i < 0) return keys.last().pose
        val a = keys[i - 1]
        val b = keys[i]
        val t = (time - a.time) / (b.time - a.time)
        return a.pose.lerp(b.pose, smoothstep(t))
    }

    private fun smoothstep(t: Float) = t * t * (3 - 2 * t)
}

/**
 * The moves. Strikes are built for opponents [DISTANCE] apart: the attacker dashes in far enough
 * for the fist or foot to reach the front of the defender's body.
 */
object Moves {
    /** Distance between the two fighters' starting points, in figure heights. */
    const val DISTANCE = 1.2f

    /** How far in front of a fighter's starting point their body starts. */
    private const val BODY_FRONT = 0.1f

    private val punchOut = Guard.copy(
        lean = 18f,
        frontArm = 72f, frontForearm = 0f,
        backArm = -15f, backForearm = 125f,
        frontThigh = 30f, frontShin = -8f,
        backThigh = -30f, backShin = 0f,
    )

    private val kickOut = Guard.copy(
        lean = -22f,
        frontArm = 20f, frontForearm = 120f,
        backArm = -30f, backForearm = 110f,
        frontThigh = 105f, frontShin = -10f,
        backThigh = -15f, backShin = 0f,
    )

    private fun dashToReach(pose: Pose, end: (Skeleton) -> Vec): Pose =
        pose.copy(x = DISTANCE - BODY_FRONT - end(Skeleton(pose)).x)

    /** The fist or foot at the moment of impact, relative to the attacker's starting point. */
    fun strikePoint(isKick: Boolean): Vec =
        if (isKick) Skeleton(kick.at(kick.impact)).frontFoot else Skeleton(punch.at(punch.impact)).frontHand

    val punch: Move = dashToReach(punchOut) { it.frontHand }.let { out ->
        Move(
            listOf(
                Key(0f, Guard),
                Key(0.45f, Guard.copy(x = 0.1f, lean = 2f, frontArm = 40f, frontForearm = 125f)),
                Key(1f, out),
                Key(1.3f, out),
                Key(2f, Guard),
            ),
            impact = 1f,
        )
    }

    val kick: Move = dashToReach(kickOut) { it.frontFoot }.let { out ->
        Move(
            listOf(
                Key(0f, Guard),
                Key(0.5f, Guard.copy(x = 0.15f, lean = -5f, frontThigh = 85f, frontShin = -110f)),
                Key(1f, out),
                Key(1.3f, out),
                Key(2f, Guard),
            ),
            impact = 1f,
        )
    }

    private val reeling = Pose(
        x = -0.4f, lift = 0.03f, lean = -28f,
        frontArm = 30f, frontForearm = 40f,
        backArm = -50f, backForearm = 30f,
        frontThigh = 35f, frontShin = -5f,
        backThigh = -25f, backShin = -20f,
    )

    /** Knocked back, then steps back into place. */
    val hit = Move(
        listOf(
            Key(0f, Guard),
            Key(0.15f, reeling.copy(x = -0.25f)),
            Key(0.5f, reeling.copy(lean = -18f, lift = 0f)),
            Key(1f, Guard),
        ),
    )

    /** Knocked off their feet and stays down. */
    val knockout = Move(
        listOf(
            Key(0f, Guard),
            Key(0.15f, reeling.copy(x = -0.3f, lift = 0.1f)),
            Key(
                0.6f,
                Pose(
                    x = -0.75f, fall = 1f,
                    frontArm = 150f, frontForearm = 10f, backArm = 120f, backForearm = 20f,
                    frontThigh = 8f, backThigh = -4f,
                ),
            ),
            Key(
                1f,
                Pose(
                    x = -0.8f, fall = 1f,
                    frontArm = 160f, frontForearm = 0f, backArm = 130f, backForearm = 10f,
                    frontThigh = 4f, backThigh = -2f,
                ),
            ),
        ),
    )

    /** Sways back out of reach. */
    val dodge = Move(
        listOf(
            Key(0f, Guard),
            Key(0.4f, Guard.copy(x = -0.2f, lift = 0.02f, lean = -25f, frontThigh = 30f, backThigh = -30f)),
            Key(1f, Guard),
        ),
    )

    /** Fist in the air. */
    val victory = Move(
        listOf(
            Key(0f, Guard),
            Key(
                1f,
                Pose(
                    frontArm = 170f, frontForearm = 0f, backArm = 20f, backForearm = 120f,
                    frontThigh = 10f, backThigh = -10f,
                ),
            ),
        ),
    )

    /** Breathing in the guard stance; [phase] 0..1 loops. Bobs by bending the knees. */
    fun idle(phase: Float): Pose {
        val s = (1 - cos(phase * 2 * PI).toFloat()) / 2
        return Guard.copy(
            frontThigh = Guard.frontThigh + 6 * s, frontShin = Guard.frontShin - 12 * s,
            backThigh = Guard.backThigh - 4 * s, backShin = Guard.backShin - 10 * s,
            frontForearm = Guard.frontForearm + 4 * s,
        )
    }
}
