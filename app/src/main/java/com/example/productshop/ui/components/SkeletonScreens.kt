package com.example.productshop.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun getShimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupSkeleton() {
    val brush = getShimmerBrush()
    
    Scaffold(
        topBar = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF1A1C2E))
                .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(modifier = Modifier.size(24.dp).background(brush, shape = RoundedCornerShape(4.dp)))
            }
        },
        containerColor = Color(0xFF1A1C2E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.width(200.dp).height(32.dp).background(brush, shape = RoundedCornerShape(8.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.width(150.dp).height(20.dp).background(brush, shape = RoundedCornerShape(4.dp)))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            repeat(6) {
                Box(modifier = Modifier.fillMaxWidth().height(56.dp).background(brush, shape = RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(56.dp).background(brush, shape = RoundedCornerShape(28.dp)))
        }
    }
}

@Composable
fun GenericSkeleton() {
    val brush = getShimmerBrush()
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(brush, shape = RoundedCornerShape(16.dp)))
            Spacer(modifier = Modifier.height(24.dp))
            repeat(3) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(brush, shape = RoundedCornerShape(12.dp)))
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
