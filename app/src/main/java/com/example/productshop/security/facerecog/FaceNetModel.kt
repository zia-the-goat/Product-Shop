package com.example.productshop.security.facerecog

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class FaceNetModel(context: Context) {

    private var interpreter: Interpreter? = null

    private val imageSize = 112

    // Change if your model outputs a different embedding size
    private val embeddingSize = 128

    init {
        try {
            val assetFileDescriptor =
                context.assets.openFd("mobilefacenet.tflite")

            val inputStream =
                assetFileDescriptor.createInputStream()

            val fileChannel =
                inputStream.channel

            val modelBuffer =
                fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.declaredLength
                )

            val options = Interpreter.Options()

            interpreter = Interpreter(modelBuffer, options)

            val outputShape = interpreter!!.getOutputTensor(0).shape()

            Log.d(
                "FaceNetModel",
                "Model loaded. Output shape = ${outputShape.contentToString()}"
            )

        } catch (e: Exception) {
            Log.e(
                "FaceNetModel",
                "Failed to load model",
                e
            )
        }
    }

    fun getFaceEmbedding(faceBitmap: Bitmap): FloatArray {

        if (interpreter == null) {
            Log.w(
                "FaceNetModel",
                "Interpreter is null, returning mock embedding"
            )

            return FloatArray(embeddingSize) {
                (0..100).random() / 100f
            }
        }

        val inputBuffer = bitmapToByteBuffer(faceBitmap)

        val output = Array(1) {
            FloatArray(embeddingSize)
        }

        interpreter!!.run(
            inputBuffer,
            output
        )

        return output[0]
    }

    private fun bitmapToByteBuffer(
        bitmap: Bitmap
    ): ByteBuffer {

        val resizedBitmap =
            Bitmap.createScaledBitmap(
                bitmap,
                imageSize,
                imageSize,
                true
            )

        val inputBuffer =
            ByteBuffer.allocateDirect(
                1 * imageSize * imageSize * 3 * 4
            )

        inputBuffer.order(
            ByteOrder.nativeOrder()
        )

        val pixels =
            IntArray(
                imageSize * imageSize
            )

        resizedBitmap.getPixels(
            pixels,
            0,
            imageSize,
            0,
            0,
            imageSize,
            imageSize
        )

        for (pixel in pixels) {

            val r =
                (pixel shr 16 and 0xFF)

            val g =
                (pixel shr 8 and 0xFF)

            val b =
                (pixel and 0xFF)

            // Normalize to [-1, 1]
            inputBuffer.putFloat(
                (r - 127.5f) / 127.5f
            )

            inputBuffer.putFloat(
                (g - 127.5f) / 127.5f
            )

            inputBuffer.putFloat(
                (b - 127.5f) / 127.5f
            )
        }

        inputBuffer.rewind()

        return inputBuffer
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}