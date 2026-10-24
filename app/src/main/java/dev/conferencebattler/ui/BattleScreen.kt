package dev.conferencebattler.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.conferencebattler.battle.BattleState
import dev.conferencebattler.battle.Side
import dev.conferencebattler.battle.TurnResult
import dev.conferencebattler.ui.fighter.Move
import dev.conferencebattler.ui.fighter.Moves
import dev.conferencebattler.ui.fighter.Pose
import dev.conferencebattler.ui.fighter.Skeleton
import dev.conferencebattler.ui.fighter.Vec
import dev.conferencebattler.ui.fighter.drawFighter
import dev.conferencebattler.ui.fighter.lookFor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Landscape battle screen in the style of a 2D fighting game:
 *
 *     [P1 health bar]          KO          [P2 health bar]
 *     [ATTACK]     P1 ->  <- P2     [ATTACK]
 *
 * The engine resolves an attack at once; [BattleAnimation] then plays it out (strike, knockback,
 * health draining) and keeps both buttons disabled until it's done.
 */
@Composable
fun BattleScreen(state: BattleState, onAttack: () -> Unit, onRematch: () -> Unit, onNewGame: () -> Unit) {
    BackHandler(onBack = onNewGame)
    val anim = remember { BattleAnimation(state) }
    LaunchedEffect(state) { anim.play(state) }

    // The stage fills the screen, edge to edge; the bars and buttons sit on top of it.
    Box(Modifier.fillMaxSize()) {
        Stage(state, anim, Modifier.fillMaxSize())
        Column(Modifier.screenPadding(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                HealthBar(state, Side.LEFT, anim.actor(Side.LEFT), Modifier.weight(1f))
                Text(
                    "KO",
                    Modifier
                        .background(Color(0xFFB71C1C), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    color = Color(0xFFFFD600),
                    fontWeight = FontWeight.Black,
                )
                HealthBar(state, Side.RIGHT, anim.actor(Side.RIGHT), Modifier.weight(1f))
            }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AttackButton(state, Side.LEFT, enabled = !anim.busy, onAttack)
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    if (!anim.busy) Banner(state, onRematch, onNewGame, Modifier.align(Alignment.TopCenter))
                }
                AttackButton(state, Side.RIGHT, enabled = !anim.busy, onAttack)
            }
        }
    }
}

/** Room kept clear at each side of the stage for the attack buttons and the screen margin. */
private val SIDE_RESERVE = 150.dp

/** The floor's height above the bottom of the screen. */
private val FLOOR_MARGIN = 44.dp

private fun playerColor(side: Side) = if (side == Side.LEFT) Color(0xFFC62828) else Color(0xFF1565C0)

/** The health bar drains from the middle outwards, with a red trail that catches up. */
@Composable
private fun HealthBar(state: BattleState, side: Side, actor: Actor, modifier: Modifier) {
    val c = state.fighter(side).character
    val align = if (side == Side.LEFT) Alignment.Start else Alignment.End
    val textAlign = if (side == Side.LEFT) TextAlign.Start else TextAlign.End
    Column(modifier, horizontalAlignment = align) {
        Canvas(Modifier.fillMaxWidth().height(20.dp)) {
            drawRect(Color(0xFF3A0A0A))
            fun bar(hp: Float, color: Color) {
                val w = size.width * (hp / c.hp).coerceIn(0f, 1f)
                drawRect(color, Offset(if (side == Side.LEFT) 0f else size.width - w, 0f), Size(w, size.height))
            }
            bar(actor.chip.value, Color(0xFFE53935))
            bar(actor.hp.value, Color(0xFFFFD600))
            drawRect(playerColor(side), style = Stroke(3.dp.toPx()))
        }
        Text(
            c.title,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
        )
        Text(
            "${c.rank}  ·  HP ${actor.hp.value.roundToInt()}",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            textAlign = textAlign,
        )
        Text(
            "ST ${c.totalSt}  DF ${c.totalDf}  ·  " +
                (c.item?.let { "${it.name} (${it.kind.stat} +${it.bonus})" } ?: "Empty-handed"),
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
        )
    }
}

@Composable
private fun AttackButton(state: BattleState, side: Side, enabled: Boolean, onAttack: () -> Unit) {
    Column(
        Modifier.width(100.dp).fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(playerLabel(side.ordinal), color = Color.White, style = MaterialTheme.typography.titleMedium)
        Button(
            onClick = onAttack,
            modifier = Modifier.fillMaxWidth().height(120.dp),
            enabled = enabled && state.winner == null && state.turn == side,
            colors = ButtonDefaults.buttonColors(
                containerColor = playerColor(side),
                contentColor = Color.White,
                // The default disabled grey disappears against the dark hall.
                disabledContainerColor = playerColor(side).copy(alpha = 0.3f),
                disabledContentColor = Color.White.copy(alpha = 0.5f),
            ),
        ) { Text("ATTACK", fontWeight = FontWeight.Bold) }
    }
}

