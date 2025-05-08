package com.storycut.domain.room.controller;

import com.storycut.domain.auth.model.CustomUserDetails;
import com.storycut.domain.room.dto.request.RoomCreateRequest;
import com.storycut.domain.room.dto.request.RoomInviteRequest;
import com.storycut.domain.room.dto.request.RoomUpdateRequest;
import com.storycut.domain.room.dto.response.RoomMemberResponse;
import com.storycut.domain.room.dto.response.RoomResponse;
import com.storycut.global.model.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공유방 관련 API 인터페이스
 */
@RequestMapping("/api/room")
@Tag(name = "Room", description = "공유방 API - MCP(Model Context Protocol)에 따라 설계됨")
public interface RoomAPI {

    /**
     * 공유방 생성 API
     */
    @Operation(
        summary = "공유방 생성",
        description = "새로운 공유방을 생성합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 공유방이 생성됨",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값이 있는 경우"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping
    ResponseEntity<BaseResponse<RoomResponse>> createRoom(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails authUser,
        @Valid @RequestBody RoomCreateRequest request);

    /**
     * 내 공유방 목록 조회 API
     */
    @Operation(
        summary = "내 공유방 목록 조회",
        description = "회원이 참여 중인 모든 공유방 목록을 조회합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 공유방 목록을 조회함",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping
    ResponseEntity<BaseResponse<List<RoomResponse>>> getMyRooms(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails authUser);

    /**
     * 공유방 수정 API
     */
    @Operation(
        summary = "공유방 수정",
        description = "공유방 정보를 수정합니다. 방장만 수정할 수 있습니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 공유방 정보가 수정됨",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값이 있는 경우"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "방장이 아닌 경우"),
        @ApiResponse(responseCode = "404", description = "해당 공유방이 존재하지 않는 경우")
    })
    @PatchMapping
    ResponseEntity<BaseResponse<RoomResponse>> updateRoom(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails authUser,
        @Parameter(description = "수정할 공유방 ID", required = true) @RequestParam Long roomId,
        @Valid @RequestBody RoomUpdateRequest request);

    /**
     * 공유방 삭제 API
     */
    @Operation(
        summary = "공유방 삭제",
        description = "공유방을 삭제합니다. 방장만 삭제할 수 있습니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 공유방이 삭제됨",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "방장이 아닌 경우"),
        @ApiResponse(responseCode = "404", description = "해당 공유방이 존재하지 않는 경우")
    })
    @DeleteMapping
    ResponseEntity<BaseResponse<String>> deleteRoom(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails authUser,
        @Parameter(description = "삭제할 공유방 ID", required = true) @RequestParam Long roomId);

    /**
     * 공유방 초대 API
     */
    @Operation(
        summary = "공유방 초대",
        description = "공유방에 새로운 멤버를 초대합니다. 방장만 초대할 수 있습니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 멤버 초대됨",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "400", description = "이미 참여 중인 멤버인 경우"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "방장이 아닌 경우"),
        @ApiResponse(responseCode = "404", description = "해당 공유방이 존재하지 않는 경우")
    })
    @PostMapping("/invite")
    ResponseEntity<BaseResponse<RoomMemberResponse>> inviteMember(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails authUser,
        @Parameter(description = "초대할 공유방 ID", required = true) @RequestParam Long roomId,
        @Valid @RequestBody RoomInviteRequest request);

    /**
     * 공유방 입장 API
     */
    @Operation(
        summary = "공유방 입장",
        description = "공유방에 입장합니다. 비밀번호가 설정된 공유방은 비밀번호 검증이 필요합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 공유방에 입장함",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "400", description = "비밀번호가 일치하지 않는 경우"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "해당 공유방이 존재하지 않는 경우")
    })
    @PostMapping("/enter")
    ResponseEntity<BaseResponse<RoomResponse>> enterRoom(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails authUser,
        @Parameter(description = "입장할 공유방 ID", required = true) @RequestParam Long roomId,
        @Parameter(description = "공유방 비밀번호 (선택사항)") @RequestParam(required = false) String password);

    /**
     * 공유방 나가기 API
     */
    @Operation(
        summary = "공유방 나가기",
        description = "공유방에서 나갑니다. 방장이 나갈 경우: 1. 방에 남은 다른 멤버가 있을 경우, 가입 시간이 가장 오래된 멤버가 새로운 방장으로 지정됩니다. 2. 방에 남은 다른 멤버가 없을 경우, 방은 자동으로 삭제됩니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 공유방에서 나감",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "해당 공유방이 존재하지 않는 경우")
    })
    @PostMapping("/leave")
    ResponseEntity<BaseResponse<String>> leaveRoom(
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails authUser,
        @Parameter(description = "나갈 공유방 ID", required = true) @RequestParam Long roomId);

    /**
     * 공유방 상세 정보 조회 API
     */
    @Operation(
        summary = "공유방 상세 정보 조회",
        description = "공유방의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 공유방 상세 정보를 조회함",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "404", description = "해당 공유방이 존재하지 않는 경우")
    })
    @GetMapping("/detail/{roomId}")
    ResponseEntity<BaseResponse<RoomResponse>> getRoomDetail(
        @Parameter(description = "조회할 공유방 ID", required = true) @PathVariable Long roomId);

    /**
     * 공유방 참여자 목록 조회 API
     */
    @Operation(
        summary = "공유방 참여자 목록 조회",
        description = "공유방 참여자 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 공유방 참여자 목록을 조회함",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "404", description = "해당 공유방이 존재하지 않는 경우")
    })
    @GetMapping("/members/{roomId}")
    ResponseEntity<BaseResponse<List<RoomMemberResponse>>> getRoomMembers(
        @Parameter(description = "조회할 공유방 ID", required = true) @PathVariable Long roomId);
}