package com.ssafy.storycut.data.repository

import com.ssafy.storycut.data.api.model.UserInfo
import com.ssafy.storycut.data.api.service.AuthApiService
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
    private val userDao: UserDao,
    private val authApiService: AuthApiService  // AuthApiService 의존성 추가
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

    /**
     * 특정 ID의 사용자 정보를 API로 조회하기
     */
    suspend fun getMemberById(authToken: String, memberId: Long): Result<UserInfo> {
        return try {
            val response = authApiService.getMemberById("Bearer $authToken", memberId)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Result.success(response.body()?.result!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "API 조회 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}