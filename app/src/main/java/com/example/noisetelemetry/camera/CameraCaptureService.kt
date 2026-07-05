package com.example.noisetelemetry.camera

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.noisetelemetry.camera.buffer.CameraFrameBuffer
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraCaptureService(
    private val context: Context,
    private val frameBuffer: CameraFrameBuffer
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var captureJob: Job? = null

    fun start(lifecycleOwner: LifecycleOwner, onError: (Exception) -> Unit = {}) {
        scope.launch {
            try {
                val provider = getCameraProvider()
                cameraProvider = provider
                bindCamera(provider, lifecycleOwner, onError)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    private suspend fun getCameraProvider(): ProcessCameraProvider = suspendCoroutine { cont ->
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            cont.resume(providerFuture.get())
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCamera(provider: ProcessCameraProvider, lifecycleOwner: LifecycleOwner, onError: (Exception) -> Unit) {
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val preview = Preview.Builder().build()
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setJpegQuality(90)
            .build()

        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun startContinuousCapture(intervalMs: Long = 5000L) {
        captureJob?.cancel()
        captureJob = scope.launch {
            while (isActive) {
                takePhoto()
                delay(intervalMs)
            }
        }
    }

    fun stopContinuousCapture() {
        captureJob?.cancel()
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return
        val outputOptions = ImageCapture.OutputFileOptions.Builder(java.io.File(context.cacheDir, "frame_${System.currentTimeMillis()}.jpg")).build()
        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { uri ->
                        scope.launch(Dispatchers.IO) {
                            val bitmap = android.graphics.BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
                            if (bitmap != null) {
                                frameBuffer.addFrame(bitmap)
                            }
                        }
                    }
                }
                override fun onError(exc: ImageCaptureException) {}
            }
        )
    }

    fun shutdown() {
        captureJob?.cancel()
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }
}
