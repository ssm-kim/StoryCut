package com.stroycut.domain.member.service;

import com.stroycut.domain.member.model.dto.MemberDto;
import com.stroycut.domain.member.model.entity.Member;
import com.stroycut.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 사용자 ID로 회원 정보를 조회합니다.
     * @param memberId 회원 ID (Long 타입)
     * @return 회원 정보 응답 DTO
     */
    @Transactional(readOnly = true)
    public MemberDto.Response getMemberInfo(Long memberId) {
        // memberId로 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다. ID: " + memberId));
        
        return MemberDto.Response.fromEntity(member);
    }

    /**
     * 사용자 ID로 회원 정보를 업데이트합니다.
     * @param memberId 회원 ID (Long 타입)
     * @param updateRequest 업데이트 요청 DTO
     * @return 업데이트된 회원 정보 응답 DTO
     */
    @Transactional
    public MemberDto.Response updateMember(Long memberId, MemberDto.UpdateRequest updateRequest) {
        // memberId로 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다. ID: " + memberId));
        
        // 변경 사항 적용
        if (updateRequest.getNickname() != null) {
            member.updateNickname(updateRequest.getNickname());
        }
        
        if (updateRequest.getPhoneNumber() != null) {
            member.updatePhoneNumber(updateRequest.getPhoneNumber());
        }
        
        if (updateRequest.getProfileImg() != null) {
            member.updateProfileImg(updateRequest.getProfileImg());
        }
        
        // 변경된 정보 저장
        Member updatedMember = memberRepository.save(member);
        log.info("회원 정보 업데이트 완료 - ID: {}", memberId);
        
        return MemberDto.Response.fromEntity(updatedMember);
    }
    
    /**
     * String 타입 memberId를 사용하는 이전 버전 메소드 (호환성 유지)
     * @deprecated CustomUserDetails 사용으로 직접 Long 타입 memberId를 전달받는 메소드 사용 권장
     */
    @Deprecated
    @Transactional(readOnly = true)
    public MemberDto.Response getMemberInfo(String memberIdStr) {
        return getMemberInfo(Long.parseLong(memberIdStr));
    }
    
    /**
     * String 타입 memberId를 사용하는 이전 버전 메소드 (호환성 유지)
     * @deprecated CustomUserDetails 사용으로 직접 Long 타입 memberId를 전달받는 메소드 사용 권장
     */
    @Deprecated
    @Transactional
    public MemberDto.Response updateMember(String memberIdStr, MemberDto.UpdateRequest updateRequest) {
        return updateMember(Long.parseLong(memberIdStr), updateRequest);
    }
}