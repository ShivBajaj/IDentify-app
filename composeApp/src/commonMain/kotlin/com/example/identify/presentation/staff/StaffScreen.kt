// Updated StaffScreen.kt
package com.example.identify.presentation.staff

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.identify.di.AppContainer
import com.example.identify.data.model.FaceScanResponse
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun StaffScreen(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel<StaffViewModel> {
        StaffViewModel(AppContainer.staffRepository)
    }

    val scanResult by viewModel.scanResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Camera Preview or Results
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                scanResult != null -> {
                    // Show results screen
                    ResultScreen(
                        result = scanResult!!,
                        onRetry = {
                            viewModel.clearResult()
                        }
                    )
                }
                else -> {
                    // Show camera
                    CameraPreview(
                        onPhotoCaptured = { imageBytes ->
                            viewModel.scanFace(imageBytes)
                        }
                    )
                }
            }

            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Scanning face...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultScreen(
    result: Result<FaceScanResponse>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        result.fold(
            onSuccess = { response ->
                SuccessResultCard(response = response)
            },
            onFailure = { error ->
                ErrorResultCard(error = error)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan Again")
        }
    }
}

@Composable
fun SuccessResultCard(
    response: FaceScanResponse,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Face Scan Successful",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display response data based on FaceScanResponse structure
            ResultItem(
                label = "Name",
                value = response.studentDetails.vname
            )

            ResultItem(
                label = "Roll Number",
                value = response.studentDetails.vrollNo
            )

            ResultItem(
                label = "Email",
                value = response.studentDetails.email
            )

            ResultItem(
                label = "Program",
                value = response.studentDetails.vprogram
            )

            ResultItem(
                label = "Branch",
                value = response.studentDetails.vbranch
            )
        }
    }
}

@Composable
fun ErrorResultCard(
    error: Throwable,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Scan Failed",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = getErrorMessage(error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ResultItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun getErrorMessage(error: Throwable): String {
    return when {
        error.message?.contains("network", ignoreCase = true) == true ->
            "Network error. Please check your connection and try again."
        error.message?.contains("timeout", ignoreCase = true) == true ->
            "Request timed out. Please try again."
        error.message?.contains("unauthorized", ignoreCase = true) == true ->
            "Authentication failed. Please log in again."
        error.message?.contains("face", ignoreCase = true) == true ->
            "No face detected or face not recognized. Please try again with a clear photo."
        error.message?.contains("400", ignoreCase = true) == true ->
            "Invalid image format. Please try taking the photo again."
        error.message?.contains("500", ignoreCase = true) == true ->
            "Server error. Please try again later."
        else ->
            error.message ?: "An unknown error occurred. Please try again."
    }
}