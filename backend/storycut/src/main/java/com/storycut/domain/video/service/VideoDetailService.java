package com.storycut.domain.video.service;

import com.storycut.domain.video.dto.response.VideoResponse;
import com.storycut.domain.video.entity.Video;
import com.storycut.domain.video.repository.VideoRepository;
import com.storycut.global.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.storycut.global.model.dto.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoDetailService {
    
    private final VideoRepository videoRepository;
    
    /**
     * ID로 비디오를 조회합니다.
     * 
     * @param videoId 비디오 ID
     * @return 조회된 비디오 엔티티
     * @throws BusinessException 비디오가 존재하지 않는 경우
     */
    public Video findVideoById(Long videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_VIDEO));
    }
    
    /**
     * 회원 ID로 비디오 목록을 조회합니다.
     * 
     * @param memberId 회원 ID
     * @return 회원이 업로드한 비디오 목록
     */
    public List<Video> findVideosByMemberId(Long memberId) {
        return videoRepository.findByMemberId(memberId);
    }
    
    /**
     * 원본 비디오 ID로 편집된 비디오 목록을 조회합니다.
     * 
     * @param originalVideoId 원본 비디오 ID
     * @return 원본 비디오를 바탕으로 편집된 비디오 목록
     */
    public List<Video> findVideosByOriginalVideoId(Long originalVideoId) {
        return videoRepository.findByOriginalVideoId(originalVideoId);
    }

    
    /**
     * 비디오 엔티티를 저장합니다.
     * 
     * @param video 저장할 비디오 엔티티
     * @return 저장된 비디오 엔티티
     */
    @Transactional
    public Video saveVideo(Video video) {
        return videoRepository.save(video);
    }

    
    /**
     * 비디오 엔티티를 응답 DTO로 변환합니다.
     * 
     * @param video 변환할 비디오 엔티티
     * @return 비디오 응답 DTO
     */
    public VideoResponse mapToResponse(Video video) {
        return VideoResponse.from(video);
    }
}
