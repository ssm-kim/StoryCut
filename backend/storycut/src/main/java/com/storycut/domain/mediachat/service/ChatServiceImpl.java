package com.storycut.domain.mediachat.service;

import com.storycut.domain.mediachat.model.ChatMessage;
import com.storycut.domain.mediachat.dto.response.ChatMessageResponse;
import com.storycut.domain.mediachat.repository.ChatMessageRepository;
import com.storycut.domain.mediachat.dto.request.ChatMessageRequest;
import com.storycut.domain.room.entity.Room;
import com.storycut.domain.room.service.RoomDetailService;
import com.storycut.global.exception.BusinessException;
import com.storycut.global.model.dto.BaseResponseStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 채팅 메시지 서비스 구현 클래스
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final RoomDetailService roomDetailService;

    @Override
    @Transactional
    public ChatMessageResponse saveVideoMessage(Long roomId, Long memberId, ChatMessageRequest request) {
        // Room 존재 확인
        roomDetailService.findRoomById(roomId);

        // 비디오 채팅 메시지 생성
        ChatMessage chatMessage = request.toEntity(roomId, memberId);

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return ChatMessageResponse.from(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getRoomMessages(Long roomId) {
        // Room 존재 확인
        roomDetailService.findRoomById(roomId);

        // 채팅 메시지 조회 (삭제되지 않은 메시지만)
        List<ChatMessage> messages = chatMessageRepository.findByRoomId(roomId);

        return messages.stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessages(Long roomId, int page, int size) {
        // Room 존재 확인
        roomDetailService.findRoomById(roomId);

        // 채팅 메시지 페이징 조회
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<ChatMessage> messagePage = chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId, pageable);

        return messagePage.getContent().stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessagesAfter(Long roomId, LocalDateTime timestamp) {
        // Room 존재 확인
        if (!roomRepository.existsById(roomId)) {
            throw new BusinessException(BaseResponseStatus.NOT_FOUND_ROOM);
        }

        // 특정 시간 이후의 채팅 메시지 조회
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdAndTimestampAfterOrderByTimestampAsc(roomId, timestamp);

        return messages.stream()
                .filter(message -> !message.isDeleted()) // 삭제되지 않은 메시지만 필터링
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteMessage(String chatId, Long memberId) {
        // 메시지 존재 확인
        Optional<ChatMessage> messageOpt = chatMessageRepository.findById(chatId);
        if (messageOpt.isEmpty()) {
            return false; // 메시지가 존재하지 않음
        }

        ChatMessage message = messageOpt.get();
        
        // 이미 삭제된 메시지인지 확인
        if (message.isDeleted()) {
            return false;
        }
        
        // 권한 확인 (메시지 작성자 또는 방장)
        boolean isMessageSender = message.getSenderId().equals(memberId);
        boolean isRoomOwner = isRoomOwner(message.getRoomId(), memberId);
        
        if (!isMessageSender && !isRoomOwner) {
            return false; // 권한 없음
        }
        
        // 소프트 딜리트 (메시지 삭제 표시)
        message.setDeleted(true);
        message.setDeletedBy(memberId);
        message.setDeletedAt(LocalDateTime.now());
        
        chatMessageRepository.save(message);
        return true;
    }
    
    @Override
    public boolean isRoomOwner(Long roomId, Long memberId) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            return false;
        }
        Room room = roomOpt.get();
        return room.getOwnerId().equals(memberId);
    }

    @Override
    @Transactional
    public void deleteRoomMessages(Long roomId) {
        // Room 존재 확인은 하지 않음 (Room이 이미 삭제되었을 수 있음)
        chatMessageRepository.deleteByRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMemberMessages(Long roomId, Long senderId, Pageable pageable) {
        // Room 존재 확인
        if (!roomRepository.existsById(roomId)) {
            throw new BusinessException(BaseResponseStatus.NOT_FOUND_ROOM);
        }

        // 특정 작성자의 채팅 메시지 조회
        Page<ChatMessage> messagePage = chatMessageRepository.findByRoomIdAndSenderIdOrderByTimestampDesc(roomId, senderId, pageable);

        return messagePage.map(ChatMessageResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> searchMessages(Long roomId, String keyword, Pageable pageable) {
        // Room 존재 확인
        if (!roomRepository.existsById(roomId)) {
            throw new BusinessException(BaseResponseStatus.NOT_FOUND_ROOM);
        }

        // 키워드 검색
        Page<ChatMessage> messagePage = chatMessageRepository.findByRoomIdAndContentContainingOrderByTimestampDesc(roomId, keyword, pageable);

        return messagePage.map(ChatMessageResponse::from);
    }

    @Override
    @Transactional
    public ChatMessageResponse createSystemMessage(Long roomId, String content) {
        // Room 존재 확인
        if (!roomRepository.existsById(roomId)) {
            throw new BusinessException(BaseResponseStatus.NOT_FOUND_ROOM);
        }

        // 시스템 메시지 생성
        ChatMessage systemMessage = ChatMessage.builder()
                .roomId(roomId)
                .senderId(0L)  // 시스템 메시지는 senderId를 0으로 설정
                .senderNickname("System")
                .content(content)
                .messageType(ChatMessage.MessageType.SYSTEM)
                .timestamp(LocalDateTime.now())
                .deleted(false)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(systemMessage);

        return ChatMessageResponse.from(savedMessage);
    }
}
