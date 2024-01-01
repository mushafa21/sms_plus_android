package com.andiraapps.sms_detector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andiraapps.sms_detector.compose.classify.ClassifyScreen
import com.andiraapps.sms_detector.compose.home.HomeScreen
import com.andiraapps.sms_detector.compose.onboarding.OnboardingScreen
import com.andiraapps.sms_detector.ui.theme.SMSDetectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SMSDetectorTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "onboarding" ){
                        composable("onboarding"){
                            OnboardingScreen(goToHome = {
                                navController.navigate("home")
                            })
                        }
                        composable("home"){
                            HomeScreen(goToClassify = {
                                navController.navigate("classify")

                            })
                        }
                        composable("classify"){
                            ClassifyScreen()
                        }
                    }
                }
            }
        }
    }
}
