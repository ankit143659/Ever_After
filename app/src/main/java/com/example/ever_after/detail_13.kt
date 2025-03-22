package com.example.ever_after

import android.app.Activity
import android.app.Dialog
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
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

class detail_13 : Fragment() {
    private val imageViews = mutableListOf<ImageView>()
    private val viewModel: dataViewModel by viewModels()

    private lateinit var loadingDialog: Dialog
    private var selectedImageIndex: Int = -1  // Track karega kaunsa ImageView update ho raha hai

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    persistUriPermission(uri)
                    loadingDialog.show()
                    processSingleImage(uri, selectedImageIndex)
                } ?: loadingDialog.dismiss()
            } else {
                loadingDialog.dismiss()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_blank, container, false)
        setupLoadingDialog()

        // ImageView list initialize
        imageViews.addAll(
            listOf(
                view.findViewById(R.id.img1),
                view.findViewById(R.id.img2),
                view.findViewById(R.id.img3),
                view.findViewById(R.id.img4),
                view.findViewById(R.id.img5),
                view.findViewById(R.id.img6)
            )
        )

        // Har ImageView ke liye Click Listener set karna
        imageViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedImageIndex = index
                openImagePicker()
            }
        }

        // Observe ViewModel changes
        viewModel.imageBitmaps.observe(viewLifecycleOwner) { bitmaps ->
            val decodedBitmaps = bitmaps.mapNotNull { decodeFromBase64(it) }
            updateImageViews(decodedBitmaps)
        }

        return view
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun setupLoadingDialog() {
        loadingDialog = Dialog(requireContext())
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setContentView(R.layout.loading_dialog)  // Custom Lottie Layout
        loadingDialog.setCancelable(false)  // Disable outside touch
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Transparent BG
    }

    private fun processSingleImage(uri: Uri, index: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmapString = getBitmapFromUri(uri)?.let { encodeToBase64(it) }

            withContext(Dispatchers.Main) {
                bitmapString?.let {
                    val currentBitmaps = viewModel.imageBitmaps.value?.toMutableList()
                        ?: MutableList(6) { "" } // ✅ Ensure list has at least 6 elements

                    if (index in currentBitmaps.indices) { // ✅ Prevent IndexOutOfBounds
                        currentBitmaps[index] = it
                        viewModel.updateImages(currentBitmaps)
                    } else {
                        Log.e("processSingleImage", "Invalid index: $index")
                    }
                }
                loadingDialog.dismiss()
            }
        }
    }


    private fun updateImageViews(bitmaps: List<Bitmap>) {
        imageViews.forEachIndexed { index, imageView ->
            if (index < bitmaps.size && bitmaps[index] != null) {
                imageView.setImageBitmap(bitmaps[index])
                imageView.visibility = View.VISIBLE
            }
        }
        loadingDialog.dismiss()
    }

    private suspend fun getBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val byteArray = inputStream?.readBytes()
            inputStream?.close()
            byteArray?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            }
        } catch (e: Exception) {
            Log.e("ImagePicker", "Error loading bitmap", e)
            null
        }
    }

    private suspend fun encodeToBase64(bitmap: Bitmap): String {
        return withContext(Dispatchers.IO) {
            val outputStream = ByteArrayOutputStream().apply {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
            }
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        }
    }

    private fun decodeFromBase64(encodedString: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("ImagePicker", "Error decoding Base64", e)
            null
        }
    }

    private fun persistUriPermission(uri: Uri) {
        try {
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            requireContext().contentResolver.takePersistableUriPermission(uri, flag)
        } catch (e: SecurityException) {
            Log.w("ImagePicker", "Persistable URI permission failed for $uri", e)
        }
    }
}

