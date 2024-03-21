package com.architecture.admin.services.push;

import com.architecture.admin.models.dao.push.PushDao;
import com.architecture.admin.models.daosub.push.PushDaoSub;
import com.architecture.admin.models.dto.push.PushDto;
import com.architecture.admin.services.BaseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/*****************************************************
 * 푸시 공통 모델러
 ****************************************************/
@Service
@RequiredArgsConstructor
public class PushService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(PushService.class);
    private final PushDao pushDao;
    private final PushDaoSub pushDaoSub;


    /*****************************************************
     *  SubFunction - select
     ****************************************************/

    /**
     * 타입 제목 가져오기
     *
     * @param idx 타입idx
     * @return 타입title
     */
    public String getPushTypeTitle(int idx) {
        return pushDaoSub.getPushTypeTitle(idx);
    }

    /**
     * 토큰 리스트 가져오기
     *
     * @param pushDto receiverIdx typeIdx
     * @return 토큰 리스트
     */
    public List<String> getPushTokenList(PushDto pushDto) {
        return pushDaoSub.getPushTokenList(pushDto);
    }

    /**
     * 야간 설정도 한 토큰 리스트 가져오기
     *
     * @param pushDto receiverIdx typeIdx
     * @return 토큰 리스트
     */
    public List<String> getNightPushTokenList(PushDto pushDto) {
        return pushDaoSub.getNightPushTokenList(pushDto);
    }

    /*****************************************************
     *  SubFunction - insert
     ****************************************************/
    /**
     * 실패 로그 입력
     *
     * @param pushDto `sender_idx` `receiver_idx` `type_idx` `contents_idx` `comment_idx` `body` `img` `fcm_token`
     */
    public void insertFailLog(PushDto pushDto) {
        logger.error("fail_token :: " + pushDto.getFcmToken());
    }
}
