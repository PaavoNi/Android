package com.example.composetutorial

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserData::class, MessageEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
