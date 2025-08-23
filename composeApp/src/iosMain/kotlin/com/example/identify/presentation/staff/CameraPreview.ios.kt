package com.example.identify.presentation.staff

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.readValue
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.AVFoundation.fileDataRepresentation
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun CameraPreview(
    onPhotoCaptured: (ByteArray) -> Unit
) {
    var capturedImage: ByteArray? by remember { mutableStateOf(null) }

    if (capturedImage != null) {
        ImagePreview(
            imageBytes = capturedImage!!,
            onRetake = { capturedImage = null },
            onUsePhoto = { onPhotoCaptured(capturedImage!!) }
        )
    } else {
        CameraCapture(
            onPhotoCaptured = { imageData ->
                capturedImage = imageData
            }
        )
    }
}

@Composable
private fun CameraCapture(
    modifier: Modifier = Modifier,
    onPhotoCaptured: (ByteArray) -> Unit
) {
    val cameraManager = remember { CameraManager(onPhotoCaptured) }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.onDispose()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        UIKitView(
            factory = { cameraManager.view },
            modifier = Modifier.fillMaxSize()
        )
        Button(
            onClick = { cameraManager.capturePhoto() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            Text("Capture")
        }
    }
}

@Composable
private fun ImagePreview(
    imageBytes: ByteArray,
    onRetake: () -> Unit,
    onUsePhoto: () -> Unit
) {
    // Convert ByteArray directly to ImageBitmap using Skia
    val imageBitmap: ImageBitmap = remember(imageBytes) {
        Image.makeFromEncoded(imageBytes).toComposeImageBitmap()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Captured photo",
            modifier = Modifier.fillMaxSize()
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = onRetake) {
                Text("Recapture")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onUsePhoto) {
                Text("Use Photo")
            }
        }
    }
}

// Helper functions
@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    return this.bytes?.readBytes(this.length.toInt()) ?: ByteArray(0)
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    return this.usePinned {
        NSData.create(bytes = it.addressOf(0), length = this.size.toULong())
    }
}

@OptIn(ExperimentalForeignApi::class)
private class CameraManager(
    private val onPhotoCaptured: (ByteArray) -> Unit
) : NSObject(), AVCapturePhotoCaptureDelegateProtocol {

    private val cameraUIView = object : UIView(CGRectZero.readValue()) {
        override fun layoutSubviews() {
            super.layoutSubviews()
            previewLayer.frame = bounds
        }
    }
    val view: UIView = cameraUIView

    private val captureSession = AVCaptureSession()
    private val photoOutput = AVCapturePhotoOutput()
    private val previewLayer = AVCaptureVideoPreviewLayer(session = captureSession)

    init {
        cameraUIView.layer.addSublayer(previewLayer)

        dispatch_async(platform.darwin.dispatch_get_global_queue(0, 0.toULong())) {
            setupCamera()
            captureSession.startRunning()
        }
    }

    private fun setupCamera() {
        captureSession.sessionPreset = AVCaptureSessionPresetPhoto

        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            ?: AVCaptureDevice.defaultDeviceWithDeviceType(
                deviceType = AVCaptureDeviceTypeBuiltInWideAngleCamera,
                mediaType = AVMediaTypeVideo,
                position = AVCaptureDevicePositionBack
            ) ?: return

        val input = try {
            AVCaptureDeviceInput(device = device, error = null)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        if (captureSession.canAddInput(input) && captureSession.canAddOutput(photoOutput)) {
            captureSession.addInput(input)
            captureSession.addOutput(photoOutput)
        }

        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
    }

    fun capturePhoto() {
        val settings = AVCapturePhotoSettings()
        photoOutput.capturePhotoWithSettings(settings, this)
    }

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?
    ) {
        didFinishProcessingPhoto.fileDataRepresentation()?.let { nsData ->
            val imageBytes = nsData.toByteArray()
            dispatch_async(dispatch_get_main_queue()) {
                onPhotoCaptured(imageBytes)
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