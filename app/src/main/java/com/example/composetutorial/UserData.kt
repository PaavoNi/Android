package com.example.composetutorial

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_data")
data class UserData(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "image_uri") val imageUri: String
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user_data WHERE id = 1")
    fun getUserData(): Flow<UserData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserData(userData: UserData)
}

@Database(entities = [UserData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}