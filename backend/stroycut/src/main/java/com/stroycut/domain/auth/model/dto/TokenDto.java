package com.stroycut.domain.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto {
    private String accessToken;
    private String refreshToken;
    private Long memberId; // 클라이언트에 memberId 반환하여 사용자 정보 조회에 활용할 수 있도록 함
}
