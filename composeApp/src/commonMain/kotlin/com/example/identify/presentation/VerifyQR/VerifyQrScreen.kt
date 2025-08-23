package com.example.identify.presentation.VerifyQR

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.identify.data.di.AppContainer
import com.example.identify.presentation.components.DetailRow

@Composable
fun VerifyQrScreen(
    modifier: Modifier = Modifier
){
    val viewModel = viewModel <VerifyQrViewModel>{
        VerifyQrViewModel(AppContainer.verificationRepository)
    }

    val verifyQrState by viewModel.verifyQrState.collectAsState()
    val status by viewModel.status.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
        ,
        contentAlignment = Alignment.Center
    ){
        when (status) {

            VerifyQrStatus.IDLE -> {
                CameraQrScan { qrData ->
                    viewModel.verifyQr(qrData)
                }
            }

            VerifyQrStatus.LOADING -> {
                CircularProgressIndicator()
            }

            VerifyQrStatus.SUCCESS -> {
                val result = verifyQrState
                result?.onSuccess { response ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DetailRow(
                                label = "Message",
                                value = response.message,
                                icon = Icons.Default.Mail
                            )

                            HorizontalDivider(
                                Modifier,
                                DividerDefaults.Thickness,
                                DividerDefaults.color
                            )

                            DetailRow(
                                label = "College Name",
                                value = response.collegeName,
                                icon = Icons.Default.School
                            )

                        }
                    }
                }?.onFailure { error ->
                    Text(
                        text = "Invalid QR!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }


            }
        }

    }
}