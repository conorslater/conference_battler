package dev.conferencebattler.ui.fighter

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import dev.conferencebattler.ui.fighter.Skeleton.Companion.HEAD_RADIUS
import dev.conferencebattler.ui.fighter.Skeleton.Companion.SHOE
import dev.conferencebattler.ui.fighter.Skeleton.Companion.TORSO
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max

private const val LEG_WIDTH = 0.075f
private const val ARM_WIDTH = 0.06f
internal const val TORSO_WIDTH = 0.17f
private const val HAND_RADIUS = 0.034f

/**
 * Draws shapes in figure coordinates: [Vec]s in figure heights, x towards the opponent, y up from
 * [origin]. [flash] whitens everything drawn, for the frame a fighter gets hit.
 */
internal class FigurePen(
    private val scope: DrawScope,
    private val origin: Offset,
    private val height: Float,
    private val facing: Float,
    private val flash: Float,
) {
    fun p(v: Vec) = Offset(origin.x + facing * v.x * height, origin.y - v.y * height)
    fun c(color: Color) = if (flash > 0f) lerp(color, Color.White, flash * 0.7f) else color

    fun line(a: Vec, b: Vec, color: Color, width: Float) =
        scope.drawLine(c(color), p(a), p(b), width * height, StrokeCap.Round)

    fun dot(v: Vec, color: Color, radius: Float) = scope.drawCircle(c(color), radius * height, p(v))

    fun ring(v: Vec, color: Color, radius: Float, width: Float) =
        scope.drawCircle(c(color), radius * height, p(v), style = Stroke(width * height))

    fun polygon(color: Color, vararg points: Vec) = polygon(color, points.asList())

    fun polygon(color: Color, points: List<Vec>) {
        val path = Path()
        points.forEachIndexed { i, v -> p(v).let { if (i == 0) path.moveTo(it.x, it.y) else path.lineTo(it.x, it.y) } }
        path.close()
        scope.drawPath(path, c(color))
    }

    /** A rectangle around [centre], [halfHeight] along [up] and [halfWidth] across it. */
    fun box(centre: Vec, up: Vec, halfWidth: Float, halfHeight: Float, color: Color) {
        val u = up * halfHeight
        val a = up.across * halfWidth
        polygon(color, centre + u - a, centre + u + a, centre - u + a, centre - u - a)
    }

    /** A half-circle cap over [centre], bulging along [up]. */
    fun dome(centre: Vec, up: Vec, radius: Float, color: Color) {
        val r = radius * height
        val o = p(centre)
        // Screen y points down, so the angle is mirrored when the fighter faces left.
        val angle = Math.toDegrees(atan2(-up.y.toDouble(), facing * up.x.toDouble())).toFloat()
        scope.drawArc(c(color), angle - 90f, 180f, true, Offset(o.x - r, o.y - r), Size(2 * r, 2 * r))
    }
}

/** Where the name badge hangs, for a torso with axes [up] and [fwd]. */
internal fun badgeCentre(sk: Skeleton, up: Vec, fwd: Vec) = sk.neck - up * 0.13f + fwd * 0.055f

/**
 * Draws a side-on fighter in [look], posed as [sk], with their [Look.gear] worn or held.
 *
 * @param origin the fighter's starting point on the floor, in pixels.
 * @param height one figure height in pixels.
 * @param facing 1 to face right, -1 to face left.
 * @param flash 0..1 whitens the figure, for the frame they get hit.
 */
fun DrawScope.drawFighter(look: Look, sk: Skeleton, origin: Offset, height: Float, facing: Float, flash: Float = 0f) {
    val pen = FigurePen(this, origin, height, facing, flash)
    val gear = look.gear
    val dressed = gear?.let { wearing(look, it) } ?: look

    // Shadow stays on the floor below the hip, shrinking while the fighter is in the air.
    val floor = pen.p(Vec(sk.hip.x, 0f))
    val airborne = sk.joints().minOf { it.y } - SHOE
    val s = 1f / (1f + 1.5f * max(0f, airborne))
    drawOval(
        Color.Black.copy(alpha = 0.3f * s),
        topLeft = Offset(floor.x - 0.28f * s * height, floor.y - 0.03f * s * height),
        size = Size(0.56f * s * height, 0.06f * s * height),
    )

    // Torso axes: up along the spine, forward across it.
    val up = (sk.neck - sk.hip) * (1 / TORSO)
    val fwd = up.across

    with(pen) { drawBody(dressed, sk, up, fwd) }
}

