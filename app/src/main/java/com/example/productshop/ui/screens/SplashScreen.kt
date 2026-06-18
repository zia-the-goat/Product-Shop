package com.example.productshop.ui.screens

import android.media.SoundPool
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productshop.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
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
                text = if (isHolding) "Initializing..." else "Hold fingerprint to enter",
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
                    contentDescription = "Security fingerprint lock. Hold to unlock.",
                    modifier = Modifier.size(80.dp),
                    tint = fingerprintColor
                )
            }

        }
    }
}

@Composable
fun BubbleBackground() {
    val context = LocalContext.current
    val soundPool = remember {
        SoundPool.Builder().setMaxStreams(10).build()
    }
    val popSoundId = remember { soundPool.load(context, R.raw.pop, 1) }
    val spawnSoundId = remember { soundPool.load(context, R.raw.spawn, 1) }

    DisposableEffect(Unit) {
        onDispose {
            soundPool.release()
        }
    }

    var bubbles by remember {
        mutableStateOf(List(50) {
            createBubble()
        })
    }

    LaunchedEffect(Unit) {
        var time = 0f
        while (true) {
            delay(16) // ~60 FPS
            time += 0.016f
            bubbles = bubbles.map { bubble ->
                // Fluid/Water-like drift using sine waves
                val driftX = sin(time + bubble.phase) * 0.0005f
                val driftY = cos(time + bubble.phase) * 0.0005f
                
                val newX = (bubble.x + bubble.speedX + driftX) % 1.0f
                val newY = (bubble.y + bubble.speedY + driftY) % 1.0f
                
                // If bubble reaches random pop (2 per sec total across 50 bubbles)
                // 2 / 60 / 50 = 0.00067
                if (Random.nextFloat() < 0.00067f) { 
                    createBubble()
                } else {
                    bubble.copy(
                        x = if (newX < 0) newX + 1f else newX,
                        y = if (newY < 0) newY + 1f else newY
                    )
                }
            }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val clickedBubbleIndex = bubbles.indexOfFirst { bubble ->
                            val bX = bubble.x * size.width
                            val bY = bubble.y * size.height
                            val dist = sqrt((offset.x - bX) * (offset.x - bX) + (offset.y - bY) * (offset.y - bY))
                            dist < (bubble.size + 10).dp.toPx()
                        }
                        if (clickedBubbleIndex != -1) {
                            soundPool.play(popSoundId, 0.6f, 0.6f, 1, 0, 1.0f)
                            val newBubbles = bubbles.toMutableList()
                            newBubbles[clickedBubbleIndex] = createBubble()
                            bubbles = newBubbles
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            // Use positionChange().getDistance() > 10 to reduce frequency
                            val distance = (change.position - change.previousPosition).getDistance()
                            if (change.pressed && distance > 15f) {
                                // Spawn a new bubble at the touch location
                                val newBubble = createBubble(
                                    x = change.position.x / size.width.toFloat(),
                                    y = change.position.y / size.height.toFloat()
                                )
                                soundPool.play(spawnSoundId, 0.2f, 0.2f, 1, 0, 1.0f)
                                bubbles = (bubbles + newBubble).takeLast(150)
                                change.consume()
                            }
                        }
                    }
                }
            }
    ) {
        bubbles.forEach { bubble ->
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = bubble.size.dp.toPx(),
                center = Offset(
                    x = size.width * bubble.x,
                    y = size.height * bubble.y
                )
            )
        }
    }
}

private fun createBubble(
    x: Float = Random.nextFloat(),
    y: Float = Random.nextFloat()
): BubbleData {
    return BubbleData(
        x = x,
        y = y,
        size = Random.nextFloat() * 15f + 5f,
        speedX = (Random.nextFloat() - 0.5f) * 0.0015f,
        speedY = (Random.nextFloat() - 0.5f) * 0.0015f,
        phase = Random.nextFloat() * 2f * Math.PI.toFloat()
    )
}

data class BubbleData(
    val x: Float,
    val y: Float,
    val size: Float,
    val speedX: Float,
    val speedY: Float,
    val phase: Float
)
