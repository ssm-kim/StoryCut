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

    @GetMapping("/detail")
    public ResponseEntity<BaseResponse<MemberDto.Response>> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        log.info("내 정보 요청 - 사용자 ID: {}, 이메일: {}", memberId, userDetails.getEmail());
        MemberDto.Response response = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(new BaseResponse<>(response));
    }

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