private fun FigurePen.drawBody(look: Look, sk: Skeleton, up: Vec, fwd: Vec) {
    val bareForearms = look.outfit == Outfit.TEE || look.outfit == Outfit.POLO
    val gear = look.gear

    fun arm(shoulder: Vec, elbow: Vec, hand: Vec, shade: Float) {
        line(shoulder, elbow, look.top.shade(shade), ARM_WIDTH)
        line(elbow, hand, (if (bareForearms) look.skin else look.top).shade(shade), ARM_WIDTH * 0.9f)
        dot(hand, look.skin.shade(shade), HAND_RADIUS)
    }

    fun leg(knee: Vec, foot: Vec, shade: Float) {
        line(sk.hip, knee, look.trousers.shade(shade), LEG_WIDTH)
        line(knee, foot, look.trousers.shade(shade), LEG_WIDTH * 0.9f)
        val toe = ((foot - knee) * (1 / Skeleton.SHIN)).normal
        line(foot - toe * 0.02f, foot + toe * 0.06f, look.shoes.shade(shade), 0.05f)
    }

    // Far side first, so the near side overlaps it.
    arm(sk.shoulder, sk.backElbow, sk.backHand, shade = 0.75f)
    leg(sk.backKnee, sk.backFoot, shade = 0.75f)
    if (look.extra == Extra.BACKPACK) {
        val top = sk.neck - up * 0.04f - fwd * 0.1f
        polygon(Color(0xFF455A64), top, top - fwd * 0.08f, top - fwd * 0.08f - up * 0.2f, top - up * 0.2f)
    }
    if (look.hair == Hair.LONG) line(sk.head - fwd * 0.05f, sk.head - fwd * 0.06f - up * 0.14f, look.hairColor, 0.07f)
    leg(sk.frontKnee, sk.frontFoot, shade = 1f)
    dot(sk.hip, look.trousers, 0.075f)
    line(sk.hip + up * 0.04f, sk.neck, look.top, TORSO_WIDTH)

    when (look.outfit) {
        Outfit.HOODIE -> dot(sk.neck - fwd * 0.06f + up * 0.02f, look.top.shade(0.85f), 0.07f)
        Outfit.POLO -> line(sk.neck - fwd * 0.04f, sk.neck + fwd * 0.06f, look.top.shade(0.8f), 0.03f)
        Outfit.TURTLENECK -> line(sk.neck, sk.neck + up * 0.05f, look.top, 0.07f)
        Outfit.BLAZER -> line(sk.neck + fwd * 0.05f, sk.neck - up * 0.2f + fwd * 0.06f, look.accent, 0.04f)
        Outfit.SHIRT_AND_TIE -> line(sk.neck + fwd * 0.07f, sk.neck - up * 0.17f + fwd * 0.075f, look.accent, 0.025f)
        Outfit.TEE, Outfit.SHIRT -> Unit
    }
    gear?.let { drawGear(it, Layer.BODY, sk, up, fwd) }

    // Lanyard and name badge.
    val badge = badgeCentre(sk, up, fwd)
    val bw = fwd * 0.032f
    val bh = up * 0.042f
    line(sk.neck - fwd * 0.03f, badge + bh - bw, look.badgeStripe, 0.012f)
    line(sk.neck + fwd * 0.03f, badge + bh + bw, look.badgeStripe, 0.012f)
    polygon(Color.White, badge + bh - bw, badge + bh + bw, badge - bh + bw, badge - bh - bw)
    polygon(look.badgeStripe, badge + bh - bw, badge + bh + bw, badge + bh * 0.4f + bw, badge + bh * 0.4f - bw)
    gear?.let { drawGear(it, Layer.CHEST, sk, up, fwd) }

    // Head: the hair is a slightly bigger circle behind the face, showing at the top and back.
    val face = if (look.hair == Hair.BALD) sk.head else sk.head + fwd * 0.008f - up * 0.01f
    when (look.hair) {
        Hair.BALD -> Unit
        Hair.CURLY -> for (i in -2..2) {
            dot(sk.head + up * 0.05f - fwd * (0.02f + 0.02f * i) + up * (0.01f * (2 - abs(i))), look.hairColor, 0.04f)
        }
        else -> dot(sk.head + up * 0.02f - fwd * 0.02f, look.hairColor, HEAD_RADIUS * 1.1f)
    }
    if (look.hair == Hair.BUN) dot(sk.head - fwd * 0.06f + up * 0.07f, look.hairColor, 0.038f)
    dot(face, look.skin, HEAD_RADIUS)
    val eye = sk.head + fwd * 0.05f + up * 0.012f
    dot(eye, Color(0xFF1B1B1B), 0.012f)
    if (look.glasses) {
        ring(eye, Color(0xFF1B1B1B), 0.024f, 0.008f)
        line(eye - fwd * 0.024f, sk.head - fwd * 0.03f, Color(0xFF1B1B1B), 0.008f)
    }
    when (look.extra) {
        Extra.CAP -> {
            val cap = look.top.shade(0.7f)
            dome(sk.head + up * 0.01f, up, HEAD_RADIUS * 1.05f, cap)
            line(sk.head + up * 0.015f + fwd * 0.03f, sk.head + up * 0.015f + fwd * 0.13f, cap, 0.022f)
        }
        Extra.HEADSET -> {
            val mic = sk.head + fwd * 0.07f - up * 0.045f
            line(sk.head - fwd * 0.02f, mic, Color(0xFF424242), 0.01f)
            dot(mic, Color(0xFF424242), 0.014f)
        }
        Extra.NONE, Extra.BACKPACK -> Unit
    }
    gear?.let { drawGear(it, Layer.HEAD, sk, up, fwd) }

    arm(sk.shoulder, sk.frontElbow, sk.frontHand, shade = 1f)
    if (gear != null && gear.prop.layer == Layer.HAND) {
        drawGear(gear, Layer.HAND, sk, up, fwd)
        // The fist closes over the handle.
        dot(sk.frontHand, look.skin, HAND_RADIUS)
    }
}

internal fun Color.shade(k: Float): Color = if (k == 1f) this else Color(red * k, green * k, blue * k, alpha)
