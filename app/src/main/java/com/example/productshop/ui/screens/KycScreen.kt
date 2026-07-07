package com.example.productshop.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.productshop.ui.viewmodel.KycUiState
import com.example.productshop.ui.viewmodel.KycViewModel
import com.example.productshop.util.AnalyticsManager
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycScreen(
    viewModel: KycViewModel,
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Fetch profile and KYC status when screen opens
    LaunchedEffect(Unit) {
        viewModel.fetchProfileAndKyc()
    }

    LaunchedEffect(uiState) {
        if (uiState is KycUiState.Success) {
            showSuccessDialog = true
            kotlinx.coroutines.delay(3000)
            showSuccessDialog = false
            onBack()
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessDialog = false
                    onBack()
                }) {
                    Text("OK")
                }
            },
            title = { Text("Verification Successful!") },
            text = { Text("Your documents have been submitted and verified. You now have full access to all our products and services.") },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(48.dp)) },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Identity Verification", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onHome) {
                        Icon(androidx.compose.material.icons.Icons.Default.Home, contentDescription = "Home")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (viewModel.kycStatus?.primaryIndicator == true) 
                    "Your identity has been verified. You have full access to all products."
                    else "Secure your account by uploading the following documents.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = if (viewModel.kycStatus?.primaryIndicator == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (viewModel.kycStatus?.primaryIndicator == true) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (viewModel.kycStatus?.primaryIndicator == true) {
                // Polished Verified UI
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFFE8F5E9).copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(96.dp),
                            shape = CircleShape,
                            color = Color(0xFF2E7D32),
                            tonalElevation = 4.dp,
                            shadowElevation = 8.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color.White,
                                modifier = Modifier.size(64.dp).padding(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Identity Verified",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1B5E20)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Your account is fully compliant",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32).copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                DocumentCaptureCard(
                    label = "Selfie",
                    description = "Clear photo of your face",
                    uri = viewModel.selfieUri,
                    onCapture = { uri -> viewModel.selfieUri = uri }
                )

                Spacer(modifier = Modifier.height(24.dp))

                DocumentCaptureCard(
                    label = "Proof of Residence",
                    description = "Utility bill or bank statement",
                    uri = viewModel.residenceUri,
                    onCapture = { uri -> viewModel.residenceUri = uri }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (uiState is KycUiState.Error) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Button only visible if NOT verified
            if (viewModel.kycStatus?.primaryIndicator != true) {
                Button(
                    onClick = { viewModel.uploadKyc(context) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = viewModel.selfieUri != null && viewModel.residenceUri != null && uiState !is KycUiState.Loading && uiState !is KycUiState.Verifying,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState is KycUiState.Loading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Uploading Documents...")
                        }
                    } else if (uiState is KycUiState.Verifying) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Checking Compliance...")
                        }
                    } else if (uiState is KycUiState.Success) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Verification Complete!")
                        }
                    } else {
                        Icon(Icons.Default.FileUpload, contentDescription = "Upload Icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Verification")
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentCaptureCard(
    label: String,
    description: String,
    uri: Uri?,
    onCapture: (Uri) -> Unit
) {
    val context = LocalContext.current
    var tempUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showOptions by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContracts.TakePicture() {
            override fun createIntent(context: Context, input: Uri): Intent {
                return super.createIntent(context, input).apply {
                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    clipData = ClipData.newRawUri(null, input)
                }
            }
        },
        onResult = { success ->
            if (success) {
                tempUri?.let { 
                    onCapture(it)
                    AnalyticsManager.logSelectContent("document_capture", label)
                }
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            onCapture(it)
            AnalyticsManager.logSelectContent("document_gallery", label)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.externalCacheDir, "${label.replace(" ", "_").lowercase()}_${System.currentTimeMillis()}.png")
            val newUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempUri = newUri
            cameraLauncher.launch(newUri)
        } else {
            Log.e("KycScreen", "Camera permission denied")
        }
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Select Document Source") },
            text = { Text("How would you like to upload your $label?") },
            confirmButton = {
                TextButton(onClick = { 
                    showOptions = false
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val file = File(context.externalCacheDir, "${label.replace(" ", "_").lowercase()}_${System.currentTimeMillis()}.png")
                        val newUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        tempUri = newUri
                        cameraLauncher.launch(newUri)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showOptions = false
                    galleryLauncher.launch("image/*")
                }) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }
            }
        )
    }

    Column {
        Text(text = label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                .clickable {
                    showOptions = true
                },
            contentAlignment = Alignment.Center
        ) {
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Captured", tint = Color(0xFF2E7D32))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera Icon", tint = Color.Gray, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tap to capture", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                }
            }
        }
    }
}
