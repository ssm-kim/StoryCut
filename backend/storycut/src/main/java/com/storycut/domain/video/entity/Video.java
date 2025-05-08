package com.storycut.domain.video.entity;

import com.storycut.global.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "video")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_id")
    private Long id;
    
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    
    @Column(name = "video_name", nullable = false)
    private String videoName;
    
    @Column(name = "video_url", nullable = false)
    private String videoUrl;
    
    @Column(name = "thumbnail")
    private String thumbnail;
    
    @Column(name = "original_video_id")
    private Long originalVideoId;

    @Column(name = "is_blur", nullable = false)
    private boolean isBlur;

    @Builder
    public Video(Long memberId, String videoName, String videoUrl,
                String thumbnail, Long originalVideoId, Boolean isBlur) {
        this.memberId = memberId;
        this.videoName = videoName;
        this.videoUrl = videoUrl;
        this.thumbnail = thumbnail;
        this.originalVideoId = originalVideoId;
        this.isBlur = isBlur;
    }
}
