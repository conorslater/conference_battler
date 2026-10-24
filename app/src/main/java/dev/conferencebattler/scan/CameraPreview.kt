package dev.conferencebattler.scan

import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Back camera preview that feeds frames to [analyzer]. Preview and analysis both use 16:9 and the
 * preview is letterboxed (FIT_CENTER), so the player sees exactly the area that gets analyzed.
 * Tap-to-focus and pinch-to-zoom come with [LifecycleCameraController].
 */
@Composable
fun CameraPreview(analyzer: ImageAnalysis.Analyzer, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            val sixteenNine = AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
            previewResolutionSelector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(sixteenNine)
                .build()
            // ML Kit wants text at least ~16 px tall; the 640x480 default is too small for
            // the fine print on a badge.
            imageAnalysisResolutionSelector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(sixteenNine)
                .setResolutionStrategy(
                    ResolutionStrategy(
                        Size(1920, 1080),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER,
                    ),
                )
                .build()
        }
    }
    DisposableEffect(lifecycleOwner, analyzer) {
        controller.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            controller.clearImageAnalysisAnalyzer()
            controller.unbind()
        }
    }
    AndroidView(
        factory = {
            PreviewView(it).apply {
                scaleType = PreviewView.ScaleType.FIT_CENTER
                this.controller = controller
            }
        },
        modifier = modifier,
    )
}
