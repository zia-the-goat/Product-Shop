package com.example.productshop.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.productshop.security.facerecog.FaceAnalyzer
import com.example.productshop.security.facerecog.FaceNetModel
import com.example.productshop.ui.viewmodel.AuthViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceSetupScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val faceNetModel = remember { FaceNetModel(context) }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isStarted by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(isStarted) {
        if (isStarted && !hasCameraPermission) {
            showRationale = true
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { 
                showRationale = false
                isStarted = false
            },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    launcher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRationale = false
                    isStarted = false
                }) {
                    Text("Cancel")
                }
            },
            title = { Text("Camera Permission Required") },
            text = { Text("Face recognition setup requires camera access to capture your facial features for secure authentication.") },
            shape = RoundedCornerShape(28.dp)
        )
    }

    var statusText by remember { mutableStateOf("Position your face in the circle") }
    var isCapturing by remember { mutableStateOf(false) }
    var isScanningRequested by remember { mutableStateOf(false) }

    LaunchedEffect(isScanningRequested) {
        if (isScanningRequested) {
            delay(10000) // 10 second timeout
            if (isScanningRequested && !isCapturing) {
                isScanningRequested = false
                statusText = "Timeout: No face detected. Please try again."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Face Setup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1C2E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1A1C2E)
    ) { padding ->
        if (!isStarted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = Color(0xFF64B5F6)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Face Recognition Setup",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "To ensure the best results, please make sure you are in a well-lit environment and remove any accessories like hats or sunglasses.",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { isStarted = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5)
                        )
                    ) {
                        Text("START SETUP", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (hasCameraPermission) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val faceAnalyzer = FaceAnalyzer(
                                onFaceDetected = { bitmap, _ ->
                                    if (isScanningRequested && !isCapturing) {
                                        isCapturing = true
                                        isScanningRequested = false
                                        
                                        scope.launch {
                                            statusText = "Face captured. Processing..."
                                            val embedding = faceNetModel.getFaceEmbedding(bitmap)
                                            viewModel.saveFaceEmbedding(embedding)
                                            
                                            delay(2000) // Simulated processing delay
                                            
                                            statusText = "Setup Successful!"
                                            delay(500)
                                            onSetupComplete()
                                        }
                                    }
                                },
                                onError = { _ -> 
                                    if (isScanningRequested) {
                                        isScanningRequested = false
                                        statusText = "Error detecting face. Try again."
                                    }
                                }
                            )

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(Executors.newSingleThreadExecutor(), faceAnalyzer)
                                }

                            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Circular Mask Overlay
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                ) {
                    // Draw dark background
                    drawRect(color = Color(0xFF1A1C2E).copy(alpha = 0.85f))
                    
                    val radius = size.minDimension / 3
                    // Cut out a transparent hole
                    drawCircle(
                        color = Color.Transparent,
                        radius = radius,
                        blendMode = BlendMode.Clear
                    )
                    // Draw circle border
                    drawCircle(
                        color = Color(0xFF64B5F6),
                        radius = radius,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            isScanningRequested = true
                            statusText = "Scanning..."
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        enabled = !isCapturing && !isScanningRequested,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5)
                        )
                    ) {
                        if (isScanningRequested) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("START SCAN", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Camera permission required", color = Color.White)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            faceNetModel.close()
        }
    }
}
