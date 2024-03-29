package com.example.freeimagesearch.data.models

data class User(
    val bio: String,
    val id: String,
    val instagram_username: String,
    val links: LinksX,
    val location: String,
    val name: String,
    val portfolio_url: String,
    val total_collections: Int,
    val total_likes: Int,
    val total_photos: Int,
    val twitter_username: String,
    val updated_at: String,
    val username: String,
    val profile_image: ProfileImageUrls? = null
)