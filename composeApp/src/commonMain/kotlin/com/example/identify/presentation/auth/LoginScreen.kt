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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    goToStudentScreen: () -> Unit,
    goToStaffScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.authState.collectAsState()
    val otpState by viewModel.otpState.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val otp by viewModel.otp.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Student", "Staff")

    val otpSent: Boolean = otpState?.isSuccess == true


    val usernameFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val otpFocus = remember { FocusRequester() }
    val roleFocus = remember { FocusRequester() }

    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Username/Email
            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.onUsernameChange(it) },
                placeholder = { Text(if (selectedRole == "Student") "Email" else "Username") },
                enabled = state !is AuthState.Loading,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (selectedRole == "Staff") {
                            passwordFocus.requestFocus()
                        } else if (selectedRole == "Student" && username.isNotEmpty()) {
                            viewModel.sendOtp(email = username)
                            otpFocus.requestFocus()
                        } else {
                            roleFocus.requestFocus()
                        }
                    }
                ),
                textStyle = TextStyle(fontFamily = FontFamily.Default),
                modifier = Modifier.fillMaxWidth()
                    .focusRequester(usernameFocus)
            )

            AnimatedVisibility(selectedRole == "Staff") {
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    placeholder = { Text("Password") },
                    enabled = state !is AuthState.Loading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (username.isNotEmpty() && password.isNotEmpty()) {
                                viewModel.staffLogin(username, password)
                                keyboardController?.hide()
                            }
                        }
                    ),
                    textStyle = TextStyle(fontFamily = FontFamily.Default),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 16.dp)
                        .focusRequester(passwordFocus)
                )
            }

            // OTP (for students after OTP sent)
            AnimatedVisibility(otpSent && selectedRole == "Student") {
                OutlinedTextField(
                    value = otp,
                    onValueChange = { viewModel.onOtpChange(it) },
                    placeholder = { Text("Enter OTP") },
                    enabled = state !is AuthState.Loading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (username.isNotEmpty() && otp.isNotEmpty()) {
                                viewModel.verifyOtp(email = username, otp = otp)
                                keyboardController?.hide()
                            }
                        }
                    ),
                    textStyle = TextStyle(fontFamily = FontFamily.Default),
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 16.dp)
                        .focusRequester(otpFocus)
                )
            }

            // Role selection
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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role) },
                            onClick = {
                                viewModel.onRoleChange(role)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Button
            Button(
                onClick = {
                    when {
                        selectedRole == "Student" && !otpSent -> viewModel.sendOtp(email = username)
                        selectedRole == "Student" && otpSent -> viewModel.verifyOtp(email = username, otp = otp)
                        selectedRole == "Staff" -> viewModel.staffLogin(username, password)
                    }
                },
                enabled = state !is AuthState.Loading && when {
                    selectedRole == "Student" && !otpSent -> username.isNotEmpty()
                    selectedRole == "Student" && otpSent -> username.isNotEmpty() && otp.isNotEmpty()
                    selectedRole == "Staff" -> username.isNotEmpty() && password.isNotEmpty()
                    else -> false
                },
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                if (state is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        when {
                            selectedRole == "Student" && !otpSent -> "Send OTP"
                            selectedRole == "Student" && otpSent -> "Verify OTP"
                            else -> "Login"
                        }
                    )
                }
            }

            // Errors
            if (state is AuthState.Error) {
                Text(
                    text = (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Navigation
        LaunchedEffect(currentRole) {
            when (currentRole) {
                CurrentRole.STUDENT -> goToStudentScreen()
                CurrentRole.STAFF -> goToStaffScreen()
                else -> {}
            }
        }
    }
}
