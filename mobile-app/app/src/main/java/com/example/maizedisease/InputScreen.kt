package com.example.maizedisease

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory.decodeStream
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import coil.compose.rememberImagePainter
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Date

@Preview
@Composable
fun InputScreenPreview(){
    InputScreen()
}

fun ContentResolver.loadBitmap(uri: Uri): Bitmap? {
    return try {
        decodeStream(openInputStream(uri))
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    }
}

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH:mm:ss").format(Date())
    val imageFileName = "JPEG" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )
    return image
}


@Composable
fun InputScreen() {
    var maxIdx by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val file = context.createImageFile()
    val cameraUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var galleryImageUri by remember { mutableStateOf<Uri?>(null) }

    var predictedClass: String? = null
    var probabilities: FloatArray? = null
    var errorMessage: String? = null

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                capturedImageUri = cameraUri
                galleryImageUri = null // Reset gallery URI

                // Load the captured image as a bitmap and pass it to the Predict function
                capturedImageUri?.let { uri ->
                    val bitmap =
                        context.contentResolver.loadBitmap(uri) // Custom extension function to load a bitmap
                    val result = predict(context, bitmap)
                    when (result) {
                        is PredictionResult.Success -> {
                            // Handle successful prediction
                            predictedClass = result.predictedClass
                            probabilities = result.probabilities
                    }
                        is PredictionResult.Error -> {
                            // Handle prediction error
                            errorMessage = result.errorMessage
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Picture capture failed", Toast.LENGTH_SHORT).show()
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            galleryImageUri = uri
            capturedImageUri = null // Reset captured image URI
            // Load the selected image as a bitmap and pass it to the Predict function
            galleryImageUri?.let { uri ->
                val bitmap = context.contentResolver.loadBitmap(uri) // Custom extension function to load a bitmap
                val result = predict(context, bitmap)
                when (result) {
                    is PredictionResult.Success -> {
                        // Handle successful prediction
                        predictedClass = result.predictedClass
                        probabilities = result.probabilities
                    }

                    is PredictionResult.Error -> {
                        // Handle prediction error
                        errorMessage = result.errorMessage
                    }
                }
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
                cameraLauncher.launch(cameraUri)
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    val currentImageUri = capturedImageUri ?: galleryImageUri

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val currentImageUri = capturedImageUri ?: galleryImageUri

        currentImageUri?.let { uri ->
            Image(
                modifier = Modifier
                    .padding(16.dp, 8.dp),
                painter = rememberImagePainter(uri),
                contentDescription = null
            )
        } ?: run {
            Text(text = "Upload a clear image of the Maize leaf...")
        }

        Button(
            onClick = {
                val permissionCheckResult =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)

                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(cameraUri)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        ) {
            Icon(imageVector = Icons.Default.Face, contentDescription = "Capture Image")
            Text("Capture Image from Camera")
        }

        Button(
            onClick = {
                galleryLauncher.launch("image/*")
            },
        ) {
            Text("Gallery")
        }
        Spacer(modifier = Modifier.height(16.dp))

    }
    currentImageUri?.let {
        val bitmap = context.contentResolver.loadBitmap(it) // Custom extension function to load a bitmap
        val result = predict(context, bitmap)
        when (result) {
            is PredictionResult.Success -> {
                // Handle successful prediction
                predictedClass = result.predictedClass
                probabilities = result.probabilities
                maxIdx = probabilities?.indices?.maxByOrNull { probabilities!![it] } ?: 0
            }

            is PredictionResult.Error -> {
                // Handle prediction error
                errorMessage = result.errorMessage
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display the top prediction first
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(Color.DarkGray) // Set the background color
                    .padding(8.dp), // Add padding to create some space between the text and the background
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top Prediction: $predictedClass",
                    color = Color.White, // Set the text color
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${probabilities?.getOrNull(maxIdx)}%",
                    color = Color.White, // Set the text color
                    modifier = Modifier.weight(1f)
                )
            }

            // Display other predictions in descending order
            val sortedProbabilities = probabilities?.withIndex()?.sortedByDescending { it.value }
            sortedProbabilities?.forEach { (index, probability) ->
                if (index != maxIdx) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .background(Color.DarkGray) // Set the background color
                            .padding(8.dp), // Add padding to create some space between the text and the background
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${classLabels[index]}",
                            color = Color.White, // Set the text color
                            modifier = Modifier.weight(1f) // Adjust weight to distribute space evenly
                        )
                        Text(
                            text = "${probability}%",
                            color = Color.White, // Set the text color
                            modifier = Modifier.weight(1f) // Adjust weight to distribute space evenly
                        )
                    }

                }
            }
        }
    }

}