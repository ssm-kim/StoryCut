package com.storycut.domain.video.dto.response;

import com.storycut.domain.video.entity.Video;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoResponse {
    private Long videoId;
    private Long memberId;
    private String videoFileName;
    private String videoUrl;
    private String thumbnail;
    private String status;
    private Long originalVideoId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static VideoResponse from(Video video) {
        return VideoResponse.builder()
                .videoId(video.getId())
                .memberId(video.getMemberId())
                .videoFileName(video.getVideoFileName())
                .videoUrl(video.getVideoUrl())
                .thumbnail(video.getThumbnail())
                .status(video.getStatus())
                .originalVideoId(video.getOriginalVideoId())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .build();
    }
}
