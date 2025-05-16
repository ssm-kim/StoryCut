package com.ssafy.storycut.data.repository

import com.ssafy.storycut.data.api.model.BaseResponse
import com.ssafy.storycut.data.api.model.room.RoomDto
import com.ssafy.storycut.data.api.model.MemberDto
import com.ssafy.storycut.data.api.model.chat.ChatDto
import com.ssafy.storycut.data.api.model.chat.ChatMessageRequest
import com.ssafy.storycut.data.api.model.room.CreateRoomRequest
import com.ssafy.storycut.data.api.service.ChatApiService
import com.ssafy.storycut.data.api.service.RoomApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepository @Inject constructor(
    private val roomApiService: RoomApiService,
    private val chatApiService: ChatApiService
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

    /**
     * 공유방에 비디오 업로드
     * @param roomId 공유방 ID
     * @param chatMessage 업로드할 채팅 메시지 요청 객체
     * @param token 사용자 인증 토큰
     * @return API 응답
     */
    suspend fun uploadRoomVideo(
        roomId: Long,
        chatMessage: ChatMessageRequest,
        token: String
    ): Response<BaseResponse<ChatDto>> {
        return chatApiService.uploadRoomVideo(roomId, chatMessage, "Bearer $token")
    }

    /**
     * 공유방의 채팅(비디오) 목록 조회
     * @param roomId 공유방 ID
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @param token 사용자 인증 토큰
     * @return API 응답
     */
    suspend fun getRoomVideos(
        roomId: Long,
        page: Int = 0,
        size: Int = 10,
        token: String
    ): Response<BaseResponse<List<ChatDto>>> {
        return chatApiService.getRoomVideos(roomId, page, size, "Bearer $token")
    }

    /**
     * 공유방 비디오 상세 조회
     * @param chatId 채팅 메시지 ID
     * @param token 사용자 인증 토큰
     * @return API 응답
     */
    suspend fun getChatDetail(
        chatId: String,
        token: String
    ): Response<BaseResponse<ChatDto>> {
        return chatApiService.getChatDetail(chatId, "Bearer $token")
    }

    /**
     * 공유방 채팅(비디오) 삭제
     * @param chatId 채팅 ID
     * @param token 사용자 인증 토큰
     * @return API 응답
     */
    suspend fun deleteChat(
        chatId: String,
        token: String
    ): Response<BaseResponse<Boolean>> {
        return chatApiService.deleteChat(chatId, "Bearer $token")
    }


}