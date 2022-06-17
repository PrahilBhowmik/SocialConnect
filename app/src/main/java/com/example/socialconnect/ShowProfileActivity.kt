package com.example.socialconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialconnect.daos.PostDao
import com.example.socialconnect.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase

class ShowProfileActivity : AppCompatActivity(), IPostAdapter {

    private lateinit var editProfileButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var profilePic: ImageView
    private lateinit var profileName: TextView
    private lateinit var postDao: PostDao
    private lateinit var adapter:PostAdapter
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        profilePic = findViewById(R.id.profilePic)
        profileName = findViewById(R.id.profileName)
        editProfileButton = findViewById(R.id.editProfileBtn)

        val user = Firebase.auth.currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            if(name ==""){
                val intent = Intent(this,EditProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
            val photoUrl = user.photoUrl
            uid = user.uid

            Glide.with(profilePic.context).load(photoUrl).circleCrop().into(profilePic)
            profileName.text = name
            setUpRecyclerView()
        }

        editProfileButton.setOnClickListener {
            val intent = Intent(this,EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpRecyclerView() {
        val user = uid
        postDao = PostDao()
        val postsCollections = postDao.postCollections
        val query = postsCollections.orderBy("createdAt", Query.Direction.DESCENDING).whereEqualTo("userId",user)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        adapter = PostAdapter(recyclerViewOptions, this)

        recyclerView = findViewById(R.id.profileRecyclerView)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
        postDao.updateLikes(postId)
    }

}