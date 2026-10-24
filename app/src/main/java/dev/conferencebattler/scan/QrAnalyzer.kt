package dev.conferencebattler.scan

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Runs ML Kit's barcode scanner, limited to QR codes, on camera frames and reports the payload of
 * the first code found. Binary payloads with no text form are reported as hex. Frames are held
 * until scanning finishes, as in [BadgeAnalyzer].
 */
class QrAnalyzer(private val onPayload: (String) -> Unit) : ImageAnalysis.Analyzer, AutoCloseable {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build(),
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val media = image.image ?: return image.close()
        scanner.process(InputImage.fromMediaImage(media, image.imageInfo.rotationDegrees))
            .addOnSuccessListener { codes ->
                codes.firstNotNullOfOrNull { code ->
                    code.rawValue ?: code.rawBytes?.joinToString("") { "%02x".format(it) }
                }?.let(onPayload)
            }
            .addOnCompleteListener { image.close() }
    }

    override fun close() = scanner.close()
}
