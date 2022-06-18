package com.example.socialconnect

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.socialconnect.daos.PostDao

class CreatePostActivity : AppCompatActivity() {
    private lateinit var postDao: PostDao
    private lateinit var postButton: Button
    private lateinit var postInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)


        postDao = PostDao()
        postButton = findViewById(R.id.postBtn)
        postInput = findViewById(R.id.postTextInput)

        postButton.setOnClickListener {
            val input = postInput.text.toString().trim()
            if(input.isNotEmpty()) {
                postDao.addPost(input)
                finish()
            }
        }
    }

}