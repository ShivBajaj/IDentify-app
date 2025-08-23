package com.example.identify.presentation.VerifyQR

import androidx.compose.runtime.Composable

@Composable
expect fun CameraQrScan(verifyQr: (String) -> Unit)