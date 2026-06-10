package com.example.productshop.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // 11. Performance: Perceived Performance via smooth animations
    var isHolding by remember { mutableStateOf(false) }
    val holdProgress = animateFloatAsState(
        targetValue = if (isHolding) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = LinearEasing),
        label = "holdProgress"
    )

    LaunchedEffect(holdProgress.value) {
        if (holdProgress.value >= 0.5f) {
            onSplashFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF42A5F5))
                )
            )
    ) {
        // 13. Visual Design: Illustrations/Animation
        BubbleBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val fingerprintColor by animateColorAsState(
                targetValue = if (isHolding) Color(0xFF81D4FA) else Color.White,
                animationSpec = tween(500),
                label = "fingerprintColor"
            )

            val scale by animateFloatAsState(
                targetValue = if (isHolding) 1.2f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "scale"
            )


            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                        append("InsureTech")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Guard")
                    }
                },
                color = Color.White,
                fontSize = 28.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isHolding) "Authorizing..." else "Hold fingerprint to enter",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 3. User Input: Touch Targets (Icon area exceeds 48dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .semantics { contentDescription = "Security fingerprint lock. Hold to unlock." }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isHolding = true
                                tryAwaitRelease()
                                isHolding = false
                            }
                        )
                    }
            ) {
                if (isHolding) {
                    // 5. Feedback: Progress Indicators (Visual glow feedback)
                    Canvas(modifier = Modifier.size(200.dp)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.4f * holdProgress.value),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.minDimension / 2
                            )
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = fingerprintColor
                )
            }

        }
    }
}

@Composable
fun BubbleBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bubbles")
    
    // Create 100 bubbles (Standardized count)
    val bubbles = remember {
        List(100) {
            BubbleData(
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                size = Random.nextFloat() * 15f + 5f,
                speedX = (Random.nextFloat() - 0.5f) * 0.1f,
                speedY = (Random.nextFloat() - 0.5f) * 0.1f
            )
        }
    }

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        bubbles.forEach { bubble ->
            val currentX = (bubble.startX + bubble.speedX * time * 20) % 1.0f
            val currentY = (bubble.startY + bubble.speedY * time * 20) % 1.0f
            
            val x = if (currentX < 0) currentX + 1f else currentX
            val y = if (currentY < 0) currentY + 1f else currentY

            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = bubble.size.dp.toPx(),
                center = Offset(
                    x = size.width * x,
                    y = size.height * y
                )
            )
        }
    }
}

data class BubbleData(
    val startX: Float,
    val startY: Float,
    val size: Float,
    val speedX: Float,
    val speedY: Float
)