/** Whose turn it is and what just happened, or the result and the rematch buttons. */
@Composable
private fun Banner(state: BattleState, onRematch: () -> Unit, onNewGame: () -> Unit, modifier: Modifier) {
    Column(
        modifier.padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val winner = state.winner
        if (winner != null) {
            Text("K.O.!", color = Color(0xFFFFD600), fontSize = 40.sp, fontWeight = FontWeight.Black)
            Text("${playerLabel(winner.ordinal)} wins!", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onRematch) { Text("Rematch") }
                OutlinedButton(onNewGame, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                    Text("New game")
                }
            }
        } else {
            state.last?.let { Text(eventText(it), color = Color.White, style = MaterialTheme.typography.bodyMedium) }
            Text(
                playerLabel(state.turn.ordinal) + if (state.last == null) " goes first!" else "'s turn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (state.turn == Side.LEFT) Color(0xFFEF9A9A) else Color(0xFF90CAF9),
            )
        }
    }
}

private fun eventText(last: TurnResult): String {
    val who = playerLabel(last.attacker.ordinal)
    if (!last.hit) return "$who missed!"
    return buildString {
        append("$who hit for ${last.damage}")
        if (last.timingBonus) append(" ×2")
        if (last.critical) append("  CRITICAL!")
    }
}

/** The floor, both fighters facing each other, and the hit effects. */
@Composable
private fun Stage(state: BattleState, anim: BattleAnimation, modifier: Modifier) {
    val idle = rememberInfiniteTransition(label = "idle")
    val phase by idle.animateFloat(0f, 1f, infiniteRepeatable(tween(1100, easing = LinearEasing)), label = "phase")
    val left = state.left.character
    val right = state.right.character
    val looks = remember(left, right) { mapOf(Side.LEFT to lookFor(left), Side.RIGHT to lookFor(right)) }
    val text = rememberTextMeasurer()

    Canvas(modifier) {
        // Sized to the space between the buttons, so a knocked-out fighter lands clear of them.
        val h = min(size.height * 0.5f, (size.width - 2 * SIDE_RESERVE.toPx()) / 4.2f)
        val floor = size.height - FLOOR_MARGIN.toPx()
        drawHall(floor)

        val origins = mapOf(
            Side.LEFT to Offset(size.width / 2 - Moves.DISTANCE * h / 2, floor),
            Side.RIGHT to Offset(size.width / 2 + Moves.DISTANCE * h / 2, floor),
        )
        fun facing(side: Side) = if (side == Side.LEFT) 1f else -1f
        fun toCanvas(side: Side, v: Vec) =
            origins.getValue(side).let { Offset(it.x + facing(side) * v.x * h, it.y - v.y * h) }

        // The attacker goes on top so the punch lands in front of the defender.
        val front = state.last?.attacker ?: Side.LEFT
        for (side in listOf(front.other, front)) {
            val actor = anim.actor(side)
            val idlePhase = if (side == Side.LEFT) phase else (phase + 0.5f) % 1f
            drawFighter(looks.getValue(side), actor.pose(idlePhase), origins.getValue(side), h, facing(side), actor.flash.value)
        }

        val impact = anim.impact ?: return@Canvas
        val t = anim.impactTime.value
        if (impact.strike != null && t < 0.3f) {
            drawSpark(toCanvas(impact.attacker, impact.strike), h * (0.08f + 0.25f * t), alpha = 1 - t / 0.3f)
        }
        val head = Skeleton(anim.actor(impact.attacker.other).pose(0f)).head
        val at = toCanvas(impact.attacker.other, head + Vec(0f, 0.2f + 0.15f * t))
        val alpha = if (t < 0.6f) 1f else (1 - t) / 0.4f
        drawLabel(text, impact.label, at, impact.color.copy(alpha = alpha))
    }
}

/** An expo hall: a backdrop, a row of pull-up banners and carpet. */
private fun DrawScope.drawHall(floor: Float) {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF1A1F3A), Color(0xFF2E2350))))
    val banners = listOf(Color(0xFF00897B), Color(0xFFAD1457), Color(0xFFF9A825), Color(0xFF3949AB))
    banners.forEachIndexed { i, color ->
        val w = size.width * 0.08f
        val x = size.width * (0.1f + 0.8f * i / (banners.size - 1)) - w / 2
        val top = floor - size.height * 0.62f
        drawRect(color.copy(alpha = 0.35f), Offset(x, top), Size(w, floor - top))
        drawRect(Color.White.copy(alpha = 0.2f), Offset(x, top + w * 0.3f), Size(w, w * 0.25f))
    }
    drawRect(Color(0xFF4A1C2C), Offset(0f, floor), Size(size.width, size.height - floor))
    drawLine(Color(0xFF7A3048), Offset(0f, floor), Offset(size.width, floor), 2.dp.toPx())
}

