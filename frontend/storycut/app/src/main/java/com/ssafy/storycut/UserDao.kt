package com.ssafy.storycut

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY lastLoginTime DESC LIMIT 1")
    suspend fun getLastLoggedInUser(): UserEntity?
}