package com.example.identify.presentation.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.identify.di.AppContainer
import com.example.identify.presentation.components.DetailRow


@Composable
fun StudentDetailsScreen(
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel<StudentViewModel> {
        StudentViewModel(AppContainer.studentRepository)
    }


    val detailsState by viewModel.studentDetailsState.collectAsState()

    val idCardState by viewModel.idCardState.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(idCardState) {
        idCardState?.onSuccess {
            // maybe show a Snackbar / Toast
            println("PDF downloaded successfully")
        }?.onFailure {
            println("Download failed: ${it.message}")
        }
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        when (val state = detailsState) {
            null -> {
                // Loading state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading student details...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            else -> {
                if (state.isSuccess) {
                    val details = state.getOrNull()!!

                    // Display details in a card layout
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            // Header
                            Text(
                                text = "Student Details",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        item {
                            // Main details card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Name
                                    DetailRow(
                                        label = "Name",
                                        value = details.vname,
                                        icon = Icons.Default.Person
                                    )

                                    HorizontalDivider(
                                        Modifier,
                                        DividerDefaults.Thickness,
                                        DividerDefaults.color
                                    )

                                    // Roll Number
                                    DetailRow(
                                        label = "Roll Number",
                                        value = details.vrollno,
                                        icon = Icons.Default.Badge
                                    )

                                    HorizontalDivider(
                                        Modifier,
                                        DividerDefaults.Thickness,
                                        DividerDefaults.color
                                    )

                                    // Email
                                    DetailRow(
                                        label = "Email",
                                        value = details.email,
                                        icon = Icons.Default.Email
                                    )

                                    HorizontalDivider(
                                        Modifier,
                                        DividerDefaults.Thickness,
                                        DividerDefaults.color
                                    )

                                    // Program
                                    DetailRow(
                                        label = "Program",
                                        value = details.vprogram,
                                        icon = Icons.Default.School
                                    )

                                    HorizontalDivider(
                                        Modifier,
                                        DividerDefaults.Thickness,
                                        DividerDefaults.color
                                    )

                                    // Branch
                                    DetailRow(
                                        label = "Branch",
                                        value = details.vbranch,
                                        icon = Icons.Default.Engineering
                                    )
                                }
                            }
                        }

                        item {
                            // Download ID Card button
                            Button(
                                onClick = {
                                    if(!isLoading) viewModel.downloadIdCard()
                                },
                                enabled = (!isLoading && idCardState == null)|| idCardState?.isFailure == true,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                when {
                                    isLoading -> {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    else -> {
                                        if (idCardState == null){
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = "Download ID Card",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Download ID Card")
                                        }else if(idCardState!!.isSuccess){
                                            Icon(
                                                imageVector = Icons.Default.Done,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Saved to Downloads")
                                        }else if (idCardState!!.isFailure){
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Try again",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Try Again")
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
//                else {
//                    // Error state
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center,
//                        modifier = Modifier.fillMaxSize()
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Error,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.error,
//                            modifier = Modifier.size(48.dp)
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Text(
//                            text = "Failed to load details",
//                            style = MaterialTheme.typography.headlineSmall,
//                            color = MaterialTheme.colorScheme.error
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(
//                            text = state.exceptionOrNull()?.message ?: "Unknown error",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant,
//                            textAlign = TextAlign.Center
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Button(
//                            onClick = { viewModel.fetchDetails() }
//                        ) {
//                            Text("Retry")
//                        }
//                    }
//                }
            }
        }
    }


}

