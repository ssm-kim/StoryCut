package com.stroycut.domain.room.controller;

import com.stroycut.domain.room.dto.request.RoomCreateRequest;
import com.stroycut.domain.room.dto.request.RoomInviteRequest;
import com.stroycut.domain.room.dto.request.RoomUpdateRequest;
import com.stroycut.domain.room.dto.response.RoomMemberResponse;
import com.stroycut.domain.room.dto.response.RoomResponse;
import com.stroycut.domain.room.service.RoomService;
import com.stroycut.global.model.dto.BaseResponse;
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
            CustomUserDetails authUser, RoomCreateRequest request) {
        
        RoomResponse response = roomService.createRoom(authUser.getMemberId(), request);
        return ResponseEntity.ok(new BaseResponse<> (response));
    }

    @Override
    public ResponseEntity<BaseResponse<List<RoomResponse>>> getMyRooms(
            CustomUserDetails authUser) {
        
        List<RoomResponse> response = roomService.getMyRooms(authUser.getMemberId());
        return ResponseEntity.ok(new BaseResponse<> (response));
    }

    @Override
    public ResponseEntity<BaseResponse<RoomResponse>> updateRoom(
            CustomUserDetails authUser, Long roomId, RoomUpdateRequest request) {
        
        RoomResponse response = roomService.updateRoom(authUser.getMemberId(), roomId, request);
        return ResponseEntity.ok(new BaseResponse<> (response));
    }

    @Override
    public ResponseEntity<BaseResponse<String>> deleteRoom(
            CustomUserDetails authUser, Long roomId) {
        
        roomService.deleteRoom(authUser.getMemberId(), roomId);
        return ResponseEntity.ok(new BaseResponse<> (roomId + "방이 삭제되었습니다."));
    }

    @Override
    public ResponseEntity<BaseResponse<RoomMemberResponse>> inviteMember(
            CustomUserDetails authUser, Long roomId, RoomInviteRequest request) {
        
        RoomMemberResponse response = roomService.inviteMember(
                authUser.getMemberId(), roomId, request.inviteMemberId());
        return ResponseEntity.ok(new BaseResponse<> (response));
    }

    @Override
    public ResponseEntity<BaseResponse<RoomResponse>> enterRoom(
            CustomUserDetails authUser, Long roomId, String password) {
        
        RoomResponse response = roomService.enterRoom(authUser.getMemberId(), roomId, password);
        return ResponseEntity.ok(new BaseResponse<> (response));
    }

    @Override
    public ResponseEntity<BaseResponse<String>> leaveRoom(
            CustomUserDetails authUser, Long roomId) {
        
        roomService.leaveRoom(authUser.getMemberId(), roomId);
        return ResponseEntity.ok(new BaseResponse<> (roomId + "번 방에서 나갔습니다."));
    }

    @Override
    public ResponseEntity<BaseResponse<RoomResponse>> getRoomDetail(
            Long roomId) {
        
        RoomResponse response = roomService.getRoomDetail(roomId);
        return ResponseEntity.ok(new BaseResponse<> (response));
    }

    @Override
    public ResponseEntity<BaseResponse<List<RoomMemberResponse>>> getRoomMembers(
            Long roomId) {
        
        List<RoomMemberResponse> response = roomService.getRoomMembers(roomId);
        return ResponseEntity.ok(new BaseResponse<> (response));
    }
}