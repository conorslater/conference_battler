package dev.conferencebattler.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Points the camera at a badge and reads all of its text. The scan finishes by itself once
 * several frames in a row agree ([StableReading]); "Use this" accepts the current frame early.
 */
@Composable
fun BadgeScanScreen(title: String, onResult: (BadgeReading) -> Unit, onCancel: () -> Unit) {
    val currentOnResult by rememberUpdatedState(onResult)
    var current by remember { mutableStateOf<BadgeReading?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var done by remember { mutableStateOf(false) }
    val analyzer = remember {
        val stable = StableReading()
        BadgeAnalyzer { lines ->
            if (done) return@BadgeAnalyzer
            val reading = readBadge(lines)
            current = reading
            val accepted = stable.offer(reading)
            progress = stable.progress
            if (accepted != null) {
                done = true
                currentOnResult(accepted)
            }
        }
    }
    DisposableEffect(analyzer) { onDispose { analyzer.close() } }

    ScanLayout(title, analyzer, onCancel) {
        Text(
            current?.text ?: "Point the camera at a badge",
            Modifier.weight(1f),
            color = Color.White,
            fontFamily = FontFamily.Monospace,
        )
        LinearProgressIndicator({ progress }, Modifier.fillMaxWidth())
        Spacer(Modifier)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onCancel) { Text("Cancel", color = Color.White) }
            Button(
                onClick = { current?.let { done = true; currentOnResult(it) } },
                enabled = current != null,
            ) { Text("Use this") }
        }
    }
}
