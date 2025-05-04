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
    public MemberDto.Response getMemberInfo(Long memberId) {
        // memberId로 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다. ID: " + memberId));
        
        return MemberDto.Response.fromEntity(member);
    }

    @Transactional
    public MemberDto.Response updateMember(Long memberId, MemberDto.UpdateRequest updateRequest) {
        // memberId로 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다. ID: " + memberId));
        
        // 변경 사항 적용
        if (updateRequest.getNickname() != null) {
            member.updateNickname(updateRequest.getNickname());
        }

        // 프로필 이미지 변경은 추후
//        if (updateRequest.getProfileImg() != null) {
//            member.updateProfileImg(updateRequest.getProfileImg());
//        }
        
        // 변경된 정보 저장
        Member updatedMember = memberRepository.save(member);
        log.info("회원 정보 업데이트 완료 - ID: {}", memberId);
        
        return MemberDto.Response.fromEntity(updatedMember);
    }
}