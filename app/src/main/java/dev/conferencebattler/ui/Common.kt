package dev.conferencebattler.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.conferencebattler.model.Character
import dev.conferencebattler.model.Item

/**
 * Keeps content clear of the status bar, navigation bar and camera cutout, plus a generous
 * margin so buttons near the edge are easy to press. The app draws edge to edge (forced from
 * Android 15), so every screen needs this.
 */
fun Modifier.screenPadding(): Modifier =
    fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp, vertical = 32.dp)

fun playerLabel(player: Int) = "Player ${player + 1}"

fun statsLine(c: Character) = "HP ${c.hp}   ST ${c.totalSt}   DF ${c.df}"

fun describe(c: Character) = "${c.tier.label} ${c.charClass.label}"

fun describe(item: Item) = "${item.rarity.label} ${item.kind.label}  ·  ST +${item.stBonus}"
