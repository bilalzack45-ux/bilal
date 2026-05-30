package com.example.data

import kotlinx.coroutines.flow.Flow

class PikPakRepository(private val db: PikPakDatabase) {
    val userProfile: Flow<UserProfile?> = db.userProfileDao().getUser("me")
    val allDrafts: Flow<List<DraftVideo>> = db.draftVideoDao().getAllDrafts()
    val allTransactions: Flow<List<WalletTransaction>> = db.walletTransactionDao().getAllTransactions()

    fun getChatMessages(chatId: String): Flow<List<ChatMessage>> {
        return db.chatMessageDao().getMessagesForChat(chatId)
    }

    suspend fun saveProfile(profile: UserProfile) {
        db.userProfileDao().insertOrUpdate(profile)
    }

    suspend fun updateProfileCoins(coins: Double) {
        db.userProfileDao().updateCoins("me", coins)
    }

    suspend fun addDraft(draft: DraftVideo) {
        db.draftVideoDao().insertDraft(draft)
    }

    suspend fun deleteDraft(draft: DraftVideo) {
        db.draftVideoDao().deleteDraft(draft)
    }

    suspend fun addTransaction(tx: WalletTransaction) {
        db.walletTransactionDao().insertTransaction(tx)
    }

    suspend fun sendMessage(msg: ChatMessage) {
        db.chatMessageDao().insertMessage(msg)
    }
}
