package com.storycut.domain.video.controller;

import com.storycut.domain.video.dto.request.VideoUploadRequest;
import com.storycut.domain.video.dto.response.VideoResponse;
import com.storycut.domain.video.service.VideoService;
import com.storycut.global.model.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController implements VideoAPI {
    
    private final VideoService videoService;
    
    @Override
    public BaseResponse<VideoResponse> uploadVideo(CustomUserDetails authUser, VideoUploadRequest request) {
        VideoResponse response = videoService.uploadVideo(authUser.getMemberId(), request);
        return new BaseResponse<>(response);
    }
    
    @Override
    public BaseResponse<VideoResponse> getVideo(Long videoId) {
        VideoResponse response = videoService.getVideo(videoId);
        return new BaseResponse<>(response);
    }
    
    @Override
    public BaseResponse<List<VideoResponse>> getAllVideos() {
        List<VideoResponse> responses = videoService.getAllVideos();
        return new BaseResponse<>(responses);
    }
    
    @Override
    public BaseResponse<List<VideoResponse>> getMyVideos(CustomUserDetails authUser) {
        List<VideoResponse> responses = videoService.getMemberVideos(authUser.getMemberId());
        return new BaseResponse<>(responses);
    }

    @Override
    public BaseResponse<List<VideoResponse>> getEditedVideos(Long originalVideoId) {
        List<VideoResponse> responses = videoService.getEditedVideos(originalVideoId);
        return new BaseResponse<>(responses);
    }

    @Override
    public BaseResponse<VideoResponse> getVideoDownloadInfo(Long videoId) {
        // 단순히 비디오 정보를 반환합니다. 클라이언트가 URL을 사용하여 다운로드합니다.
        VideoResponse response = videoService.getVideo(videoId);
        return new BaseResponse<>(response);
    }
}
