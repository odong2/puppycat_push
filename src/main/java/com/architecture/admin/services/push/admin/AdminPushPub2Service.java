package com.architecture.admin.services.push.admin;

import com.architecture.admin.models.dto.push.AdminPushDto;
import com.architecture.admin.services.BaseService;
import com.architecture.admin.services.SNSService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(name = "use.sqs.listener.enabled", havingValue = "true")
public class AdminPushPub2Service extends BaseService {

    private final AdminPushService adminPushService;
    private final SNSService snsService;
    //idx 를 3으로 나눴을 때 나머지 값
    Integer remain = 1;
    // worker 순서
    Integer worker = 2;
    @Value("${cloud.aws.sns.push.all.topic.arn2}")
    private String snsTopicARN;

    @Scheduled(cron = "0 * * * * *")
    public void pushWorker2() {

        // cron 활성화 상태 체크
        if (adminPushService.checkWorkerState(worker)) {

            // 푸시 어드민 테이블 조회
            // type_idx (9-notice-전체회원),(10-event-알람 설정한 회원)
            AdminPushDto pushInfo = adminPushService.getAdminPushInfo();

            // 푸시 보낼 내역이 있으면
            if (pushInfo != null) {

                // 1번 워커 회원 조회 limit 조회
                Long limit = adminPushService.getWorkerLimit(worker);
                // 야간 시간 체크
                boolean checkNightTime = super.checkNightTime();

                AdminPushDto adminPushDto = AdminPushDto.builder()
                        .limit(limit)
                        .num(remain)
                        .build();

                // 리스트 초기화
                List<AdminPushDto> tokenList = null;

                // 푸시 타입 (9-notice-전체회원),(10-event-알람 설정한 회원)
                // 9번이면 전체 회원 토큰 조회
                if (pushInfo.getTypeIdx() == 9) {
                    // 전체 회원 토큰 조회
                    tokenList = adminPushService.getAllMemberToken(adminPushDto);
                }
                // 10번이면 알림 설정한 회원 토큰 조회
                else if (pushInfo.getTypeIdx() == 10) {
                    if (checkNightTime) {
                        // 알람 설정도 하고 야간 알람 설정도 한 회원의 토큰 조회 ( 3-1, 2-1 )
                        tokenList = adminPushService.getAlarmMemberNightToken(adminPushDto);

                    } else {
                        // 알림 설정한 회원 토큰 조회
                        tokenList = adminPushService.getAlarmMemberToken(adminPushDto);
                    }
                }
                // 잘못된 타입 설정
                else {
                    pushAlarm("어드민 푸시 :: typeIdx 확인 필요", "NHJ");
                }

                Long idx = 0L;
                if (tokenList != null && !tokenList.isEmpty()) {
                    for (AdminPushDto tokenInfo : tokenList) {
                        idx = tokenInfo.getIdx();

                        JSONObject data = new JSONObject();
                        data.put("push_idx", String.valueOf(pushInfo.getIdx()));
                        data.put("title", pushInfo.getTitle());
                        data.put("body", pushInfo.getBody());
                        data.put("type", pushInfo.getTypeTitle());
                        data.put("contents_type", String.valueOf(pushInfo.getContentsType()));
                        data.put("contents_idx", String.valueOf(pushInfo.getContentsIdx()));
                        data.put("image", pushInfo.getImg());
                        data.put("token", tokenInfo.getFcmToken());

                        snsService.publish(data.toString(), snsTopicARN, "PushAdmin");
                    }
                    // 마지막에서 토큰 limit 업데이트
                    adminPushDto.setLimit(idx);
                    adminPushDto.setNum(worker);
                    adminPushService.updateWorkerLimit(adminPushDto);
                } else {
                    pushAlarm("worker2:전송 완료", "NHJ");

                    // 크론 종료
                    adminPushService.updateStateWorker(worker);

                    // 0으로 업데이트
                    adminPushDto.setLimit(0L);
                    adminPushDto.setNum(worker);
                    adminPushService.updateWorkerLimit(adminPushDto);
                }
            } else {
                pushAlarm("푸시가 없는데 크론이 ON되어있습니다" + "\n" + "worker2 확인 필요", "NHJ");
            }
        }
    }
}
