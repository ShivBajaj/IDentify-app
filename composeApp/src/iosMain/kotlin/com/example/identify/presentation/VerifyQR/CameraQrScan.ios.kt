package com.example.identify.presentation.VerifyQR

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import io.ktor.util.date.getTimeMillis
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSError
import platform.QuartzCore.CALayer
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun CameraQrScan(verifyQr: (String) -> Unit) {
    var permissionGranted by remember { mutableStateOf(true) } // handled by moko-permissions

    if (permissionGranted) {
        CameraPreview(onQrScanned = verifyQr)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Camera permission is required to scan QR codes",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = { /* launch permission with moko-permissions */ }) {
                Text("Grant Camera Permission")
            }
        }
    }
}

@Composable
private fun CameraPreview(onQrScanned: (String) -> Unit) {
    val qrScanner = remember { QrCodeScanner(onQrScanned) }

    DisposableEffect(Unit) {
        onDispose {
            qrScanner.onDispose()
        }
    }

    UIKitView(
        factory = { qrScanner.view },
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalForeignApi::class)
private class QrCodeScanner(
    private val onQrScanned: (String) -> Unit
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    private val captureSession = AVCaptureSession()
    private val previewLayer = AVCaptureVideoPreviewLayer(session = captureSession)

    private val cameraUIView = object : UIView(CGRectZero.readValue()) {
        override fun layoutSubviews() {
            super.layoutSubviews()
            previewLayer.frame = bounds
        }
    }
    val view: UIView = cameraUIView

    private var lastScanTimestamp: Long = 0
    private val scanCooldown = 2000L // 2s cooldown like Android

    init {
        cameraUIView.layer.addSublayer(previewLayer)

        dispatch_async(platform.darwin.dispatch_get_global_queue(0, 0.toULong())) {
            setupCamera()
            captureSession.startRunning()
        }
    }

    private fun setupCamera() {
        captureSession.sessionPreset = AVCaptureSessionPresetHigh

        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            ?: return

        val input = try {
            AVCaptureDeviceInput(device = device, error = null)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        val metadataOutput = AVCaptureMetadataOutput()

        if (captureSession.canAddInput(input) && captureSession.canAddOutput(metadataOutput)) {
            captureSession.addInput(input)
            captureSession.addOutput(metadataOutput)

            metadataOutput.setMetadataObjectsDelegate(this, dispatch_get_main_queue())
            metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        }

        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
    }

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        val currentTime = getTimeMillis()
        if (currentTime - lastScanTimestamp < scanCooldown) return

        didOutputMetadataObjects.forEach { obj ->
            val qr = obj as? AVMetadataMachineReadableCodeObject
            val value = qr?.stringValue
            if (value != null) {
                lastScanTimestamp = currentTime
                onQrScanned(value)
                return
            }
        }
    }

    fun onDispose() {
        dispatch_async(platform.darwin.dispatch_get_global_queue(0, 0.toULong())) {
            if (captureSession.isRunning()) {
                captureSession.stopRunning()
            }
        }
    }
}
