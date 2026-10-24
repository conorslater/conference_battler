package dev.conferencebattler.scan

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * Runs ML Kit's on-device Latin text recognizer on camera frames and reports every line found.
 * The frame is held until recognition finishes, so with CameraX's default KEEP_ONLY_LATEST
 * strategy frames are dropped instead of queueing up.
 */
class BadgeAnalyzer(private val onLines: (List<OcrLine>) -> Unit) : ImageAnalysis.Analyzer, AutoCloseable {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val media = image.image ?: return image.close()
        recognizer.process(InputImage.fromMediaImage(media, image.imageInfo.rotationDegrees))
            .addOnSuccessListener { text ->
                onLines(
                    text.textBlocks.flatMap { it.lines }.mapNotNull { line ->
                        line.boundingBox?.let { OcrLine(line.text, it.top, it.left, it.height()) }
                    },
                )
            }
            .addOnCompleteListener { image.close() }
    }

    override fun close() = recognizer.close()
}
