package com.storycut.domain.member.controller;

import com.storycut.domain.member.model.dto.MemberDto;
import com.storycut.domain.member.service.MemberService;
import com.storycut.global.model.dto.BaseResponse;
import com.storycut.domain.auth.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "멤버 API")
public class MemberController {

    private final MemberService memberService;

    // 회원 상세 정보 조회
    @GetMapping("/detail")
    public ResponseEntity<BaseResponse<MemberDto.Response>> getMyInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        log.info("내 정보 요청 - 사용자 ID: {}, 이메일: {}", memberId, userDetails.getEmail());
        MemberDto.Response response = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(new BaseResponse<>(response));
    }

    // 회원 정보 업데이트
    @PatchMapping("/detail")
    public ResponseEntity<BaseResponse<MemberDto.Response>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MemberDto.UpdateRequest updateRequest) {
        Long memberId = userDetails.getMemberId();
        log.info("내 정보 업데이트 요청 - 사용자 ID: {}, 닉네임: {}", memberId, updateRequest.getNickname());
        MemberDto.Response response = memberService.updateMember(memberId, updateRequest);
        return ResponseEntity.ok(new BaseResponse<>(response));
    }

    // 계정 탈퇴
    @DeleteMapping()
    public ResponseEntity<BaseResponse<Void>> deleteMyAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        log.info("계정 탈퇴 요청 - 사용자 ID: {}, 이메일: {}", memberId, userDetails.getEmail());
        memberService.deleteMember(memberId);
        return ResponseEntity.ok(new BaseResponse<>());
    }
}