package com.architecture.admin.models.dto.push;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminPushDto {

    // sns_push_admin
    private Long idx;               // 고유 번호
    private Integer typeIdx;        // 푸시타입
    private String title;           // 제목
    private String body;            // 내용
    private Integer contentsType;   // 푸시타입
    private Long contentsIdx;    // 이동될 IDX
    private String img;             // 이미지
    private String adminId;         // 관리자id
    private Integer state;          // 상태값
    private String regDate;     // 등록일
    private String regDateTz;   // 등록일 타임존

    // sns_push_type
    private String typeTitle;    // 타입 제목
    private Integer notiType;    // 공지 타입
    private Integer subType;     // 공지 상세 타입


    private List<MultipartFile> uploadFile;    // 프로필 업로드 이미지

    // sns_member_notification_sub_setting
    private Long memberIdx;     // 회원번호

    // sns_member_app
    private String fcmToken;     // fcm 토큰

    private int num;                // 나머지값
    private Long followerIdx;       // 팔로워 idx
    private Long limit;             // 조회 limit

    private String senderUuid;     // 푸시 보내는 회원 ( 1:N 경우 )
    private String receiverUuid;   // 푸시 받는 회원 ( 1:1 경우 )
    private Integer grade;      // 받는 회원 ( 0:1~1000 1:1001~2000 2:2001~3000)

    // sql
    private Long insertedIdx;
    private Long affectedRow;

}