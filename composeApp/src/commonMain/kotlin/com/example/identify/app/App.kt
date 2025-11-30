package com.example.identify.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.identify.di.AppContainer
import com.example.identify.presentation.VerifyQR.VerifyQrScreen
import com.example.identify.presentation.auth.AuthViewModel
import com.example.identify.presentation.auth.CurrentRole
import com.example.identify.presentation.auth.LoginScreen
import com.example.identify.presentation.components.AppBar
import com.example.identify.presentation.staff.StaffScreen
import com.example.identify.presentation.student.StudentDetailsScreen
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {

        val factory = rememberPermissionsControllerFactory()
        val controller = remember(factory) {
            factory.createPermissionsController()
        }

        BindEffect(controller)

        val permissionsViewModel: PermissionsViewModel = viewModel{
            PermissionsViewModel(controller)
        }


        if(permissionsViewModel.state == PermissionState.NotDetermined){
            permissionsViewModel.provideOrRequestRecordAudioPermission()
        }

        val navController = rememberNavController()

        val authViewModel: AuthViewModel = viewModel{
            AuthViewModel(authRepository = AppContainer.authRepository)
        }

        val logoutInProgress by authViewModel.logoutInProgress.collectAsState()

        val currentRole by authViewModel.currentRole.collectAsState()

        Scaffold(
            topBar = {
                AppBar(

                    logOut = {
                        navController.navigate(Routes.LOGIN.name) {
                            popUpTo(0) { inclusive = true } // clears whole back stack
                        }
                        authViewModel.logout()
                    },
                    qrScan = {
                        navController.navigate(Routes.SCAN_QR.name)
                    },
                     modifier = Modifier
                )
            }
        )
        {
            Box (
                modifier = Modifier.fillMaxSize()
            ){
                NavHost(
                    navController = navController,
                    startDestination = Routes.LOGIN.name,
                    modifier = Modifier.padding(it)
                ) {


                    composable(Routes.LOGIN.name) {

                        LoginScreen(
                            viewModel = authViewModel,
                            { navController.navigate(Routes.STUDENT.name) },
                            { navController.navigate(Routes.STAFF_HOME.name) }
                        )
                    }

                    composable(Routes.SCAN_QR.name) {
                        VerifyQrScreen()
                    }

                    composable(Routes.STUDENT.name) {
                        StudentDetailsScreen()
                    }

                    composable(Routes.STAFF_HOME.name) {
                        StaffScreen()
                    }

                }

                if (logoutInProgress) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)), // dim background
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Logging out...", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                if(currentRole == CurrentRole.LOGGED_OUT){
                    navController.navigate(Routes.LOGIN.name)
                }
            }

        }
    }
}