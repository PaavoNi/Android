package com.example.composetutorial

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_data WHERE id = 1")
    fun getUserData(): Flow<UserData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserData(userData: UserData)

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages")
    suspend fun getAllMessagesFirst(): List<MessageEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
}
