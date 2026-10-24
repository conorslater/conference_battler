package dev.conferencebattler.ui.fighter

import dev.conferencebattler.ui.fighter.Skeleton.Companion.HEAD_RADIUS
import kotlin.math.PI
import kotlin.math.atan2
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
) {
    fun lerp(to: Pose, t: Float): Pose {
        fun l(a: Float, b: Float) = a + (b - a) * t
        return Pose(
            l(x, to.x), l(lift, to.lift), l(lean, to.lean),
            l(frontArm, to.frontArm), l(frontForearm, to.frontForearm),
            l(backArm, to.backArm), l(backForearm, to.backForearm),
            l(frontThigh, to.frontThigh), l(frontShin, to.frontShin),
            l(backThigh, to.backThigh), l(backShin, to.backShin),
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

    val length: Float get() = sqrt(x * x + y * y)

    fun unit(): Vec = length.let { if (it == 0f) this else Vec(x / it, y / it) }

    /** Rotated [deg] anticlockwise. */
    fun rotate(deg: Float): Vec {
        val r = (deg * PI / 180).toFloat()
        return Vec(x * cos(r) - y * sin(r), x * sin(r) + y * cos(r))
    }
}

/**
 * Joint positions in figure heights. x points towards the opponent and y points up, with the
 * feet's starting point at the origin. Build one from a [Pose], or from loose joints for a
 * [Ragdoll]; the shoulder and head sit on the line through the hip and neck either way.
 */
class Skeleton(
    val hip: Vec,
    val neck: Vec,
    val frontElbow: Vec,
    val frontHand: Vec,
    val backElbow: Vec,
    val backHand: Vec,
    val frontKnee: Vec,
    val frontFoot: Vec,
    val backKnee: Vec,
    val backFoot: Vec,
) {
    val shoulder: Vec get() = alongSpine(TORSO - 0.04f)
    val head: Vec get() = alongSpine(HEAD_REACH)

    private fun alongSpine(d: Float) = hip + (neck - hip) * (d / TORSO)

    /** The joints in constructor order. */
    fun joints() = listOf(hip, neck, frontElbow, frontHand, backElbow, backHand, frontKnee, frontFoot, backKnee, backFoot)

    /**
     * Part way to [to]. Each bone turns through the smaller angle at its own length, so limbs
     * swing rather than shrink, which lets a fighter roll up off their back.
     */
    fun lerp(to: Skeleton, t: Float): Skeleton {
        fun swing(from: Vec, toward: Vec, start: Vec, length: Float): Vec {
            val a = atan2(from.y, from.x)
            var d = atan2(toward.y, toward.x) - a
            if (d > PI) d -= (2 * PI).toFloat()
            if (d < -PI) d += (2 * PI).toFloat()
            val r = a + d * t
            return start + Vec(cos(r), sin(r)) * length
        }
        val h = hip + (to.hip - hip) * t
        val n = swing(neck - hip, to.neck - to.hip, h, TORSO)
        val s = h + (n - h) * ((TORSO - 0.04f) / TORSO)
        val fe = swing(frontElbow - shoulder, to.frontElbow - to.shoulder, s, UPPER_ARM)
        val be = swing(backElbow - shoulder, to.backElbow - to.shoulder, s, UPPER_ARM)
        val fk = swing(frontKnee - hip, to.frontKnee - to.hip, h, THIGH)
        val bk = swing(backKnee - hip, to.backKnee - to.hip, h, THIGH)
        return Skeleton(
            h, n,
            fe, swing(frontHand - frontElbow, to.frontHand - to.frontElbow, fe, FOREARM),
            be, swing(backHand - backElbow, to.backHand - to.backElbow, be, FOREARM),
            fk, swing(frontFoot - frontKnee, to.frontFoot - to.frontKnee, fk, SHIN),
            bk, swing(backFoot - backKnee, to.backFoot - to.backKnee, bk, SHIN),
        )
    }

    companion object {
        const val THIGH = 0.25f
        const val SHIN = 0.24f
        const val TORSO = 0.32f
        const val UPPER_ARM = 0.17f
        const val FOREARM = 0.16f
        const val HEAD_RADIUS = 0.085f
        const val SHOE = 0.025f

        /** From the hip to the centre of the head. */
        const val HEAD_REACH = TORSO + 0.11f

        operator fun invoke(p: Pose): Skeleton {
            // Stand the hip on whichever leg reaches lower, so the feet stay on the floor.
            val drop = max(legDrop(p.frontThigh, p.frontShin), legDrop(p.backThigh, p.backShin))
            val hip = Vec(p.x, drop + SHOE + p.lift)
            val up = Vec(sin(rad(p.lean)), cos(rad(p.lean)))
            val shoulder = hip + up * (TORSO - 0.04f)

            val frontElbow = shoulder + down(p.lean + p.frontArm) * UPPER_ARM
            val backElbow = shoulder + down(p.lean + p.backArm) * UPPER_ARM
            val frontKnee = hip + down(p.frontThigh) * THIGH
            val backKnee = hip + down(p.backThigh) * THIGH
            return Skeleton(
                hip, hip + up * TORSO,
                frontElbow, frontElbow + down(p.lean + p.frontArm + p.frontForearm) * FOREARM,
                backElbow, backElbow + down(p.lean + p.backArm + p.backForearm) * FOREARM,
                frontKnee, frontKnee + down(p.frontThigh + p.frontShin) * SHIN,
                backKnee, backKnee + down(p.backThigh + p.backShin) * SHIN,
            )
        }

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

/** One knee down, a hand on the other, pushing up off the floor. */
val Kneel = Pose(
    lean = 25f,
    frontArm = 35f, frontForearm = 30f,
    backArm = -10f, backForearm = 20f,
    frontThigh = 80f, frontShin = -80f,
    backThigh = 0f, backShin = -95f,
)

/** Anything that poses a fighter over time 0..[end]. */
interface Motion {
    val end: Float
    fun at(time: Float): Skeleton
}

internal fun smoothstep(t: Float) = t * t * (3 - 2 * t)

/** One step of a move: reach [pose] at [time], easing in and out from the previous key. */
data class Key(val time: Float, val pose: Pose)

/**
 * A move as keyframes over time 0..[end]. [impact] is when a strike lands; the screen runs the
 * move to [impact], applies the hit, then runs on to [end].
 */
class Move(val keys: List<Key>, val impact: Float = keys.last().time) : Motion {
    override val end: Float get() = keys.last().time

    override fun at(time: Float) = Skeleton(pose(time))

    fun pose(time: Float): Pose {
        if (time <= keys.first().time) return keys.first().pose
        val i = keys.indexOfFirst { it.time >= time }
        if (i < 0) return keys.last().pose
        val a = keys[i - 1]
        val b = keys[i]
        val t = (time - a.time) / (b.time - a.time)
        return a.pose.lerp(b.pose, smoothstep(t))
    }
}

/** Keyframed [Skeleton]s, for motions that don't start from a [Pose], like getting up off the floor. */
class Blend(private val keys: List<Pair<Float, Skeleton>>) : Motion {
    override val end: Float get() = keys.last().first

    override fun at(time: Float): Skeleton {
        val i = keys.indexOfFirst { it.first >= time }
        if (i == 0) return keys.first().second
        if (i < 0) return keys.last().second
        val (ta, a) = keys[i - 1]
        val (tb, b) = keys[i]
        return a.lerp(b, smoothstep((time - ta) / (tb - ta)))
    }
}

/**
 * A blow that lands: [point] is the fist or foot at impact, relative to the attacker's starting
 * point. A finisher has a [launch] that sends the defender flying.
 */
class Strike(val move: Move, val point: Vec, val launch: Launch? = null)

/**
 * The moves. Strikes are built for opponents [DISTANCE] apart: the attacker reaches far enough
 * for the fist or foot to touch the front of the defender's body or head.
 */
object Moves {
    /** Distance between the two fighters' starting points, in figure heights. */
    const val DISTANCE = 1.2f

    /** How far in front of a fighter's starting point their body starts. */
    const val BODY_FRONT = 0.1f

    /** How far in front of a fighter's starting point their face is, in the guard stance. */
    val HEAD_FRONT = Skeleton(Guard).head.x + HEAD_RADIUS

    /** Where the attacker stands to throw a combo. */
    val close = Guard.copy(x = 0.5f)

    /** Steps in from the starting point to throw a combo. */
    val stepIn = Move(listOf(Key(0f, Guard), Key(1f, close)))

    /**
     * Winds up from [from] and strikes with [limb], leaning in so it reaches [front] (in front of
     * the defender's starting point), then holds [follow] and settles into [to].
     */
    private fun strike(
        from: Pose,
        windUp: Pose,
        out: Pose,
        to: Pose,
        front: Float,
        limb: (Skeleton) -> Vec,
        follow: Pose? = null,
        launch: Launch? = null,
    ): Strike {
        val reached = out.copy(x = DISTANCE - front - limb(Skeleton(out)).x)
        val keys = if (follow == null) {
            listOf(
                Key(0f, from),
                Key(0.45f, windUp.copy(x = from.x + 0.05f)),
                Key(1f, reached),
                Key(1.25f, reached),
                Key(2f, to),
            )
        } else {
            val held = follow.copy(x = reached.x + 0.05f)
            listOf(
                Key(0f, from),
                Key(0.55f, windUp.copy(x = from.x + 0.05f)),
                Key(1f, reached),
                Key(1.35f, held),
                Key(1.9f, held),
                Key(2.6f, to),
            )
        }
        return Strike(Move(keys, impact = 1f), limb(Skeleton(reached)), launch)
    }

    /** Straight punch to the body. */
    val jab = strike(
        close,
        windUp = Guard.copy(lean = 2f, frontArm = 40f, frontForearm = 125f),
        out = Guard.copy(
            lean = 18f,
            frontArm = 72f, frontForearm = 0f,
            backArm = -15f, backForearm = 125f,
            frontThigh = 30f, frontShin = -8f,
            backThigh = -30f, backShin = 0f,
        ),
        to = close,
        front = BODY_FRONT,
        limb = { it.frontHand },
    )

    /** Open hand across the face. */
    val slap = strike(
        close,
        windUp = Guard.copy(lean = 0f, frontArm = -20f, frontForearm = 110f, backArm = 40f),
        out = Guard.copy(
            lean = 10f,
            frontArm = 115f, frontForearm = -15f,
            backArm = -20f, backForearm = 110f,
            frontThigh = 30f, frontShin = -8f,
            backThigh = -30f, backShin = 0f,
        ),
        to = close,
        front = HEAD_FRONT,
        limb = { it.frontHand },
    )

    /** Front kick to the stomach. */
    val bodyKick = strike(
        close,
        windUp = Guard.copy(lean = -5f, frontThigh = 85f, frontShin = -110f),
        out = Guard.copy(
            lean = -22f,
            frontArm = 20f, frontForearm = 120f,
            backArm = -30f, backForearm = 110f,
            frontThigh = 105f, frontShin = -10f,
            backThigh = -15f, backShin = 0f,
        ),
        to = close,
        front = BODY_FRONT,
        limb = { it.frontFoot },
    )

    /** The quick blows a combo is made of, in the order they're thrown. */
    val combo = listOf(jab, slap, bodyKick)

    /** Crouches, then springs up fist first under the chin. Throws the defender up and over. */
    val uppercut = strike(
        close,
        windUp = Guard.copy(
            lean = 25f,
            frontArm = 10f, frontForearm = 130f,
            frontThigh = 50f, frontShin = -60f,
            backThigh = -20f, backShin = -50f,
        ),
        out = Guard.copy(
            lift = 0.05f, lean = -10f,
            frontArm = 125f, frontForearm = -5f,
            backArm = -20f, backForearm = 110f,
            frontThigh = 20f, frontShin = 0f,
            backThigh = -25f, backShin = 0f,
        ),
        to = Guard,
        front = HEAD_FRONT,
        limb = { it.frontHand },
        follow = Guard.copy(
            lift = 0.1f, lean = -15f,
            frontArm = 170f, frontForearm = 0f,
            backArm = -25f, backForearm = 100f,
            frontThigh = 15f, frontShin = -5f,
            backThigh = -20f, backShin = -10f,
        ),
        launch = Launch(Vec(-0.9f, 3.0f), spin = 2.4f),
    )

    /** Leans right back and swings the front foot into the side of the head. */
    val roundhouse = strike(
        close,
        windUp = Guard.copy(lean = -5f, lift = 0.02f, frontThigh = 60f, frontShin = -120f, backArm = 60f),
        out = Guard.copy(
            lean = -40f,
            frontArm = -40f, frontForearm = 60f,
            backArm = 60f, backForearm = 40f,
            frontThigh = 145f, frontShin = -5f,
            backThigh = -10f, backShin = 0f,
        ),
        to = Guard,
        front = HEAD_FRONT,
        limb = { it.frontFoot },
        follow = Guard.copy(
            lean = -30f,
            frontArm = -50f, frontForearm = 50f,
            backArm = 70f, backForearm = 30f,
            frontThigh = 165f, frontShin = -10f,
            backThigh = -10f, backShin = 0f,
        ),
        launch = Launch(Vec(-1.6f, 2.1f), spin = 3.4f),
    )

    private val reeling = Pose(
        x = -0.4f, lift = 0.03f, lean = -28f,
        frontArm = 30f, frontForearm = 40f,
        backArm = -50f, backForearm = 30f,
        frontThigh = 35f, frontShin = -5f,
        backThigh = -25f, backShin = -20f,
    )

    /** Rocked back by a quick blow, then straight back into guard. */
    val flinch = Move(
        listOf(
            Key(0f, Guard),
            Key(0.2f, reeling.copy(x = -0.12f, lean = -20f, lift = 0f)),
            Key(1f, Guard),
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

    /**
     * From lying where a [Ragdoll] left them: a moment dazed, up onto one knee, stand, and step
     * back to the starting point. Time is in seconds.
     */
    fun getUp(down: Skeleton): Motion {
        val x = down.hip.x
        return Blend(
            listOf(
                0f to down,
                0.35f to down,
                0.8f to Skeleton(Kneel.copy(x = x)),
                1.15f to Skeleton(Guard.copy(x = x)),
                1.4f to Skeleton(Guard.copy(x = x / 2, lift = 0.04f, frontThigh = 5f, backThigh = -5f)),
                1.65f to Skeleton(Guard),
            ),
        )
    }

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
