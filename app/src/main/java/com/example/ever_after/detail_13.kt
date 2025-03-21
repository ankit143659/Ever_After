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

    private lateinit var loadingDialog : Dialog


    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedUris = mutableListOf<Uri>()
                result.data?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        persistUriPermission(uri)
                        selectedUris.add(uri)
                    }
                } ?: result.data?.data?.let { uri ->
                    persistUriPermission(uri)
                    selectedUris.add(uri)
                }

                if (selectedUris.isNotEmpty()) {
                    loadingDialog.show()
                    processImages(selectedUris)
                } else {
                    loadingDialog.dismiss() // Agar koi image select nahi hui to dismiss karna
                }
            } else {
                loadingDialog.dismiss() // User ne cancel kar diya
            }
        }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_blank, container, false)
        setupLoadingDialog()
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

        view.findViewById<Button>(R.id.btnSelectImage).setOnClickListener {
            openImagePicker()
        }

        // Observe ViewModel changes
        viewModel.imageBitmaps.observe(viewLifecycleOwner) { bitmaps ->
            val decodedBitmaps = bitmaps.mapNotNull { decodeFromBase64(it) }
            updateImageViews(decodedBitmaps)
        }

        return view
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
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

    private fun processImages(newUris: List<Uri>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val newBitmapStrings = newUris.mapNotNull { uri ->
                getBitmapFromUri(uri)?.let { encodeToBase64(it) }
            }

            withContext(Dispatchers.Main) {
                val maxSize = 6  // **Max 6 images allowed**
                var currentBitmaps = viewModel.imageBitmaps.value?.toMutableList() ?: mutableListOf()

                if (currentBitmaps.size < maxSize) {
                    // **Step 1: Pehle slots sequentially fill honge**
                    val spaceLeft = maxSize - currentBitmaps.size
                    val imagesToAdd = newBitmapStrings.take(spaceLeft)
                    currentBitmaps.addAll(imagesToAdd)
                }

                if (newBitmapStrings.size > (maxSize - currentBitmaps.size)) {
                    // **Step 2: Agar list full ho chuki, to oldest images replace karni shuru karo**
                    val overflow = newBitmapStrings.size - (maxSize - currentBitmaps.size)
                    currentBitmaps =
                        (currentBitmaps.drop(overflow) + newBitmapStrings).takeLast(maxSize).toMutableList()
                }

                viewModel.updateImages(currentBitmaps)
            }
        }
    }





    private fun updateImageViews(bitmaps: List<Bitmap>) {
        for (i in imageViews.indices) {
            if (i < bitmaps.size) {
                imageViews[i].setImageBitmap(bitmaps[i])
                imageViews[i].visibility = View.VISIBLE
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
