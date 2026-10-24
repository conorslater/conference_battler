package dev.conferencebattler.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.conferencebattler.battle.BattleState
import dev.conferencebattler.battle.Fighter
import dev.conferencebattler.battle.Side

/**
 * Landscape battle screen, laid out like the sketch:
 *
 *     [ATTACK]  stats (P1)   (P2) stats  [ATTACK]
 *
 * Each player's button is on their own edge and only the active player's button is enabled.
 */
@Composable
fun BattleScreen(state: BattleState, onAttack: () -> Unit, onRematch: () -> Unit, onNewGame: () -> Unit) {
    BackHandler(onBack = onNewGame)
    Row(
        Modifier.screenPadding(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AttackButton(state, Side.LEFT, onAttack)
        Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                FighterPanel(state, Side.LEFT, Modifier.weight(1f))
                FighterPanel(state, Side.RIGHT, Modifier.weight(1f))
            }
            if (state.winner != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onRematch) { Text("Rematch") }
                    OutlinedButton(onNewGame) { Text("New game") }
                }
            }
        }
        AttackButton(state, Side.RIGHT, onAttack)
    }
}

@Composable
private fun AttackButton(state: BattleState, side: Side, onAttack: () -> Unit) {
    Column(
        Modifier.width(140.dp).fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(playerLabel(side.ordinal), style = MaterialTheme.typography.titleMedium)
        Button(
            onClick = onAttack,
            modifier = Modifier.fillMaxWidth().height(140.dp),
            enabled = state.winner == null && state.turn == side,
        ) { Text("ATTACK", fontWeight = FontWeight.Bold) }
    }
}

/** Name, figure and stats for one side. The stats sit on the outer side, next to the button. */
@Composable
private fun FighterPanel(state: BattleState, side: Side, modifier: Modifier) {
    val fighter = state.fighter(side)
    val c = fighter.character
    val active = state.winner == null && state.turn == side
    val dimmed = !active && state.winner != side
    Column(
        modifier.alpha(if (dimmed) 0.5f else 1f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            c.title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        Text(c.rank, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, maxLines = 1)
        Text(
            c.item?.let { "${it.name} (${it.kind.stat} +${it.bonus})" } ?: "Empty-handed",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (side == Side.LEFT) Stats(fighter)
            Box(contentAlignment = Alignment.Center) {
                Figure(side)
                HitMarker(state, side)
            }
            if (side == Side.RIGHT) Stats(fighter)
        }
        Text(statusText(state, side), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Stats(fighter: Fighter) {
    val c = fighter.character
    Column(Modifier.width(110.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("HP ${fighter.hp}", style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator({ fighter.hp.toFloat() / c.hp }, Modifier.fillMaxWidth())
        Text("ST ${c.totalSt}")
        Text("DF ${c.totalDf}")
    }
}

/** Circle head over a rectangle body, as in the sketch. Red for player 1, blue for player 2. */
@Composable
private fun Figure(side: Side) {
    val color = if (side == Side.LEFT) Color(0xFFC62828) else Color(0xFF1565C0)
    Canvas(Modifier.size(width = 70.dp, height = 110.dp)) {
        val head = size.width * 0.3f
        drawCircle(color, radius = head, center = Offset(size.width / 2, head))
        drawRect(
            color,
            topLeft = Offset(size.width * 0.15f, head * 2 + 6.dp.toPx()),
            size = Size(size.width * 0.7f, size.height - head * 2 - 6.dp.toPx()),
        )
    }
}

/** The last attack's damage (or MISS) over the defender, fading out. */
@Composable
private fun HitMarker(state: BattleState, side: Side) {
    val last = state.last ?: return
    if (last.attacker == side) return
    val fade = remember(state.turnNumber) { Animatable(1f) }
    LaunchedEffect(state.turnNumber) { fade.animateTo(0f, tween(durationMillis = 800, delayMillis = 700)) }
    val text = if (last.hit) "-${last.damage}" else "MISS"
    Text(
        text,
        Modifier.alpha(fade.value),
        color = if (last.hit) Color(0xFFFFD600) else Color.White,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
    )
}

/** One line under each fighter describing what just happened to or for them. */
private fun statusText(state: BattleState, side: Side): String {
    state.winner?.let { return if (it == side) "WINNER!" else "Defeated" }
    val last = state.last ?: return if (state.turn == side) "Goes first!" else ""
    if (last.attacker != side) return if (state.turn == side) "Your turn" else ""
    if (!last.hit) return "Missed!"
    return buildString {
        append("Hit for ${last.damage}")
        if (last.timingBonus) append(" ×2")
        if (last.critical) append("  CRITICAL!")
    }
}
