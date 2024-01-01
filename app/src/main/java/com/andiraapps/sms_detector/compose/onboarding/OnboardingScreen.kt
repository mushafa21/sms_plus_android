package com.andiraapps.sms_detector.compose.onboarding

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.andiraapps.sms_detector.R
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

private val permission: String = Manifest.permission.READ_SMS
private val requestCode: Int = 1

data class OnboardPage(
    val imageRes: Int, val title: String, val description: String
)

val onboardPagesList = listOf(
    OnboardPage(
        imageRes = R.drawable.img_onboarding_1,
        title = "Selamat Datang di Aplikasi SMS Plus!",
        description = "Dengan SMS Plus anda bisa mendeteksi pesan SMS spam"
    ), OnboardPage(
        imageRes = R.drawable.img_onboarding_2,
        title = "Berikan izin untuk mengakses pesan SMS anda",
        description = "Aplikasi ini tidak mengumpulkan pesan sms anda"
    ), OnboardPage(
        imageRes = R.drawable.img_onboarding_3,
        title = "Selamat Menikmati Aplikasi SMS Plus",
        description = "Rasakan Pengalaman SMS bebas dengan Spam!"
    )
)

@Composable
fun OnboardingScreen(
    goToHome: () -> Unit
) {

    val onboardPages = onboardPagesList

    val currentPage = remember { mutableStateOf(0) }

    // Create an English-German translator:
    val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.INDONESIAN)
        .setTargetLanguage(TranslateLanguage.ENGLISH)
        .build()
    val indonesianEnglishTranslator = Translation.getClient(options)
    val conditions = DownloadConditions.Builder()
        .requireWifi()
        .build()
    indonesianEnglishTranslator.downloadModelIfNeeded(conditions)
        .addOnSuccessListener {
            // Model downloaded successfully. Okay to start translating.
            // (Set a flag, unhide the translation UI, etc.)
        }
        .addOnFailureListener { exception ->
            // Model couldn’t be downloaded or other internal error.
            // ...
        }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        OnBoardImageView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            imageRes = onboardPages[currentPage.value].imageRes
        )

        OnBoardDetails(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            currentPage = onboardPages[currentPage.value]
        )

        OnBoardNavButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            currentPage = currentPage.value,
            noOfPages = onboardPages.size,
            onFinished = goToHome,
            onNextClicked =
            {
                currentPage.value++
            }
        )

        TabSelector(
            onboardPages = onboardPages,
            currentPage = currentPage.value
        ) { index ->
            currentPage.value = index
        }
    }
}

@Composable
fun OnBoardImageView(modifier: Modifier = Modifier, imageRes: Int) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillWidth
        )
        Box(modifier = Modifier
            .fillMaxSize()
            .align(Alignment.BottomCenter)
            .graphicsLayer {
                // Apply alpha to create the fading effect
                alpha = 0.6f
            }
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        Pair(0.8f, Color.Transparent), Pair(1f, Color.White)
                    )
                )
            ))
    }
}


@Composable
fun OnBoardDetails(
    modifier: Modifier = Modifier, currentPage: OnboardPage
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = currentPage.title,
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = currentPage.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun OnBoardNavButton(
    modifier: Modifier = Modifier,
    currentPage: Int,
    noOfPages: Int,
    onNextClicked: () -> Unit,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    if (currentPage == 1) {
        if (ContextCompat.checkSelfPermission(context, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(permission), requestCode)
        }
    }
    Button(
        onClick = {
            if (currentPage < noOfPages - 1) {
                onNextClicked()
            } else {
                onFinished()
            }
        }, modifier = modifier
    ) {
        Text(text = if (currentPage < noOfPages - 1) "Next" else "Get Started")
    }
}

@Composable
fun TabSelector(onboardPages: List<OnboardPage>, currentPage: Int, onTabSelected: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = currentPage,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        onboardPages.forEachIndexed { index, _ ->
            Tab(selected = index == currentPage, onClick = {
                onTabSelected(index)
            }, modifier = Modifier.padding(16.dp), content = {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (index == currentPage) MaterialTheme.colorScheme.onPrimary
                            else Color.LightGray, shape = RoundedCornerShape(4.dp)
                        )
                )
            })
        }
    }
}


