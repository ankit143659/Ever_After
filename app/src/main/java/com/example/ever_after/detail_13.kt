package com.example.ever_after

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.io.ByteArrayOutputStream
import java.io.InputStream

class detail_13 : Fragment() {

    private val imageViews = mutableListOf<ImageView>()
    private val selectedImageStrings = mutableListOf<String>() // Store Base64 strings

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedUris = mutableListOf<Uri>()
                result.data?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                        selectedUris.add(clipData.getItemAt(i).uri)
                    }
                } ?: result.data?.data?.let { uri ->
                    selectedUris.add(uri)
                }

                // Take persistent URI permissions here
                selectedUris.forEach { uri ->
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                if (selectedUris.isNotEmpty()) {
                    processImages(selectedUris)
                }
            }
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_blank, container, false)

        // Initialize ImageViews from GridLayout
        imageViews.apply {
            add(view.findViewById(R.id.img1))
            add(view.findViewById(R.id.img2))
            add(view.findViewById(R.id.img3))
            add(view.findViewById(R.id.img4))
            add(view.findViewById(R.id.img5))
            add(view.findViewById(R.id.img6))
        }

        view.findViewById<View>(R.id.btnSelectImage).setOnClickListener {
            if (selectedImageStrings.size < 6) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                imagePickerLauncher.launch(intent)
            } else {
                Toast.makeText(requireContext(), "Maximum 6 images allowed!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun processImages(newUris: List<Uri>) {
        val availableSlots = 6 - selectedImageStrings.size
        val toAdd = newUris.take(availableSlots)

        for (uri in toAdd) {
            val bitmap = uriToBitmap(uri)
            if (bitmap != null) {
                val base64String = bitmapToBase64(bitmap)
                selectedImageStrings.add(base64String)
            }
        }

        updateImageViews()
    }

    private fun updateImageViews() {
        for (i in selectedImageStrings.indices) {
            if (i < imageViews.size) {
                val bitmap = base64ToBitmap(selectedImageStrings[i])
                if (bitmap != null) {
                    imageViews[i].setImageBitmap(bitmap)
                    imageViews[i].visibility = View.VISIBLE
                } else {
                    Log.e("ImagePicker", "Bitmap is null for image at index $i")
                }
            } else {
                Log.e("ImagePicker", "Index $i is out of bounds for imageViews")
            }
        }
    }

    // 🔹 Convert URI to Bitmap
    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap == null) {
                Log.e("ImagePicker", "Failed to decode bitmap from URI: $uri")
            } else {
                Log.d("ImagePicker", "Bitmap loaded successfully: $uri")
            }
            bitmap
        } catch (e: Exception) {
            Log.e("ImagePicker", "Error converting URI to Bitmap: ${e.message}")
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
        Log.d("ImagePicker", "Base64 string: $base64String")
        return base64String
    }

    private fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            if (bitmap == null) {
                Log.e("ImagePicker", "Failed to decode bitmap from Base64 string")
            } else {
                Log.d("ImagePicker", "Bitmap decoded successfully from Base64 string")
            }
            bitmap
        } catch (e: Exception) {
            Log.e("ImagePicker", "Error converting Base64 to Bitmap: ${e.message}")
            null
        }
    }
}
