package com.example.socialconnect.models

data class Post (
    val text: String = "",
    val createdBy: User = User(),
    val userId: String = "",
    val createdAt: Long = 0L,
    val likedBy: ArrayList<String> = ArrayList())