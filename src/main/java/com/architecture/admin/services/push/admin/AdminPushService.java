package com.architecture.admin.services.push.admin;

import com.architecture.admin.models.dao.push.AdminPushDao;
import com.architecture.admin.models.daosub.push.AdminPushDaoSub;
import com.architecture.admin.models.dto.push.AdminPushDto;
import com.architecture.admin.services.BaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/*****************************************************
 * 푸시 공통 모델러
 ****************************************************/
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "use.sqs.listener.enabled", havingValue = "true")
public class AdminPushService extends BaseService {
    private final AdminPushDao adminPushDao;
    private final AdminPushDaoSub adminPushDaoSub;
    /*****************************************************
     *  Modules
     ****************************************************/
    /**
     * 푸시 worker 상태값 체크하기
     *
     * @param idx worker.idx
     * @return 실행중이면 true
     */
    public boolean checkWorkerState(Integer idx) {
        boolean result = false;
        Integer iState = getState(idx);
        if (iState == 1) {
            result = true;
        }
        return result;
    }

    /*****************************************************
     *  SubFunction - select
     ****************************************************/
    /**
     * 상태값 조회하기
     *
     * @param idx worer.idx
     * @return state [0: 미사용 , 1: 사용]
     */
    public Integer getState(Integer idx) {
        return adminPushDaoSub.getState(idx);
    }

    /**
     * 관리자 등록 푸시 정보 가져오기
     *
     * @return 관리자 등록 푸시 정보
     */
    public AdminPushDto getAdminPushInfo() {
        return adminPushDaoSub.getAdminPushInfo();
    }

    /**
     * idx limit값 가져오기
     *
     * @param num 워커 숫자
     * @return limit값
     */
    public Long getWorkerLimit(int num) {
        return adminPushDaoSub.getWorkerLimit(num);
    }

    /**
     * 전체 회원의 토큰 정보 가져오기
     *
     * @param adminPushDto num limit
     * @return 토큰 정보
     */
    public List<AdminPushDto> getAllMemberToken(AdminPushDto adminPushDto) {
        return adminPushDaoSub.getAllMemberToken(adminPushDto);
    }

    /**
     * 알림 설정한 회원의  토큰
     *
     * @param adminPushDto num limit
     * @return 토큰 정보
     */
    public List<AdminPushDto> getAlarmMemberToken(AdminPushDto adminPushDto) {
        return adminPushDaoSub.getAlarmMemberToken(adminPushDto);
    }

    /**
     * 알림/야간알림 설정 한 회원의  토큰
     *
     * @param adminPushDto num limit
     * @return 토큰 정보
     */
    public List<AdminPushDto> getAlarmMemberNightToken(AdminPushDto adminPushDto) {
        return adminPushDaoSub.getAlarmMemberNightToken(adminPushDto);
    }

    /*****************************************************
     *  SubFunction - insert
     ****************************************************/

    /*****************************************************
     *  SubFunction - Update
     ****************************************************/

    /**
     * limit값 업데이트
     *
     * @param adminPushDto idx
     */
    public void updateWorkerLimit(AdminPushDto adminPushDto) {
        // 등록일
        adminPushDto.setRegDate(dateLibrary.getDatetime());
        adminPushDao.updateWorkerLimit(adminPushDto);
    }

    /**
     * 크론 상태값 업데이트
     *
     * @param num idx
     */
    public void updateStateWorker(int num) {
        // 등록일
        adminPushDao.updateStateWorker(num);
    }
    /*****************************************************
     *  SubFunction - Delete
     ****************************************************/
}
