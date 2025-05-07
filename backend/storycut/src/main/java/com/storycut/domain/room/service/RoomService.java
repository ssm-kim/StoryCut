package com.storycut.domain.room.service;

import com.storycut.domain.room.dto.request.RoomCreateRequest;
import com.storycut.domain.room.dto.request.RoomUpdateRequest;
import com.storycut.domain.room.dto.response.RoomMemberResponse;
import com.storycut.domain.room.dto.response.RoomResponse;

import java.util.List;

/**
 * 공유방 관련 서비스 인터페이스
 * <p>
 * 공유방의 생성, 조회, 수정, 삭제 및 멤버 관리 기능을 정의합니다.
 * 이 인터페이스는 MCP(Model Context Protocol)에 따라 설계되었습니다.
 * </p>
 */
public interface RoomService {
    
    /**
     * 공유방을 생성합니다.
     * 
     * @param memberId 공유방을 생성하는 회원의 ID (방장)
     * @param request 공유방 생성에 필요한 정보를 담은 요청 객체
     * @return 생성된 공유방 정보를 담은 응답 객체
     * @throws com.storycut.global.exception.BusinessException 유효하지 않은 요청 값이 있는 경우
     */
    RoomResponse createRoom(Long memberId, RoomCreateRequest request);
    
    /**
     * 회원이 참여 중인 모든 공유방 목록을 조회합니다.
     * 
     * @param memberId 조회하려는 회원의 ID
     * @return 회원이 참여 중인 공유방 목록
     */
    List<RoomResponse> getMyRooms(Long memberId);
    
    /**
     * 공유방 정보를 수정합니다.
     * 방장만 공유방 정보를 수정할 수 있습니다.
     * 
     * @param memberId 수정을 요청하는 회원의 ID (방장)
     * @param roomId 수정할 공유방의 ID
     * @param request 수정할 정보를 담은 요청 객체
     * @return 수정된 공유방 정보를 담은 응답 객체
     * @throws com.storycut.global.exception.BusinessException 해당 공유방이 존재하지 않거나 방장이 아닌 경우
     */
    RoomResponse updateRoom(Long memberId, Long roomId, RoomUpdateRequest request);
    
    /**
     * 공유방을 삭제합니다.
     * 방장만 공유방을 삭제할 수 있습니다.
     * 
     * @param memberId 삭제를 요청하는 회원의 ID (방장)
     * @param roomId 삭제할 공유방의 ID
     * @throws com.storycut.global.exception.BusinessException 해당 공유방이 존재하지 않거나 방장이 아닌 경우
     */
    void deleteRoom(Long memberId, Long roomId);
    
    /**
     * 공유방에 새로운 멤버를 초대합니다.
     * 방장만 멤버를 초대할 수 있습니다.
     * 
     * @param hostMemberId 초대를 요청하는 회원의 ID (방장)
     * @param roomId 멤버를 초대할 공유방의 ID
     * @param inviteMemberId 초대할 회원의 ID
     * @return 초대된 멤버의 정보를 담은 응답 객체
     * @throws com.storycut.global.exception.BusinessException 해당 공유방이 존재하지 않거나 방장이 아닌 경우
     * @throws com.storycut.global.exception.BusinessException 이미 참여 중인 멤버인 경우
     */
    RoomMemberResponse inviteMember(Long hostMemberId, Long roomId, Long inviteMemberId);
    
    /**
     * 공유방에 입장합니다.
     * 비밀번호가 설정된 공유방은 비밀번호 검증을 통과해야 입장할 수 있습니다.
     * 
     * @param memberId 입장을 요청하는 회원의 ID
     * @param roomId 입장할 공유방의 ID
     * @param password 공유방 입장 비밀번호 (null 가능)
     * @return 입장한 공유방 정보를 담은 응답 객체
     * @throws com.storycut.global.exception.BusinessException 해당 공유방이 존재하지 않는 경우
     * @throws com.storycut.global.exception.BusinessException 비밀번호가 일치하지 않는 경우
     */
    RoomResponse enterRoom(Long memberId, Long roomId, String password);
    
    /**
     * 공유방에서 나갑니다.
     * 방장이 나갈 경우 다음 규칙이 적용됩니다:
     * 1. 방에 남은 다른 멤버가 있을 경우, 가입 시간이 가장 오래된(생성 일자가 가장 빠른) 멤버가 새로운 방장으로 지정됩니다.
     * 2. 방에 남은 다른 멤버가 없을 경우, 방은 자동으로 삭제됩니다.
     * 일반 멤버가 나갈 경우에는 단순히 공유방 멤버에서 제외됩니다.
     * 
     * @param memberId 나가기를 요청하는 회원의 ID
     * @param roomId 나갈 공유방의 ID
     * @throws com.storycut.global.exception.BusinessException 해당 공유방이 존재하지 않는 경우
     * @throws java.util.NoSuchElementException 방장이 나가는데 남은 멤버가 있지만 가장 오래된 멤버를 찾을 수 없는 경우
     */
    void leaveRoom(Long memberId, Long roomId);
    
    /**
     * 공유방의 상세 정보를 조회합니다.
     * 
     * @param roomId 조회할 공유방의 ID
     * @return 공유방 상세 정보를 담은 응답 객체
     * @throws com.storycut.global.exception.BusinessException 해당 공유방이 존재하지 않는 경우
     */
    RoomResponse getRoomDetail(Long roomId);
    
    /**
     * 공유방 참여자 목록을 조회합니다.
     * 
     * @param roomId 조회할 공유방의 ID
     * @return 공유방 참여자 목록을 담은 응답 객체 리스트
     */
    List<RoomMemberResponse> getRoomMembers(Long roomId);
}
