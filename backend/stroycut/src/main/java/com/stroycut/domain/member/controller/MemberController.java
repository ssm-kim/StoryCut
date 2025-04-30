package com.stroycut.domain.member.controller;

import com.stroycut.domain.auth.model.CustomUserDetails;
import com.stroycut.domain.member.model.dto.MemberDto;
import com.stroycut.domain.member.service.MemberService;
import com.stroycut.global.common.model.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 현재 로그인한 사용자의 상세 정보를 조회합니다.
     * @param userDetails 인증된 사용자 정보 (CustomUserDetails 객체)
     * @return 회원 정보 응답
     */
    @GetMapping("/detail")
    public ResponseEntity<BaseResponse<MemberDto.Response>> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        log.info("내 정보 요청 - 사용자 ID: {}, 이메일: {}", memberId, userDetails.getEmail());
        MemberDto.Response response = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(new BaseResponse<>(response));
    }

    /**
     * 현재 로그인한 사용자의 정보를 업데이트합니다.
     * @param userDetails 인증된 사용자 정보 (CustomUserDetails 객체)
     * @param updateRequest 업데이트할 정보
     * @return 업데이트된 회원 정보 응답
     */
    @PutMapping("/detail")
    public ResponseEntity<BaseResponse<MemberDto.Response>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MemberDto.UpdateRequest updateRequest) {
        Long memberId = userDetails.getMemberId();
        log.info("내 정보 업데이트 요청 - 사용자 ID: {}", memberId);
        MemberDto.Response response = memberService.updateMember(memberId, updateRequest);
        return ResponseEntity.ok(new BaseResponse<>(response));
    }
}