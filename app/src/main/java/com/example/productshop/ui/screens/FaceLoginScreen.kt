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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.productshop.security.facerecog.FaceAnalyzer
import com.example.productshop.security.facerecog.FaceNetModel
import com.example.productshop.ui.viewmodel.AuthUiState
import com.example.productshop.ui.viewmodel.AuthViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceLoginScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val faceNetModel = remember { FaceNetModel(context) }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showRationale by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            showRationale = true
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { 
                showRationale = false
                onBack() 
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
                    onBack() 
                }) {
                    Text("Cancel")
                }
            },
            title = { Text("Camera Permission Required") },
            text = { Text("We need access to your camera to recognize your face and securely log you into your account.") },
            shape = RoundedCornerShape(28.dp)
        )
    }

    var statusText by remember { mutableStateOf("Authenticating...") }
    var isVerifying by remember { mutableStateOf(false) }

    val uiState = viewModel.uiState
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess()
        } else if (uiState is AuthUiState.Error) {
            statusText = uiState.message
            isVerifying = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Face Login") },
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
        if (hasCameraPermission) {
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
                                    if (!isVerifying && uiState !is AuthUiState.Loading) {
                                        isVerifying = true
                                        val embedding = faceNetModel.getFaceEmbedding(bitmap)
                                        viewModel.onFaceMatched(embedding, onLoginSuccess)
                                    }
                                },
                                onError = { _ -> statusText = "Error detecting face" }
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

                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center).size(64.dp),
                        color = Color(0xFF64B5F6)
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp)
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

                    if (uiState is AuthUiState.Error) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                statusText = "Authenticating..."
                                isVerifying = false
                                viewModel.resetState()
                            },
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold)
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
