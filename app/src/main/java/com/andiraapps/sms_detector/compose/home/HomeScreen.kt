package com.andiraapps.sms_detector.compose.home

import android.content.Context
import android.provider.Telephony
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andiraapps.sms_detector.model.Sms
import com.andiraapps.sms_detector.ui.theme.SMSDetectorTheme
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier
import java.io.IOException


private const val MODEL_PATH = "model_spam.tflite"
private var classifier: NLClassifier? = null
val smsList : ArrayList<Sms> = ArrayList()



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    goToClassify : ()->Unit
) {
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
    readSms(context = context)


    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = "SMS Plus")
            }) 
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = goToClassify) {
                Text(text = "KLASIFIKASIKAN")
            }
        }
    ) {paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            LazyColumn(){
                items(smsList.count()){ index ->
                    SmsItem(sms = smsList[index])
                }
            }


        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(){
    SMSDetectorTheme {
        HomeScreen(){}
    }
}

fun runDetection(text: String, onResult : (Float)->Unit) {
    val apiResults: List<Category> = classifier!!.classify(text)
    val score: Float = apiResults[1].getScore()
    onResult(score)
}


private fun readSms(context: Context)
{
    val numberCol = Telephony.TextBasedSmsColumns.ADDRESS
    val textCol = Telephony.TextBasedSmsColumns.BODY
    val typeCol = Telephony.TextBasedSmsColumns.TYPE // 1 - Inbox, 2 - Sent

    val projection = arrayOf(numberCol, textCol, typeCol)

    val cursor = context.contentResolver.query(
        Telephony.Sms.CONTENT_URI,
        projection, null, null, null
    )

    val numberColIdx = cursor!!.getColumnIndex(numberCol)
    val textColIdx = cursor.getColumnIndex(textCol)
    val typeColIdx = cursor.getColumnIndex(typeCol)

    var count = 0;

    while (cursor.moveToNext()) {
        if (count == 50){
            break
        }

        val number = cursor.getString(numberColIdx)
        val text = cursor.getString(textColIdx)
        val type = cursor.getString(typeColIdx)

        Log.d("MY_APP", "$number $text $type")
        if(type == "1"){
            smsList.add(
                Sms().apply {
                    sender = number
                    message = text
                }
            )
            count ++
        }

    }

    cursor.close()
}


@Composable
fun SmsItem(sms : Sms){
    var spamScore by remember { mutableStateOf(0f) }
    var spamScoreText by remember { mutableStateOf("") }
    runDetection(sms.message){score ->
        spamScore = score
        if(score > 0.5){
            spamScoreText = "${score*100}% Pesan Spam"
        }

    }
    Box(modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 5.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(
            if (spamScore > 0.8) {
                Color.Red
            } else if (spamScore > 0.5) {
                Color(0xfff7bd01)
            } else {
                MaterialTheme.colorScheme.primary
            }
        )){
        Column(
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp)
        ) {
            Text(text = sms.sender, style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = sms.message, style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = spamScoreText, style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }
    }
}
