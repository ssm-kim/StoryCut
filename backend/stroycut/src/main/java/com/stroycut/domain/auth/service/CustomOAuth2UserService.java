package com.stroycut.domain.auth.service;

import com.stroycut.domain.auth.model.GoogleOAuth2UserInfo;
import com.stroycut.domain.auth.model.OAuth2UserInfo;
import com.stroycut.domain.member.model.entity.Member;
import com.stroycut.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 현재 로그인 진행 중인 서비스를 구분하는 코드 (구글만 지원하므로 여기서는 항상 "google")
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        // OAuth2 로그인 진행 시 키가 되는 필드값 (구글의 경우 "sub")
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // OAuth2UserService를 통해 가져온 OAuth2User의 attributes
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 서비스 타입에 따른 OAuth2UserInfo 객체 생성
        OAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // 사용자 정보 업데이트 또는 생성
        Member member = saveOrUpdateMember(userInfo);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName
        );
    }

    // 사용자 정보를 저장하거나 업데이트하는 메소드
    @Transactional
    public Member saveOrUpdateMember(OAuth2UserInfo userInfo) {
        Optional<Member> memberOptional = memberRepository.findByProviderId(userInfo.getId());
        
        if (memberOptional.isPresent()) {
            // 기존 회원이 있으면 정보 업데이트
            Member existingMember = memberOptional.get();
            if (!existingMember.getEmail().equals(userInfo.getEmail())) {
                // 이메일이 변경되었을 경우 처리 (필요하다면)
                log.info("User email changed from {} to {}", existingMember.getEmail(), userInfo.getEmail());
            }
            return existingMember;
        } else {
            // 새 회원이면 생성
            Member newMember = Member.builder()
                    .email(userInfo.getEmail())
                    .name(userInfo.getName())
                    .nickname(userInfo.getName()) // 초기 닉네임은 이름과 동일하게 설정
                    .profileImg(userInfo.getImageUrl())
                    .phoneNumber("") // 초기값은 빈 문자열, 나중에 사용자가 업데이트 가능
                    .providerId(userInfo.getId())
                    .build();
            
            return memberRepository.save(newMember);
        }
    }
}
