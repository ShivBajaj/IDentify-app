package com.example.identify.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.identify.data.di.AppContainer
import org.jetbrains.compose.ui.tooling.preview.Preview



@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun LoginScreen(
    viewModel: AuthViewModel,
    goToStudentScreen: ()-> Unit,
    goToStaffScreen: ()-> Unit,
    modifier: Modifier = Modifier
){

    val state by viewModel.authState.collectAsState()
    val otpState by viewModel.otpState.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Student", "Staff")
    var selectedRole by remember { mutableStateOf(roles[0]) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    // Track OTP sent state based on actual API response
    var otpSent: Boolean = otpState?.isSuccess == true

    Box (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = {
                    Text(if (selectedRole == roles[0]) "Email" else "Username")
                },
                enabled = state !is AuthState.Loading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        when {
                            selectedRole == roles[0] && !otpSent -> {
                                viewModel.sendOtp(email = username)
                            }
                            else -> {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        }
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(selectedRole == roles[1]){
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password") },
                    enabled = state !is AuthState.Loading,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                                    viewModel.staffLogin(username = username, password = password)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(otpSent && selectedRole == roles[0]){
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    placeholder = { Text("Enter OTP") },
                    enabled = state !is AuthState.Loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            viewModel.verifyOtp(email = username, otp = otp)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedRole,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Role") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    enabled = state !is AuthState.Loading,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role) },
                            onClick = {
                                selectedRole = role
                                expanded = false
                                // Reset states when role changes
                                username = ""
                                password = ""
                                otp = ""
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    when {
                        selectedRole == roles[0] && !otpSent -> {
                            viewModel.sendOtp(email = username)
                        }
                        selectedRole == roles[0] && otpSent -> {
                            viewModel.verifyOtp(email = username, otp = otp)
                        }
                        selectedRole == roles[1] -> {
                            viewModel.staffLogin(username = username, password = password)
                        }
                    }
                },
                enabled = state !is AuthState.Loading && when {
                    selectedRole == roles[0] && !otpSent -> username.isNotEmpty()
                    selectedRole == roles[0] && otpSent -> username.isNotEmpty() && otp.isNotEmpty()
                    selectedRole == roles[1] -> username.isNotEmpty() && password.isNotEmpty()
                    else -> false
                },
                modifier = Modifier.fillMaxWidth()
            ){
                if (state is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        when {
                            selectedRole == roles[0] && !otpSent -> "Send OTP"
                            selectedRole == roles[0] && otpSent -> "Verify OTP"
                            else -> "Login"
                        }
                    )
                }
            }

            // Show error messages
            if (state is AuthState.Error) {
                Text(
                    text = (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        LaunchedEffect(currentRole) {
            when (currentRole) {
                CurrentRole.STUDENT -> {
                    otpSent = false
                    goToStudentScreen()
                }
                CurrentRole.STAFF -> {
                    goToStaffScreen()
                }
                else -> {}
            }
        }
    }
}

