package com.storycut.domain.mediachat.dto.response;

import com.storycut.domain.mediachat.model.ChatMessage;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 채팅 메시지 응답 DTO
 */
@Getter
@Builder
public class ChatMessageResponse {

    private String id;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String content;
    private String messageType;
    private String mediaUrl;
    // 비디오 관련 필드 추가
    private String videoId;
    private String videoTitle;
    private String thumbnailUrl;
    private LocalDateTime timestamp;
    private boolean deleted;
    private Long deletedBy;
    private LocalDateTime deletedAt;

    /**
     * ChatMessage 엔티티를 응답 DTO로 변환합니다.
     *
     * @param chatMessage 채팅 메시지 엔티티
     * @return 채팅 메시지 응답 DTO
     */
    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .roomId(chatMessage.getRoomId())
                .senderId(chatMessage.getSenderId())
                .senderNickname(chatMessage.getSenderNickname())
                .content(chatMessage.getContent())
                .messageType(chatMessage.getMessageType().name())
                .mediaUrl(chatMessage.getMediaUrl())
                .videoId(chatMessage.getVideoId())
                .videoTitle(chatMessage.getVideoTitle())
                .thumbnailUrl(chatMessage.getThumbnailUrl())
                .timestamp(chatMessage.getTimestamp())
                .deleted(chatMessage.isDeleted())
                .deletedBy(chatMessage.getDeletedBy())
                .deletedAt(chatMessage.getDeletedAt())
                .build();
    }
}
