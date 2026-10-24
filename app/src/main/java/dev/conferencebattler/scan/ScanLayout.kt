package dev.conferencebattler.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.conferencebattler.ui.screenPadding

/**
 * Shared layout for the scan screens: camera preview on the left, a controls [panel] on the
 * right, both kept clear of the system bars. Asks for the camera permission first if needed.
 */
@Composable
fun ScanLayout(
    title: String,
    analyzer: ImageAnalysis.Analyzer,
    onCancel: () -> Unit,
    panel: @Composable ColumnScope.() -> Unit,
) {
    BackHandler(onBack = onCancel)
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
    }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.CAMERA) }

    if (!granted) {
        Column(
            Modifier.screenPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("The camera is needed to scan badges and QR codes.")
            Button({ launcher.launch(Manifest.permission.CAMERA) }) { Text("Allow camera") }
            OutlinedButton(onCancel) { Text("Cancel") }
        }
        return
    }

    Row(Modifier.fillMaxSize().background(Color.Black).safeDrawingPadding()) {
        CameraPreview(analyzer, Modifier.weight(1f).fillMaxHeight())
        Column(
            Modifier
                .width(300.dp)
                .fillMaxHeight()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
            panel()
        }
    }
}
