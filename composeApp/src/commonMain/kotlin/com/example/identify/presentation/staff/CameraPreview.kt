package com.example.identify.presentation.staff

import androidx.compose.runtime.Composable


@Composable
expect fun CameraPreview(
    onPhotoCaptured: (ByteArray) -> Unit = {}
)
