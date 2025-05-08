package com.storycut.domain.video.dto.request;

import com.storycut.domain.video.entity.Video;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoUploadRequest {
    
    @NotBlank(message = "파일 이름은 필수입니다.")
    @Size(max = 255, message = "파일 이름은 255자 이하여야 합니다.")
    private String videoName;
    
    @NotBlank(message = "비디오 URL은 필수입니다.")
    private String videoUrl;
    
    private String thumbnail;
    
    private Long originalVideoId;

    private Boolean isBlur;
    
    public Video toEntity(Long memberId) {
        return Video.builder()
                .memberId(memberId)
                .videoName(videoName)
                .videoUrl(videoUrl)
                .thumbnail(thumbnail)
                .originalVideoId(originalVideoId)
                .isBlur(isBlur)
                .build();
    }
}
