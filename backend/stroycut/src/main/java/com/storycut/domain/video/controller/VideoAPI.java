package com.storycut.domain.video.controller;

import com.storycut.domain.video.dto.request.VideoUploadRequest;
import com.storycut.domain.video.dto.response.VideoResponse;
import com.storycut.global.model.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "비디오 API", description = "비디오 관련 API")
public interface VideoAPI {
    
    @PostMapping
    @Operation(summary = "비디오 업로드", description = "비디오 정보를 DB에 저장합니다. FastAPI에서 S3에 업로드한 후 호출해야 합니다.")
    BaseResponse<VideoResponse> uploadVideo(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @RequestBody VideoUploadRequest request);
    
    @GetMapping("/{videoId}")
    @Operation(summary = "비디오 상세 조회", description = "비디오 ID로 비디오 정보를 조회합니다.")
    BaseResponse<VideoResponse> getVideo(@PathVariable Long videoId);
    
    @GetMapping
    @Operation(summary = "모든 비디오 목록 조회", description = "모든 비디오 목록을 조회합니다.")
    BaseResponse<List<VideoResponse>> getAllVideos();
    
    @GetMapping("/member")
    @Operation(summary = "내 비디오 목록 조회", description = "회원이 업로드한 비디오 목록을 조회합니다.")
    BaseResponse<List<VideoResponse>> getMyVideos(@AuthenticationPrincipal CustomUserDetails authUser;
    
    @GetMapping("/edited/{originalVideoId}")
    @Operation(summary = "편집된 비디오 목록 조회", description = "원본 비디오를 기반으로 편집된 비디오 목록을 조회합니다.")
    BaseResponse<List<VideoResponse>> getEditedVideos(@PathVariable Long originalVideoId);
    
    @GetMapping("/download/{videoId}")
    @Operation(summary = "비디오 다운로드 정보 조회", description = "비디오 다운로드를 위한 정보를 조회합니다.")
    BaseResponse<VideoResponse> getVideoDownloadInfo(@PathVariable Long videoId);
}
