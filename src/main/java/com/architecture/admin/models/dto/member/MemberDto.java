package com.architecture.admin.models.dto.member;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberDto {
    /**
     * sns_member
     **/
    private Long idx;           // 회원번호
    @Email
    private String id;          // 아이디
    private String uuid;        // 고유아이디
    private String nick;        // 닉네임
    private String partner;     // 파트너명
    private String lang;        // 사용언어
    private Integer isSimple;   // 간편가입
    private String loginIp;     // 로그인 아이피
    private String joinIp;      // 가입 아이피
    private Integer state;      // 상태값 (1:정상/2:대기/3:제재)
    private String stateText;   // 상태값 문자변환
    private String stateBg;     // 상태 bg 색상
    private String lastLogin;   // 마지막 로그인(UTC)
    private String lastLoginTz; // 마지막 로그인 타임존
    private String regDate;     // 등록일
    private String regDateTz;   // 등록일 타임존
    private Integer isDel;      // 탈퇴상태 (0:정상/1:탈퇴)

    /**
     * sns_member_info
     **/
    private Long memberIdx;     // 회원번호
    private String name;        // 이름
    private String phone;       // 전화번호
    private String gender;      // 성별(M: male, F: female)
    private String genderText;  // 성별 문자변환
    private String birth;       // 생년월일

    /**
     * sns_member_password
     **/
    private String password;          // 패스워드
    private String passwordConfirm;   // 패스워드 확인
    private String modifyDate;  // 수정일
    private String modifyDateTz;// 수정일 타임존

    /**
     * sns_member_simple
     **/
    private String simpleId;  // 간편가입 넘어오는 아이디
    private String simpleType;  // 간편가입 타입 (ex kakao google)
    private String authToken;   // 토큰 값 (refresh)

    // sql
    private Long insertedIdx;
    private Integer affectedRow;

}