package com.ssafy.storycut.data.repository

import com.ssafy.storycut.data.api.model.UserInfo
import com.ssafy.storycut.data.local.dao.UserDao
import com.ssafy.storycut.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 사용자 정보를 관리하는 리포지토리
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    /**
     * 사용자 정보를 Room 데이터베이스에 저장
     */
    suspend fun saveUser(userInfo: UserInfo) {
        val userEntity = UserEntity(
            email = userInfo.email,
            name = userInfo.name,
            nickname = userInfo.nickname,
            profileImg = userInfo.profileImg,
            createdAt = userInfo.createdAt,
            updatedAt = userInfo.updatedAt
        )
        userDao.insertUser(userEntity)
    }
    
    /**
     * 현재 로그인된 사용자 정보를 Flow로 가져오기
     */
    fun getCurrentUser(): Flow<UserInfo?> {
        return userDao.getLatestUser().map { entity ->
            entity?.let {
                UserInfo(
                    email = it.email,
                    name = it.name,
                    nickname = it.nickname,
                    profileImg = it.profileImg,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }
    }
    
    /**
     * 특정 이메일의 사용자 정보 가져오기
     */
    fun getUserByEmail(email: String): Flow<UserInfo?> {
        return userDao.getUserByEmail(email).map { entity ->
            entity?.let {
                UserInfo(
                    email = it.email,
                    name = it.name,
                    nickname = it.nickname,
                    profileImg = it.profileImg,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }
    }
    
    /**
     * 로그아웃 - 모든 사용자 정보 삭제
     */
    suspend fun logout() {
        userDao.deleteAllUsers()
    }
}