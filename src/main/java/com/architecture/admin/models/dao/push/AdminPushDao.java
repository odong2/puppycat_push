package com.architecture.admin.models.dao.push;

import com.architecture.admin.models.dto.push.AdminPushDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface AdminPushDao {
    /**
     * 푸시 등록
     *
     * @param adminPushDto
     * @return insertedIdx
     */
    Long insertAdminPush(AdminPushDto adminPushDto);
    /**
     * sns_push_admin 기존내역 state 0 으로 변경
     *
     * @return
     */
    void updateState(AdminPushDto adminPushDto);

    /**
     * worker에서 마지막으로 돌아간 idx 값 가져오기
     *
     * @param adminPushDto idx
     */
    void updateWorkerLimit(AdminPushDto adminPushDto);

    /**
     * cron 상태값 0으로 변경
     *
     * @param num idx
     */
    void updateStateWorker(int num);

    /**
     * 워커 작동
     */
    void updateWorkerState();
}
