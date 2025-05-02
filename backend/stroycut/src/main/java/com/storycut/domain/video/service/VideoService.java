package com.storycut.domain.video.service;

import com.storycut.domain.video.dto.request.VideoUploadRequest;
import com.storycut.domain.video.dto.response.VideoResponse;

import java.util.List;

/**
 * 비디오 관련 서비스 인터페이스
 * <p>
 * 비디오의 업로드, 조회, 상태 업데이트 등의 기능을 정의합니다.
 * </p>
 */
public interface VideoService {
    
    /**
     * 비디오를 업로드합니다.
     * FastAPI에서 S3에 업로드한 후, Spring Boot에서 DB에 정보를 저장합니다.
     * 
     * @param memberId 비디오를 업로드하는 회원의 ID
     * @param request 비디오 업로드에 필요한 정보를 담은 요청 객체
     * @return 업로드된 비디오 정보를 담은 응답 객체
     */
    VideoResponse uploadVideo(Long memberId, VideoUploadRequest request);
    
    /**
     * 비디오 정보를 조회합니다.
     * 
     * @param videoId 조회할 비디오의 ID
     * @return 비디오 정보를 담은 응답 객체
     * @throws com.storycut.global.exception.BusinessException 해당 비디오가 존재하지 않는 경우
     */
    VideoResponse getVideo(Long videoId);
    
    /**
     * 모든 비디오 목록을 조회합니다.
     * 
     * @return 비디오 목록
     */
    List<VideoResponse> getAllVideos();

    /**
     * 특정 회원이 업로드한 비디오 목록을 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 회원이 업로드한 비디오 목록
     */
    List<VideoResponse> getMemberVideos(Long memberId);
    
    /**
     * 원본 비디오를 기반으로 한 편집 비디오 목록을 조회합니다.
     * 
     * @param originalVideoId 원본 비디오의 ID
     * @return 편집된 비디오 목록
     */
    List<VideoResponse> getEditedVideos(Long originalVideoId);
}
