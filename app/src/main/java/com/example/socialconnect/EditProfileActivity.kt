package com.example.socialconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.socialconnect.daos.PostDao
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImg: ImageView
    private lateinit var editImgBtn: Button
    private lateinit var editName: EditText
    private lateinit var saveBtn: Button
    private lateinit var logOutBtn: Button
    private lateinit var deleteBtn: Button
    private lateinit var storage: FirebaseStorage
    private lateinit var downloadUri: Uri
    private lateinit var progressBar: ProgressBar
    private lateinit var actionText: TextView
    private lateinit var linearLayout: LinearLayout
    private val pickImage = 100
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImg = findViewById(R.id.profileImg)
        editName = findViewById(R.id.editName)
        editImgBtn = findViewById(R.id.imgEditBtn)
        saveBtn = findViewById(R.id.saveBtn)
        logOutBtn = findViewById(R.id.logOutBtn)
        deleteBtn = findViewById(R.id.deleteBtn)
        progressBar = findViewById(R.id.progressBar2)
        actionText = findViewById(R.id.actionText)
        linearLayout = findViewById(R.id.linLayoutEp)

        logOutBtn.setOnClickListener {
            logOut()
        }

        deleteBtn.setOnClickListener {
            Toast.makeText(this,"Not Yet Implemented",Toast.LENGTH_SHORT).show()
        }

        val user = Firebase.auth.currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val photoUrl = user.photoUrl
            val uid = user.uid
            downloadUri = photoUrl
                ?: Uri.parse("https://firebasestorage.googleapis.com/v0/b/social-connect-f289d." +
                        "appspot.com/o/images%2FemptyUserImg.png?alt=media&token=8eb3221d-4ef7-4a51-8efb-da9f5114e31f")

            Glide.with(profileImg.context).load(photoUrl).circleCrop().into(profileImg)
            editName.setText(name)

            storage=FirebaseStorage.getInstance()
            saveBtn.setOnClickListener {
                val profileUpdates = userProfileChangeRequest {
                    displayName = editName.text.toString()
                    photoUri = Uri.parse(downloadUri.toString())
                }

                if(!profileUpdates.displayName.isNullOrBlank()){
                    user.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                PostDao().updatePosts()
                                Toast.makeText(this,"Profile updated successfully",Toast.LENGTH_SHORT).show()
                                val intent = Intent(this,SignInActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                }
                else
                    Toast.makeText(this,"Name cannot be empty",Toast.LENGTH_SHORT).show()
            }

            editImgBtn.setOnClickListener {
                selectImage()
            }

        }
    }

    private fun selectImage(){
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, pickImage)

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            imageUri?.let {cropImage()}
        }

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            imageUri = data?.let { UCrop.getOutput(it) }
            uploadImage()
        } else if (resultCode == UCrop.RESULT_ERROR) {
           Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun cropImage(){
        val destination = UUID.randomUUID().toString() + ".jpg"
        UCrop.of(imageUri!!, Uri.fromFile(File(cacheDir, destination)))
            .withAspectRatio(1F, 1F)
            .start(this)
    }

    private fun uploadImage(){
        progressBar.visibility=View.VISIBLE
        actionText.text="Uploading image"
        actionText.visibility=View.VISIBLE
        linearLayout.visibility=View.GONE
        if(imageUri != null){
            val storageReference = storage.reference
            val fileName=UUID.randomUUID().toString()
            val ref = storageReference.child("images/$fileName")
            val uploadTask = ref.putFile(imageUri!!)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUri = task.result
                    Glide.with(profileImg.context).load(downloadUri.toString()).circleCrop().into(profileImg)
                    Toast.makeText(this,"Successfully Uploaded",Toast.LENGTH_SHORT).show()
                    progressBar.visibility=View.GONE
                    actionText.visibility=View.GONE
                    linearLayout.visibility=View.VISIBLE
                } else {
                    Toast.makeText(this,"Couldn't upload image.\nPlease try again",Toast.LENGTH_SHORT).show()
                    progressBar.visibility=View.GONE
                    actionText.visibility=View.GONE
                    linearLayout.visibility=View.VISIBLE
                }
            }

        }else{
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
            progressBar.visibility=View.GONE
            actionText.visibility=View.GONE
            linearLayout.visibility=View.VISIBLE
        }
    }

    private fun logOut(){
        Firebase.auth.signOut()
        val intent = Intent(this,SignInActivity::class.java)
        finishAffinity()
        startActivity(intent)
    }

}

