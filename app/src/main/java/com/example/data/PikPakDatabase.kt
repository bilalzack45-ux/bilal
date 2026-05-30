package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserProfile::class, DraftVideo::class, WalletTransaction::class, ChatMessage::class],
    version = 1,
    exportSchema = false
)
abstract class PikPakDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun draftVideoDao(): DraftVideoDao
    abstract fun walletTransactionDao(): WalletTransactionDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: PikPakDatabase? = null

        fun getDatabase(context: Context): PikPakDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PikPakDatabase::class.java,
                    "pikpak_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
