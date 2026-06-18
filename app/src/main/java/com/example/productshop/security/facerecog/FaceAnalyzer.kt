package com.example.productshop.security.facerecog

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer(
    private val onFaceDetected: (Bitmap, Face) -> Unit,
    private val onError: (Exception) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
    )

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces[0]
                        val bitmap = imageProxy.toBitmap()
                        val rotatedBitmap = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())
                        
                        // Crop face
                        val bounds = face.boundingBox
                        val croppedBitmap = cropFace(rotatedBitmap, bounds)
                        
                        if (croppedBitmap != null) {
                            onFaceDetected(croppedBitmap, face)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun cropFace(bitmap: Bitmap, bounds: Rect): Bitmap? {
        val left = bounds.left.coerceAtLeast(0)
        val top = bounds.top.coerceAtLeast(0)
        val width = bounds.width().coerceAtMost(bitmap.width - left)
        val height = bounds.height().coerceAtMost(bitmap.height - top)
        
        if (width <= 0 || height <= 0) return null
        
        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }
}
