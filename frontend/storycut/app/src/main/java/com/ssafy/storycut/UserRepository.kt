package com.ssafy.storycut

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {

    suspend fun saveUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun getLastLoggedInUser() = withContext(Dispatchers.IO) {
        userDao.getLastLoggedInUser()
    }

    suspend fun getUserByEmail(email: String) = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }
}