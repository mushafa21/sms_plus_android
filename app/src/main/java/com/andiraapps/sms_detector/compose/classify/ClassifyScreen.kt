package com.andiraapps.sms_detector.compose.classify

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.andiraapps.sms_detector.compose.home.runDetection
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier
import java.io.IOException

private const val MODEL_PATH = "model_spam.tflite"
private var classifier: NLClassifier? = null


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassifyScreen() {
    val context = LocalContext.current
    // Init the classifier.
    try {
        classifier = NLClassifier.createFromFile(context, MODEL_PATH)
    } catch (e: IOException) {
        Log.e("CLASSIFIER ERROR", e.message!!)
    }


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


    var text by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Klasifikasikan")})
        }
    ) {paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(value = text, onValueChange = {value->
                text = value
            }, modifier = Modifier.fillMaxWidth(), placeholder = {
                Text(text = "Masukan teks")
            })
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = {
                val message = text.lowercase().trim()
                indonesianEnglishTranslator.translate(message).addOnSuccessListener {
                    runDetection(message){result ->
                        if(result > 0.5){
                            resultText = "${result*100}% Terdeteksi Pesan Spam"
                        } else {
                            resultText = "Bukan Pesan Spam (${result*100}% Terdeteksi Pesan Spam)"
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }

            }) {
                Text(text = "Classify")
            }

            Text(text = resultText)


        }
    }
}

