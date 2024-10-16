package com.specure.signaltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.specure.core.presentation.designsystem.SignalTrackerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel by viewModel<com.specure.signaltracker.MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.state.isRegistering
            }
        }

        setContent {
            SignalTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!viewModel.state.isRegistering) {
                        val navController = rememberNavController()
                        com.specure.signaltracker.NavigationRoot(
                            navController = navController,
                        )
                    } else {
                        Dialog(
                            onDismissRequest = {}
                        ) {
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(id = com.specure.signaltracker.R.string.registering_app),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}