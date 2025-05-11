package com.ssafy.storycut.data.repository

import com.ssafy.storycut.data.api.model.BaseResponse
import com.ssafy.storycut.data.api.model.room.RoomDto
import com.ssafy.storycut.data.api.model.MemberDto
import com.ssafy.storycut.data.api.model.room.CreateRoomRequest
import com.ssafy.storycut.data.api.service.RoomApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepository @Inject constructor(
    private val roomApiService: RoomApiService
) {
    // 내 공유방 목록 조회
    suspend fun getMyRooms(token: String): Response<BaseResponse<List<RoomDto>>> {
        return roomApiService.getMyRooms("Bearer $token")
    }

    // 공유방 생성
    suspend fun createRoom(request: CreateRoomRequest, token: String): Response<BaseResponse<RoomDto>> {
        return roomApiService.createRoom(request, "Bearer $token")
    }

    // 공유방 삭제
    suspend fun deleteRoom(roomId: String, token: String): Response<BaseResponse<Boolean>> {
        return roomApiService.deleteRoom(roomId, "Bearer $token")
    }

    // 공유방 수정
    suspend fun updateRoom(roomDto: RoomDto, token: String): Response<BaseResponse<RoomDto>> {
        return roomApiService.updateRoom(roomDto, "Bearer $token")
    }

    // 공유방 나가기
    suspend fun leaveRoom(roomId: String, token: String): Response<BaseResponse<Boolean>> {
        return roomApiService.leaveRoom(roomId, "Bearer $token")
    }

    // 공유방 초대코드 생성
    suspend fun createInviteCode(roomId: String, token: String): Response<BaseResponse<String>> {
        return roomApiService.createInviteCode(roomId, "Bearer $token")
    }

    // 공유방 입장
    suspend fun enterRoom(inviteCode: String, token: String): Response<BaseResponse<RoomDto>> {
        return roomApiService.enterRoom(inviteCode, "Bearer $token")
    }

    // 공유방 참여자 목록 조회
    suspend fun getRoomMembers(roomId: String, token: String): Response<BaseResponse<List<MemberDto>>> {
        return roomApiService.getRoomMembers(roomId, "Bearer $token")
    }

    // 공유방 상세 정보 조회
    suspend fun getRoomDetail(roomId: String, token: String): Response<BaseResponse<RoomDto>> {
        return roomApiService.getRoomDetail(roomId, "Bearer $token")
    }

    // 초대코드로 공유방 ID 조회
    suspend fun decodeInviteCode(inviteCode: String, token: String): Response<BaseResponse<String>> {
        return roomApiService.decodeInviteCode(inviteCode, "Bearer $token")
    }
}