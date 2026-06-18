package com.example.productshop.security.facerecog

import kotlin.math.sqrt

object FaceRecognitionUtils {
    
    fun calculateCosineSimilarity(vector1: FloatArray, vector2: FloatArray): Float {
        if (vector1.size != vector2.size) return 0f
        
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        
        for (i in vector1.indices) {
            dotProduct += vector1[i] * vector2[i]
            normA += vector1[i] * vector1[i]
            normB += vector2[i] * vector2[i]
        }
        
        return dotProduct / (sqrt(normA.toDouble()) * sqrt(normB.toDouble())).toFloat()
    }

    // Typical threshold for MobileFaceNet is around 0.7 - 0.8
    fun isMatch(vector1: FloatArray, vector2: FloatArray, threshold: Float = 0.75f): Boolean {
        return calculateCosineSimilarity(vector1, vector2) >= threshold
    }
}
