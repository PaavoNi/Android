package com.example.composetutorial

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_data")
data class UserData(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "image_uri") val imageUri: String
)
