package dev.conferencebattler.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.conferencebattler.model.Character
import dev.conferencebattler.model.Item
import dev.conferencebattler.model.ItemKind

@Composable
fun TitleScreen(onStart: () -> Unit, onDebug: () -> Unit) {
    Column(
        Modifier.screenPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Conference Battler", style = MaterialTheme.typography.displaySmall)
        Text("Scan your badges. Scan a QR code for a weapon or spell. Fight!")
        Button(onStart) { Text("New game") }
        TextButton(onDebug) { Text("Generator test") }
    }
}

@Composable
fun CharacterCardScreen(
    player: Int,
    character: Character,
    onScanItem: () -> Unit,
    onSkip: () -> Unit,
    onRescan: () -> Unit,
) {
    BackHandler(onBack = onRescan)
    CardLayout(
        heading = "${playerLabel(player)}: ${character.title}",
        info = {
            Text(describe(character), style = MaterialTheme.typography.titleMedium)
            Text(statsLine(character), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            val kind = ItemKind.entries.first { it.usableBy == character.charClass }
            Text("Scan a QR code to equip a ${kind.label.lowercase()}, or skip.")
        },
        actions = {
            Button(onScanItem, Modifier.fillMaxWidth()) { Text("Scan QR code") }
            OutlinedButton(onSkip, Modifier.fillMaxWidth()) { Text("Skip") }
            TextButton(onRescan, Modifier.fillMaxWidth()) { Text("Rescan badge") }
        },
    )
}

@Composable
fun ItemCardScreen(
    player: Int,
    character: Character,
    item: Item,
    onEquip: () -> Unit,
    onScanAnother: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val usable = character.canEquip(item)
    CardLayout(
        heading = "${playerLabel(player)} found: ${item.name}",
        info = {
            Text(describe(item), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (usable) {
                Text(character.title, fontWeight = FontWeight.Bold)
                Text("With it: ${statsLine(character.equip(item))}", style = MaterialTheme.typography.headlineSmall)
            } else {
                Text(
                    "Only ${item.kind.usableBy.label.lowercase()}s can use a ${item.kind.label.lowercase()}. " +
                        "${character.title} is a ${character.charClass.label.lowercase()}.",
                )
            }
        },
        actions = {
            if (usable) Button(onEquip, Modifier.fillMaxWidth()) { Text("Equip") }
            OutlinedButton(onScanAnother, Modifier.fillMaxWidth()) { Text("Scan another") }
            OutlinedButton(onSkip, Modifier.fillMaxWidth()) { Text("Skip") }
        },
    )
}

/** Landscape card: details on the left, a column of buttons on the right. */
@Composable
private fun CardLayout(
    heading: String,
    info: @Composable ColumnScope.() -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
) {
    Row(Modifier.screenPadding(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(heading, style = MaterialTheme.typography.headlineMedium)
            info()
        }
        Column(
            Modifier.width(220.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            content = actions,
        )
    }
}
