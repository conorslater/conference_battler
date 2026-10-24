package dev.conferencebattler.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.conferencebattler.gen.CharacterGenerator
import dev.conferencebattler.gen.ItemGenerator
import dev.conferencebattler.gen.Seeds
import dev.conferencebattler.model.Character
import dev.conferencebattler.model.Item
import dev.conferencebattler.scan.BadgeScanScreen

/** Scan or type badge text, or type a QR payload, and see what it generates. */
@Composable
fun DebugScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var badgeText by remember { mutableStateOf("") }
    var scannedName by remember { mutableStateOf<String?>(null) }
    var scanning by remember { mutableStateOf(false) }
    var payload by remember { mutableStateOf("") }
    if (scanning) {
        BadgeScanScreen(
            title = "Scan badge",
            onResult = { reading ->
                badgeText = reading.text
                scannedName = reading.name
                scanning = false
            },
            onCancel = { scanning = false },
        )
        return
    }
    Column(
        Modifier
            .screenPadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onBack) { Text("Back") }
            Button({ scanning = true }) { Text("Scan badge") }
        }
        OutlinedTextField(badgeText, { badgeText = it }, Modifier.fillMaxWidth(), label = { Text("Badge text") })
        if (badgeText.isNotBlank()) {
            StatsText(debugText(CharacterGenerator.generate(badgeText, scannedName ?: badgeText)))
            StatsText("hashed: ${Seeds.badgeKey(badgeText)}")
        }
        OutlinedTextField(payload, { payload = it }, Modifier.fillMaxWidth(), label = { Text("QR payload") })
        if (payload.isNotEmpty()) StatsText(debugText(ItemGenerator.generate(payload)))
    }
}

@Composable
private fun StatsText(text: String) {
    Text(text, fontFamily = FontFamily.Monospace)
}

private fun debugText(c: Character) = """
    ${c.title}
    ${c.tier.label} ${c.charClass.label}  seed=${c.seed.toString(16)}
    HP ${c.hp}  ST ${c.st}  DF ${c.df}
""".trimIndent()

private fun debugText(item: Item) =
    "${item.name} (${item.kind.label}, ${item.rarity.label}, lvl ${item.level})\nST +${item.stBonus}"
