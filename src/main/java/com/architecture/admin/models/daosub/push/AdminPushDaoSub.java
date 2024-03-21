package com.architecture.admin.models.daosub.push;

import com.architecture.admin.models.dto.push.AdminPushDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface AdminPushDaoSub {
    /**
     * worker 상태값 가져오기
     *
     * @param idx worker.idx
     * @return state [0: 미사용 , 1: 사용]
     */
    Integer getState(Integer idx);

    /**
     * 공지 상태값 가져오기
     *
     * @param noticeIdx 공지 idx
     * @return state
     */
    Integer getNoticeStateByIdx(Long noticeIdx);

    /**
     * 콘텐츠 상태값 가져오기
     *
     * @param contentsIdx 콘텐츠 idx
     * @return state
     */
    Integer getContentsStateByIdx(Long contentsIdx);

    /**
     * 관리자 등록 푸시 정보 가져오기
     *
     * @return 관리자 등록 푸시 정보
     */
    AdminPushDto getAdminPushInfo();


    /**
     * state가 1인 워커 카운트
     *
     * @return
     */
    Integer getWorkerCheck();


    /**
     * idx limit값 가져오기
     *
     * @param num 워커 숫자
     * @return limit값
     */
    Long getWorkerLimit(int num);

    /**
     * 전체 회원의 토큰 정보 가져오기
     *
     * @param adminPushDto num limit
     * @return 토큰 정보
     */
    List<AdminPushDto> getAllMemberToken(AdminPushDto adminPushDto);

    /**
     * 전체 회원 중 야간 알림을 설정한 회원의 토큰
     *
     * @param adminPushDto num limit
     * @return 토큰 정보
     */
    List<AdminPushDto> getAllMemberNightToken(AdminPushDto adminPushDto);

    /**
     * 알림 설정한 회원의  토큰
     *
     * @param adminPushDto num limit
     * @return 토큰 정보
     */
    List<AdminPushDto> getAlarmMemberToken(AdminPushDto adminPushDto);

    /**
     * 알림/야간알림 설정 한 회원의  토큰
     *
     * @param adminPushDto num limit
     * @return 토큰 정보
     */
    List<AdminPushDto> getAlarmMemberNightToken(AdminPushDto adminPushDto);
}
