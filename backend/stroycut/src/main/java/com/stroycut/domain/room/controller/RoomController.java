package com.stroycut.domain.room.controller;

import com.stroycut.domain.room.dto.request.RoomCreateRequest;
import com.stroycut.domain.room.dto.request.RoomInviteRequest;
import com.stroycut.domain.room.dto.request.RoomUpdateRequest;
import com.stroycut.domain.room.dto.response.RoomMemberResponse;
import com.stroycut.domain.room.dto.response.RoomResponse;
import com.stroycut.domain.room.service.RoomService;
import com.stroycut.global.model.dto.BaseResponse;
import com.stroycut.global.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController implements RoomAPI {

    private final RoomService roomService;
    
    @Override
    public ResponseEntity<BaseResponse<RoomResponse>> createRoom(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody RoomCreateRequest request) {
        
        RoomResponse response = roomService.createRoom(authUser.getId(), request);
        return ResponseEntity.ok(new BaseResponse<> (response));
    }

    @Override
    public ResponseEntity<BaseResponse<List<RoomResponse>>> getMyRooms(
            @AuthenticationPrincipal AuthUser authUser) {
        
        List<RoomResponse> response = roomService.getMyRooms(authUser.getId());
        return ResponseEntity.ok(new BaseResponse<> (response));
    }

    @Override
    public ResponseEntity<BaseResponse<RoomResponse>> updateRoom(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam Long roomId,
            @Valid @RequestBody RoomUpdateRequest request) {
        
        RoomResponse response = roomService.updateRoom(authUser.getId(), roomId, request);
        return ResponseEntity.ok(new BaseResponse<> (response));
    }

    @Override
    public ResponseEntity<BaseResponse<String>> deleteRoom(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam Long roomId) {
        
        roomService.deleteRoom(authUser.getId(), roomId);
        return ResponseEntity.ok(new BaseResponse<> (roomId + "방이 삭제되었습니다."));
    }

    @Override
    public ResponseEntity<BaseResponse<RoomMemberResponse>> inviteMember(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam Long roomId,
            @Valid @RequestBody RoomInviteRequest request) {
        
        RoomMemberResponse response = roomService.inviteMember(
                authUser.getId(), roomId, request.inviteMemberId());
        return ResponseEntity.ok(new BaseResponse<> (response));
    }

    @Override
    public ResponseEntity<BaseResponse<RoomResponse>> enterRoom(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam Long roomId,
            @RequestParam(required = false) String password) {
        
        RoomResponse response = roomService.enterRoom(authUser.getId(), roomId, password);
        return ResponseEntity.ok(new BaseResponse<> (response));
    }

    @Override
    public ResponseEntity<BaseResponse<String>> leaveRoom(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam Long roomId) {
        
        roomService.leaveRoom(authUser.getId(), roomId);
        return ResponseEntity.ok(new BaseResponse<> (roomId + "번 방에서 나갔습니다."));
    }

    @Override
    public ResponseEntity<BaseResponse<RoomResponse>> getRoomDetail(
            @PathVariable Long roomId) {
        
        RoomResponse response = roomService.getRoomDetail(roomId);
        return ResponseEntity.ok(new BaseResponse<> (response));
    }

    @Override
    public ResponseEntity<BaseResponse<List<RoomMemberResponse>>> getRoomMembers(
            @PathVariable Long roomId) {
        
        List<RoomMemberResponse> response = roomService.getRoomMembers(roomId);
        return ResponseEntity.ok(new BaseResponse<> (response));
    }
}