package com.example.mobilecomputing.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM user_data LIMIT 1")
    suspend fun getUser(): User?

    @Update
    suspend fun updateUser(user: User)
}