private fun DrawScope.drawSpark(centre: Offset, radius: Float, alpha: Float) {
    fun star(outer: Float, inner: Float) = Path().apply {
        for (i in 0 until 16) {
            val r = if (i % 2 == 0) outer else inner
            val a = i * PI / 8
            val x = centre.x + r * cos(a).toFloat()
            val y = centre.y + r * sin(a).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(star(radius, radius * 0.45f), Color(0xFFFFD600).copy(alpha = alpha))
    drawPath(star(radius * 0.55f, radius * 0.25f), Color.White.copy(alpha = alpha))
}

private fun DrawScope.drawLabel(text: TextMeasurer, label: String, centre: Offset, color: Color) {
    val layout = text.measure(label, TextStyle(color = color, fontSize = 26.sp, fontWeight = FontWeight.Black))
    drawText(layout, topLeft = Offset(centre.x - layout.size.width / 2, centre.y - layout.size.height))
}

/** One fighter's animation state. With no [move] they breathe in the guard stance. */
private class Actor(hp: Int) {
    var move by mutableStateOf<Move?>(null)
    val time = Animatable(0f)
    /** The yellow bar, which drops on impact. */
    val hp = Animatable(hp.toFloat())
    /** The red trail behind it, which catches up a moment later. */
    val chip = Animatable(hp.toFloat())
    val flash = Animatable(0f)

    fun pose(idlePhase: Float): Pose = move?.at(time.value) ?: Moves.idle(idlePhase)

    suspend fun perform(move: Move, durationMillis: Int, until: Float = move.end) {
        this.move = move
        time.snapTo(0f)
        time.animateTo(until, tween(durationMillis, easing = LinearEasing))
    }

    suspend fun reset(hp: Int) {
        move = null
        this.hp.snapTo(hp.toFloat())
        chip.snapTo(hp.toFloat())
        flash.snapTo(0f)
    }
}

/**
 * @param strike where the fist or foot landed, relative to the attacker; null for a miss.
 */
private class Impact(val attacker: Side, val strike: Vec?, val label: String, val color: Color)

/** Plays each [TurnResult] out on screen, then lets the next player attack. */
private class BattleAnimation(start: BattleState) {
    private val left = Actor(start.left.hp)
    private val right = Actor(start.right.hp)

    /** True while an attack is playing; the attack buttons wait for it. */
    var busy by mutableStateOf(false)
    var impact by mutableStateOf<Impact?>(null)
    val impactTime = Animatable(0f)

    fun actor(side: Side) = if (side == Side.LEFT) left else right

    suspend fun play(state: BattleState) {
        val last = state.last
        if (last == null) {
            // A new battle or a rematch.
            busy = false
            impact = null
            left.reset(state.left.hp)
            right.reset(state.right.hp)
            return
        }
        busy = true
        val attacker = actor(last.attacker)
        val defender = actor(last.attacker.other)
        val hpLeft = state.fighter(last.attacker.other).hp.toFloat()
        // Each player alternates punch, kick, punch...; the ×2 bonus always gets the big kick.
        val kick = last.timingBonus || ((state.turnNumber - 2) / 2) % 2 == 1
        val move = if (kick) Moves.kick else Moves.punch

        coroutineScope {
            if (!last.hit) launch { defender.perform(Moves.dodge, durationMillis = 550) }
            attacker.perform(move, durationMillis = STRIKE_MILLIS, until = move.impact)

            launch { attacker.time.animateTo(move.end, tween(RECOVER_MILLIS, easing = LinearEasing)) }
            impact = if (last.hit) {
                Impact(
                    last.attacker,
                    Moves.strikePoint(kick),
                    "-${last.damage}" + if (last.critical) "  CRITICAL!" else "",
                    if (last.critical) Color(0xFFFF7043) else Color(0xFFFFD600),
                )
            } else {
                Impact(last.attacker, null, "MISS", Color.White)
            }
            launch {
                impactTime.snapTo(0f)
                impactTime.animateTo(1f, tween(900, easing = LinearEasing))
            }
            if (last.hit) {
                launch {
                    defender.flash.snapTo(1f)
                    defender.flash.animateTo(0f, tween(250))
                }
                launch { defender.hp.animateTo(hpLeft, tween(250)) }
                launch { defender.chip.animateTo(hpLeft, tween(500, delayMillis = 450)) }
                if (state.winner != null) {
                    defender.perform(Moves.knockout, durationMillis = 900)
                } else {
                    defender.perform(Moves.hit, durationMillis = 550)
                }
            }
        }

        if (state.winner == null) {
            attacker.move = null
            defender.move = null
        } else {
            attacker.perform(Moves.victory, durationMillis = 400)
        }
        busy = false
    }

    companion object {
        const val STRIKE_MILLIS = 280
        const val RECOVER_MILLIS = 450
    }
}
