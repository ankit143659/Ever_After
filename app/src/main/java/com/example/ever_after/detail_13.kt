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
    private val viewModel: dataViewModel by viewModels()
    private lateinit var loadingDialog: Dialog
    private var selectedImageView: ImageView? = null


    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageView?.let {
                        loadingDialog.show()
                        processImage(uri, it)  // ✅ Ab ImageView pass ho rha h
                    }
                }
            }
        }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_blank, container, false)
        setupLoadingDialog()

       val imageViews = listOf(
            view.findViewById(R.id.img1),
            view.findViewById(R.id.img2),
            view.findViewById<ImageView>(R.id.img3),
            view.findViewById<ImageView>(R.id.img4),
            view.findViewById<ImageView>(R.id.img5),
            view.findViewById<ImageView>(R.id.img6)
        )

        imageViews.forEachIndexed { index, imageView ->
            imageView.tag = index  // 🔹 ImageView ke tag me position store karna
            imageView.setOnClickListener { openImagePicker(imageView, index) }
        }



        return view
    }

    private fun openImagePicker(imageView: ImageView, position: Int) {
        selectedImageView = imageView
        selectedImageView?.tag = position  // 🔹 Position store karna
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        imagePickerLauncher.launch(intent)
    }



    private fun setupLoadingDialog() {
        loadingDialog = Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.loading_dialog)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun processImage(uri: Uri, imageView: ImageView) {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = getBitmapFromUri(uri)
            val bitmapString = bitmap?.let { encodeToBase64(it) }
            val position = imageView.tag as? Int ?: return@launch

            withContext(Dispatchers.Main) {
                bitmap?.let { imageView.setImageBitmap(it) }
                bitmapString?.let {if ( viewModel.updateImage(bitmap, position)){
                    loadingDialog.dismiss()
                }else{
                    loadingDialog.dismiss()
                }
                }  // 🔹 Position ke saath update
            }
        }
    }




    fun decodeBase64(encodedString: String): Bitmap? {
        val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }


    private suspend fun getBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.e("ImagePicker", "Error loading bitmap", e)
            null
        }
    }

    private suspend fun encodeToBase64(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val scaledBitmap = compressBitmap(bitmap, 800) // Resize and compress
        ByteArrayOutputStream().apply {
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 40, this) // JPEG 60% quality
        }.toByteArray().let { Base64.encodeToString(it, Base64.DEFAULT) }
    }

    private fun compressBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat() / height.toFloat()

        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / aspectRatio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

}
