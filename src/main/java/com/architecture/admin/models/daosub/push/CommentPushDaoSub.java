package com.architecture.admin.models.daosub.push;

import com.architecture.admin.models.dto.push.CommentPushDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface CommentPushDaoSub {

    /**
     * 댓글에 멘션 된 회원 리스트 가져오기
     *
     * @param commentIdx 댓글IDX
     * @return 멘션 된 회원 리스트
     */
    List<String> getMentionMember(Long commentIdx);
    
    /**
     * 댓글 작성자 정보 가져오기
     *
     * @param commentIdx 댓글 IDX
     * @return 컨텐츠idx 댓글 작성자idx,nick 부모댓글idx
     */
    CommentPushDto getCommentWriterInfo(Long commentIdx);

    /**
     * 컨텐츠 작성자 가져오기
     *
     * @param commentIdx 댓글IDX
     * @return 컨텐츠 작성자 uuid
     */
    String getContentsMember(Long commentIdx);


    /**
     * 부모댓글 작성자 가져오기
     *
     * @param commentIdx 부모댓글IDX
     * @return 부모댓글 작성자 uuid
     */
    String getParentCommentMember(Long commentIdx);

    /**
     * 댓글 수정전 멘션된 회원 리스트
     * @param commentWriterInfo
     * @return
     */
    List<String> getPrevMentionMember(CommentPushDto commentWriterInfo);
}
