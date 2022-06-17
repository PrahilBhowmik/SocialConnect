package com.example.socialconnect.daos

import com.example.socialconnect.models.Post
import com.example.socialconnect.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDao {

    private val db = FirebaseFirestore.getInstance()
    val postCollections = db.collection("posts")
    private val auth = Firebase.auth
    private val userDao = UserDao()

    fun addPost(text: String) {
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!
            val currentTime = System.currentTimeMillis()
            val post = Post(text, user,currentUserId, currentTime)
            postCollections.document().set(post)
        }
    }

    private fun getPostById(postId: String): Task<DocumentSnapshot> {
        return postCollections.document(postId).get()
    }

    fun updateLikes(postId: String) {
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)!!
            val isLiked = post.likedBy.contains(currentUserId)

            if(isLiked) {
                post.likedBy.remove(currentUserId)
            } else {
                post.likedBy.add(currentUserId)
            }
            postCollections.document(postId).set(post)
        }
    }

    fun updatePosts(){
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!
            postCollections.whereEqualTo("userId", currentUserId).get()
                .addOnSuccessListener { documents->
                    for (document in documents) {
                        GlobalScope.launch {
                            val postId =document.id
                            val post = getPostById(postId).await().toObject(Post::class.java)!!
                            val newPost = Post(post.text,user,post.userId,post.createdAt,post.likedBy)
                            postCollections.document(postId).set(newPost)
                        }
                    }
                }
        }
    }
}