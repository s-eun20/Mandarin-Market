package com.example.market

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.lifecycle.lifecycleScope
import com.example.market.databinding.FragmentWriteBinding
import com.example.market.model.Product
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WriteFragment : BaseFragment() {
    private var _binding: FragmentWriteBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { Firebase.auth }
    private val db by lazy { Firebase.firestore }

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            nameContent.setText(auth.currentUser?.email ?: "")

            addImageButton.setOnClickListener {
                openFileChooser()
            }

            button.setOnClickListener {
                lifecycleScope.launch {
                    saveDataToFirestore()
                    showToastMessage("글쓰기 성공")
                    onBackPressed()
                }
            }
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data

            lifecycleScope.launch {
                saveDataToFirestore()

                showToastMessage("글쓰기 성공")
                onBackPressed()
            }
        }
    }

    private suspend fun saveDataToFirestore() = coroutineScope {
        val title = binding.titleContent.text.toString().trim()
        val price = binding.priceContent.text.toString().trim()
        val content = binding.contentView.text.toString().trim()
        val name = binding.nameContent.text.toString().trim()
        var imageUrl = ""

        launch(Dispatchers.IO) {
            if (imageUri != null) {
                val storageRef = FirebaseStorage.getInstance().getReference("item_images")
                val imageName = "${System.currentTimeMillis()}.${getFileExtension(imageUri!!)}"

                try {
                    storageRef.child(imageName).putFile(imageUri!!).await()
                    imageUrl = (storageRef.child(imageName).downloadUrl.await()).toString()

                } catch (e: Exception) {
                    e.printStackTrace()

                    launch(Dispatchers.Main) {
                        showToastMessage("Error uploading image")
                    }

                    return@launch
                }
            }

            val product =
                Product("", auth.uid!!, title, price, imageUrl, content, "판매중", name)

            db.collection("products").add(product)
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = requireActivity().contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }
}
