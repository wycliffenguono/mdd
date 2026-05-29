package com.example.maizedisease

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.maizedisease.ml.Mdd
import com.example.maizedisease.ui.theme.MaizeDiseaseTheme
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaizeDiseaseTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InputScreen()
                }
            }
        }
    }
}

sealed class PredictionResult {
    data class Success(val predictedClass: String, val probabilities: FloatArray) : PredictionResult()
    data class Error(val errorMessage: String) : PredictionResult()
}

// Define the class labels in the same order as the probabilities
val classLabels = listOf(
    "Cercospora",
    "Common Rust",
    "Northern Leaf Blight",
    "Healthy"
)


fun predict(context: Context, bitmap: Bitmap?): PredictionResult {
    val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(150, 150, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()

    try {
        if (bitmap == null) {
            // Handle null bitmap
            return PredictionResult.Error("Error: Bitmap is null")
        }

        val model = Mdd.newInstance(context)
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        val byteBuffer = imageProcessor.process(tensorImage).buffer

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 150, 150, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        // Find the index with the maximum probability
        var maxIdx = 0
        outputFeature0.forEachIndexed { index, fl ->
            Log.d("Predict", "Index: ${index} Float: ${fl}")
            if (outputFeature0[maxIdx] < fl) {
                maxIdx = index
            }
        }

        // Convert probabilities to percentages rounded off to two decimal places
        val probabilitiesPercentage = outputFeature0.map { (it * 100).toDouble() }.toDoubleArray()
            .map { String.format("%.2f", it) }
            .map { it.toFloat() }
            .toFloatArray()

        // Get the predicted class label
        val predictedClass = classLabels[maxIdx]

        // Releases model resources if no longer used.
        model.close()

        // Return the predicted class label
        return PredictionResult.Success(predictedClass, probabilitiesPercentage)
    } catch (e: Exception) {
        // Handle exceptions and log the details
        Log.e("Predict", "Error in prediction", e)
        return PredictionResult.Error("Error in prediction")
    }
}
