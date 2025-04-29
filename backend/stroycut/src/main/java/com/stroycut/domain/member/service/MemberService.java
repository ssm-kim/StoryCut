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

    @Transactional(readOnly = true)
    public MemberDto.Response getMemberInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다: " + email));
        
        return MemberDto.Response.fromEntity(member);
    }

    @Transactional
    public MemberDto.Response updateMember(String email, MemberDto.UpdateRequest updateRequest) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다: " + email));
        
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
        log.info("회원 정보 업데이트 완료 - 이메일: {}", email);
        
        return MemberDto.Response.fromEntity(updatedMember);
    }
}