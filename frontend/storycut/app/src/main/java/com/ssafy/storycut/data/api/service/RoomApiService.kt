package com.ssafy.storycut.data.api.service

import com.ssafy.storycut.data.api.model.BaseResponse
import com.ssafy.storycut.data.api.model.MemberDto
import com.ssafy.storycut.data.api.model.room.CreateRoomRequest
import com.ssafy.storycut.data.api.model.room.RoomDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RoomApiService {

    // 내 공유방 목록 조회
    @GET("api/room")
    suspend fun getMyRooms(@Header("Authorization") token: String): Response<BaseResponse<List<RoomDto>>>

    // 공유방 생성
    @POST("api/room")
    suspend fun createRoom(@Body roomDto: CreateRoomRequest, @Header("Authorization") token: String): Response<BaseResponse<RoomDto>>

    // 공유방 삭제
    @DELETE("api/room")
    suspend fun deleteRoom(@Query("roomId") roomId: String, @Header("Authorization") token: String): Response<BaseResponse<Boolean>>

    // 공유방 수정
    @PATCH("api/room")
    suspend fun updateRoom(@Body roomDto: RoomDto, @Header("Authorization") token: String): Response<BaseResponse<RoomDto>>

    // 공유방 나가기
    @POST("api/room/leave")
    suspend fun leaveRoom(@Query("roomId") roomId: String, @Header("Authorization") token: String): Response<BaseResponse<Boolean>>

    // 공유방 초대코드 생성
    @POST("api/room/invite")
    suspend fun createInviteCode(@Query("roomId") roomId: String, @Header("Authorization") token: String): Response<BaseResponse<String>>

    // 공유방 입장
    @POST("api/room/enter")
    suspend fun enterRoom(@Query("inviteCode") inviteCode: String, @Header("Authorization") token: String): Response<BaseResponse<RoomDto>>

    // 공유방 참여자 목록 조회
    @GET("api/room/members/{roomId}")
    suspend fun getRoomMembers(@Path("roomId") roomId: String, @Header("Authorization") token: String): Response<BaseResponse<List<MemberDto>>>

    // 공유방 상세 정보 조회
    @GET("api/room/detail/{roomId}")
    suspend fun getRoomDetail(@Path("roomId") roomId: String, @Header("Authorization") token: String): Response<BaseResponse<RoomDto>>

    // 초대코드로 공유방 ID 조회
    @GET("api/room/decode")
    suspend fun decodeInviteCode(@Query("inviteCode") inviteCode: String, @Header("Authorization") token: String): Response<BaseResponse<String>>

}