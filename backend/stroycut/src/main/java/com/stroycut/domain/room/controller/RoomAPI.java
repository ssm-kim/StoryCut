package com.stroycut.domain.room.controller;

import com.stroycut.domain.room.dto.request.RoomCreateRequest;
import com.stroycut.domain.room.dto.request.RoomInviteRequest;
import com.stroycut.domain.room.dto.request.RoomUpdateRequest;
import com.stroycut.domain.room.dto.response.RoomMemberResponse;
import com.stroycut.domain.room.dto.response.RoomResponse;
import com.stroycut.global.model.dto.BaseResponse;
import com.stroycut.global.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공유방 관련 API 인터페이스
 */
@RequestMapping("/api/room")
public interface RoomAPI {

    /**
     * 공유방 생성 API
     */
    @PostMapping
    ResponseEntity<BaseResponse<RoomResponse>> createRoom(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @RequestBody RoomCreateRequest request);

    /**
     * 내 공유방 목록 조회 API
     */
    @GetMapping
    ResponseEntity<BaseResponse<List<RoomResponse>>> getMyRooms(
            @AuthenticationPrincipal CustomUserDetails authUser);

    /**
     * 공유방 수정 API
     */
    @PatchMapping
    ResponseEntity<BaseResponse<RoomResponse>> updateRoom(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @RequestParam Long roomId,
            @Valid @RequestBody RoomUpdateRequest request);

    /**
     * 공유방 삭제 API
     */
    @DeleteMapping
    ResponseEntity<BaseResponse<String>> deleteRoom(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @RequestParam Long roomId);

    /**
     * 공유방 초대 API
     */
    @PostMapping("/invite")
    ResponseEntity<BaseResponse<RoomMemberResponse>> inviteMember(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @RequestParam Long roomId,
            @Valid @RequestBody RoomInviteRequest request);

    /**
     * 공유방 입장 API
     */
    @PostMapping("/enter")
    ResponseEntity<BaseResponse<RoomResponse>> enterRoom(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @RequestParam Long roomId,
            @RequestParam(required = false) String password);

    /**
     * 공유방 나가기 API
     */
    @PostMapping("/leave")
    ResponseEntity<BaseResponse<String>> leaveRoom(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @RequestParam Long roomId);

    /**
     * 공유방 상세 정보 조회 API
     */
    @GetMapping("/detail/{roomId}")
    ResponseEntity<BaseResponse<RoomResponse>> getRoomDetail(
            @PathVariable Long roomId);

    /**
     * 공유방 참여자 목록 조회 API
     */
    @GetMapping("/members/{roomId}")
    ResponseEntity<BaseResponse<List<RoomMemberResponse>>> getRoomMembers(
            @PathVariable Long roomId);
}