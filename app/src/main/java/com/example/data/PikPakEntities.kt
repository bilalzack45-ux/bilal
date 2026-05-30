package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val followers: Int,
    val following: Int,
    val likes: Int,
    val isVerified: Boolean,
    val creatorCategory: String,
    val balanceCoins: Double,
    val avatarUrl: String,
    val coverUrl: String
)

@Entity(tableName = "draft_videos")
data class DraftVideo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val tags: String,
    val resolution: String,
    val duration: Int,
    val filterName: String,
    val beautyLevel: Int,
    val location: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // RECHARGE, WITHDRAWAL, GIFT_SENT, CREATOR_PAYOUT
    val amount: Double,
    val methodOrGift: String, // e.g. "PayPal", "Gold Diamond Gift", "Wise"
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatId: String,
    val sender: String, // "Me", "Creator", "Friend"
    val text: String,
    val mediaType: String = "TEXT", // TEXT, IMAGE, VOICE
    val timestamp: Long = System.currentTimeMillis()
)
