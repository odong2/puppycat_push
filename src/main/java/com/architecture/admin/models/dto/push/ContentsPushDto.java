package com.architecture.admin.models.dto.push;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentsPushDto {

    // sns_contents_mention_mapping
    private Long idx;           // 고유 번호
    private Long contentsIdx;   // 컨텐츠 번호
    private Long mentionIdx;    // 멘션 번호
    private Integer state;      // 상태값
    private String regDate;     // 등록일
    private String regDateTz;   // 등록일 타임존
    private String modiDate;    // 수정일
    private String modiDateTz;   // 수정일 타임존

    // sns_member_mention
    private String memberUuid;    // 회원uuid

    // sns_img_member_tag_mapping
    private Long imgIdx;        // 컨텐츠 번호

    // sns_member_follow
    private String followUuid;     // 팔로워uuid


    private String nick;        // 회원 닉네임
    private String url;         // 이미지 url

    // sql
    private Long insertedIdx;
    private Long affectedRow;

}
