package com.architecture.admin.models.daosub.push;

import com.architecture.admin.models.dto.push.ContentsPushDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface ContentsPushDaoSub {
    /**
     * 컨텐츠에 이미지 태그 된 회원 리스트 가져오기
     *
     * @param contentsPushDto 컨텐츠IDX
     * @return 이미지 태그 된 회원 리스트
     */
    List<String> getImgTagMember(ContentsPushDto contentsPushDto);

    /**
     * 수정전 컨텐츠에 이미지 태그 된 회원 리스트
     *
     * @param contentsPushDto 컨텐츠IDX
     * @return
     */
    List<String> getPrevImgTagMember(ContentsPushDto contentsPushDto);


    /**
     * 컨텐츠에 멘션 된 회원 리스트 가져오기
     *
     * @param contentsPushDto 컨텐츠IDX
     * @return 멘션 된 회원 리스트
     */
    List<String> getMentionMember(ContentsPushDto contentsPushDto);
    
    /**
     * 컨텐츠 작성자 회원 정보
     *
     * @param contentsIdx 컨텐츠IDX
     * @return 컨텐츠 작성자 idx nick 작성regdate
     */
    ContentsPushDto getContentsWriterInfo(Long contentsIdx);

    /**
     * 컨텐츠 첫번째 이미지 가져오기
     * 
     * @param contentsIdx 컨텐츠IDX
     * @return 이미지url
     */
    String getContentsImg(Long contentsIdx);

    /**
     * 컨텐츠 등록자의 팔로워 리스트 가져오기
     * 
     * @param contentsPushDto memberIdx regdate
     * @return 팔로워 idx
     */
    List<String> getFollowerMember(ContentsPushDto contentsPushDto);
}
