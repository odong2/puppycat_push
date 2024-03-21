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
public class CommentPushDto {
    //sns_contents_comment
    private Long contentsIdx; // 컨텐츠idx
    private Long parentIdx;   // 부모댓글idx

    // sns_contents_comment_mention_mapping
    private Long idx;           // 고유 번호
    private Long commentIdx;   // 댓글 번호
    private Long mentionIdx;    // 멘션 번호
    private Integer state;      // 상태값
    private String modiDate;    // 수정일
    private String regDate;     // 등록일
    private String regDateTz;   // 등록일 타임존

    // sns_member_mention
    private String memberUuid;    // 회원uuid

    private String nick;        // 회원 닉네임

    // sql
    private Long insertedIdx;
    private Long affectedRow;

}
