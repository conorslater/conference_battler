package dev.conferencebattler.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Scans a QR code. QR decoding is exact, so the first payload read is accepted. */
@Composable
fun QrScanScreen(title: String, onResult: (String) -> Unit, onSkip: () -> Unit, onCancel: () -> Unit) {
    val currentOnResult by rememberUpdatedState(onResult)
    val analyzer = remember {
        var done = false
        QrAnalyzer { payload ->
            if (!done) {
                done = true
                currentOnResult(payload)
            }
        }
    }
    DisposableEffect(analyzer) { onDispose { analyzer.close() } }

    ScanLayout(title, analyzer, onCancel) {
        Text(
            "Point the camera at any QR code. It becomes a piece of conference swag.",
            Modifier.weight(1f),
            color = Color.White,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onCancel) { Text("Back", color = Color.White) }
            OutlinedButton(onSkip) { Text("Skip", color = Color.White) }
        }
    }
}
