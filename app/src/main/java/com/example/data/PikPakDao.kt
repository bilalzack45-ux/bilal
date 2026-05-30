package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = :id_")
    fun getUser(id_: String): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)

    @Query("UPDATE user_profiles SET balanceCoins = :coins WHERE id = :id_")
    suspend fun updateCoins(id_: String, coins: Double)
}

@Dao
interface DraftVideoDao {
    @Query("SELECT * FROM draft_videos ORDER BY timestamp DESC")
    fun getAllDrafts(): Flow<List<DraftVideo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: DraftVideo)

    @Delete
    suspend fun deleteDraft(draft: DraftVideo)
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: WalletTransaction)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId_ ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId_: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: ChatMessage)
}
