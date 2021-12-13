package com.orlove101.android.casersapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.orlove101.android.casersapp.databinding.ActivityMainBinding
import com.orlove101.android.casersapp.utils.CropImageContract
import com.orlove101.android.casersapp.utils.MIMETYPE_IMAGES
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val cropResultLauncher = registerForActivityResult(CropImageContract()) { uri ->
        if (uri != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val imageBitmap: Bitmap = Glide
                    .with(this@MainActivity)
                    .asBitmap()
                    .load(uri)
                    .submit()
                    .get()

                getTextFromImage(imageBitmap)
            }
        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                showRationalDialog(
                    getString(R.string.rationale_title),
                    getString(R.string.rationale_desc),
                    Manifest.permission.CAMERA
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonCapture.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                cropResultLauncher.launch(MIMETYPE_IMAGES)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showRationalDialog(
        title: String,
        message: String,
        permission: String
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                requestPermissionLauncher.launch(permission)
            }
        builder.create().show()
    }

    private fun getTextFromImage(imageBitmap: Bitmap) {
        val image = InputImage.fromBitmap(imageBitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { text ->
                processTextRecognitionResult(text)
            }
            .addOnFailureListener { error ->
                error.printStackTrace();
            }
    }

    private fun processTextRecognitionResult(texts: Text) {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            showToast("No text found")
            return
        }

        binding.textData.post {
            binding.textData.text = texts.text
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}