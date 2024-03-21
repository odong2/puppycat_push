package com.architecture.admin.services.push.contents;

import com.architecture.admin.models.dao.push.ContentsPushDao;
import com.architecture.admin.models.daosub.push.ContentsPushDaoSub;
import com.architecture.admin.models.dto.push.ContentsPushDto;
import com.architecture.admin.models.dto.push.PushDto;
import com.architecture.admin.services.BaseService;
import com.architecture.admin.services.SNSService;
import com.architecture.admin.services.push.PushService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "use.sqs.listener.enabled", havingValue = "true")
public class ContentsPushPubService extends BaseService {

    private final ContentsPushDao contentsPushDao;
    private final ContentsPushDaoSub contentspushDaoSub;
    private final PushService pushService;
    private final SNSService snsService;
    @Value("${cloud.aws.sns.push.contents.topic.arn}")
    private String snsTopicARN;
    @Value("${cloud.aws.sns.push.contents.topic.arn2}")
    private String snsTopicARN2;
    @Value("${cloud.aws.sns.push.contents.topic.arn3}")
    private String snsTopicARN3;
    @Value("${cloud.aws.cf.url}")
    private String imgDomain;

    /*****************************************************
     *  Modules
     ****************************************************/
    @SqsListener(value = {"${cloud.aws.sqs.push.all.contents.pushPub.listener}", "${cloud.aws.sqs.push.all.contents.pushPub.listener2}", "${cloud.aws.sqs.push.all.contents.pushPub.listener3}"}, deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void contentsPush(@Payload String message) {
        // 푸시 타입
        String type;
        // sns messageDeduplicationId
        String dpId;

        // 큐 data 꺼내오기
        JSONObject obj = new JSONObject(message);
        String action = obj.getString("action");
        long contentsIdx = obj.getLong("contents_idx");

        // 컨텐츠 IDX 를 3으로 나눠서 QUE에 분산 시킴
        long remain = contentsIdx % 3;
        String arn;

        if (remain == 0) {
            arn = snsTopicARN;
        } else if (remain == 1) {
            arn = snsTopicARN2;
        } else {
            arn = snsTopicARN3;
        }

        // 컨텐츠 작성 정보
        ContentsPushDto contentsWriterInfo = getContentsWriterInfo(contentsIdx);

        String senderUuid = contentsWriterInfo.getMemberUuid();
        String senderNick = contentsWriterInfo.getNick();
        String writeDate = contentsWriterInfo.getRegDate();

        // 컨텐츠 이미지 첫번째꺼 가져오기
        String imgUrl = getContentsImg(contentsIdx);
        // 이미지 full url
        String img = imgDomain + imgUrl;

        // 컨텐츠 등록 START
        if (Objects.equals(action, "regist")) {
            // 이미지 태그 된 회원 목록 조회 
            List<String> imgTagMemberList = getImgTagMember(contentsWriterInfo);

            // 이미지 태그 된 회원이 존재한다면
            if (imgTagMemberList != null && !imgTagMemberList.isEmpty()) {
                // 타입 타이틀 - 이미지 내 태그 푸시 타입 : 11 [ sns_push_type ]
                type = pushService.getPushTypeTitle(11);

                // 한명씩 돌면서 토큰 조회
                for (String tagMemberUuid : imgTagMemberList) {
                    // 토큰 리스트
                    List<String> pushTokenlist = null;
                    // 차단 내역 가져오기 ( 한명이라도 차단 했으면 true )
                    boolean chekBlock = super.bChkBlock(tagMemberUuid, senderUuid);
                    // 본인이 본인을 멘션했는지 체크
                    boolean checkMe = Objects.equals(tagMemberUuid, senderUuid);
                    // 본인이 아니고, 차단내역도 없으면
                    if (!chekBlock && !checkMe) {
                        // 토큰 조회 위해 푸시 DTO 세팅 - 이미지 내 태그 푸시 타입 : 11
                        PushDto pushDto = PushDto.builder()
                                .receiverUuid(tagMemberUuid)
                                .typeIdx(11)
                                .build();

                        // 푸시 받을 회원 토큰 리스트(fcm 토큰이 정상이고,알람 설정한 회원)
                        pushTokenlist = pushService.getPushTokenList(pushDto);

                    }
                    if (pushTokenlist != null && !pushTokenlist.isEmpty()) {
                        JSONObject data = new JSONObject();
                        data.put("body", senderNick);
                        data.put("type", type);
                        data.put("contents_idx", contentsIdx);
                        data.put("comment_idx", 0);
                        data.put("image", img);
                        data.put("TokenList", pushTokenlist);

                        dpId = senderUuid + ".regist." + contentsIdx + "." + tagMemberUuid;
                        snsService.publish(data.toString(), arn, "PushAdmin", "admin3", dpId);
                    }
                }
            }

            // 컨텐츠에 맨션 된 회원 목록 조회
            List<String> metionMemberList = getMentionMember(contentsWriterInfo);

            // 멘션된 회원이 있다면
            if (metionMemberList != null && !metionMemberList.isEmpty()) {
                // 타입 타이틀 - 컨텐츠 멘션 : 5 [ sns_push_type ]
                type = pushService.getPushTypeTitle(5);

                // 한명씩 돌면서 토큰 조회
                for (String mentionMemberUuid : metionMemberList) {
                    // 토큰 리스트
                    List<String> pushTokenlist = null;
                    // 차단 내역 가져오기 ( 한명이라도 차단 했으면 true )
                    boolean chekBlock = super.bChkBlock(mentionMemberUuid, senderUuid);
                    // 본인이 본인을 멘션했는지 체크
                    boolean checkMe = Objects.equals(mentionMemberUuid, senderUuid);
                    // 본인이 아니고, 차단내역도 없으면
                    if (!chekBlock && !checkMe) {
                        // 토큰 조회 위해 푸시 DTO 세팅 - 컨텐츠 멘션 : 5
                        PushDto pushDto = PushDto.builder()
                                .receiverUuid(mentionMemberUuid)
                                .typeIdx(5)
                                .build();

                        // 야간 시간 체크
                        boolean checkNightTime = super.checkNightTime();
                        if (checkNightTime) {
                            // 푸시 받을 회원 토큰 리스트(fcm 토큰이 정상이고,알람 설정한 회원,야간알림 설정한 회원)
                            pushTokenlist = pushService.getNightPushTokenList(pushDto);
                        } else {
                            // 푸시 받을 회원 토큰 리스트(fcm 토큰이 정상이고,알람 설정한 회원)
                            pushTokenlist = pushService.getPushTokenList(pushDto);
                        }
                    }
                    if (pushTokenlist != null && !pushTokenlist.isEmpty()) {
                        JSONObject data = new JSONObject();
                        data.put("body", senderNick);
                        data.put("type", type);
                        data.put("contents_idx", contentsIdx);
                        data.put("comment_idx", 0);
                        data.put("image", img);
                        data.put("TokenList", pushTokenlist);

                        dpId = senderUuid + ".regist." + contentsIdx + "." + mentionMemberUuid;

                        snsService.publish(data.toString(), arn, "PushAdmin", "admin3", dpId);

                    }
                }
            }

            // 컨텐츠 작성자의 팔로워 회원 목록 가져오기
            List<String> followerList = getFollowerMember(senderUuid, writeDate);

            if (followerList != null && !followerList.isEmpty()) {
                // 타입 타이틀 - 팔로워 새글 : 4 [ sns_push_type ]
                type = pushService.getPushTypeTitle(4);

                // 한명씩 돌면서 토큰 조회
                for (String followerUuid : followerList) {
                    // 토큰 리스트
                    List<String> pushTokenlist = null;

                    // 토큰 조회 위해 푸시 DTO 세팅 - 팔로워 새글 : 4
                    PushDto pushDto = PushDto.builder()
                            .receiverUuid(followerUuid)
                            .typeIdx(4)
                            .build();

                    // 푸시 받을 회원 토큰 리스트(fcm 토큰이 정상이고,알람 설정한 회원)
                    pushTokenlist = pushService.getPushTokenList(pushDto);

                    if (pushTokenlist != null && !pushTokenlist.isEmpty()) {
                        JSONObject data = new JSONObject();
                        data.put("body", senderNick);
                        data.put("type", type);
                        data.put("contents_idx", contentsIdx);
                        data.put("comment_idx", 0);
                        data.put("image", img);
                        data.put("TokenList", pushTokenlist);

                        dpId = senderUuid + ".regist." + contentsIdx + "." + followerUuid;
                        snsService.publish(data.toString(), arn, "PushAdmin", "admin3", dpId);
                    }
                }
            }
        }
        // 컨텐츠 등록 END
        // 컨텐츠 수정 START
        else if (Objects.equals(action, "modify")) {
            // 컨텐츠 수정 전 한번이라도 이미지 태그 된 회원 목록 조회 ( 상태값 신경 X )
            List<String> prevImgTagMemberList = getPrevImgTagMember(contentsWriterInfo);

            // 이미지 태그 된 회원 목록 조회
            List<String> imgTagMemberList = getImgTagMember(contentsWriterInfo);

            // 이미지 태그 된 회원이 존재한다면
            if (imgTagMemberList != null && !imgTagMemberList.isEmpty()) {
                // 타입 타이틀 - 이미지 내 태그 푸시 타입 : 11 [ sns_push_type ]
                type = pushService.getPushTypeTitle(11);

                // 한명씩 돌면서 토큰 조회
                for (String tagMemberUuid : imgTagMemberList) {
                    // 기존에 태그된 회원이 아니라면 푸시 보내기
                    if (!prevImgTagMemberList.contains(tagMemberUuid)) {
                        // 토큰 리스트
                        List<String> pushTokenlist = null;
                        // 차단 내역 가져오기 ( 한명이라도 차단 했으면 true )
                        boolean chekBlock = super.bChkBlock(tagMemberUuid, senderUuid);
                        // 본인이 본인을 멘션했는지 체크
                        boolean checkMe = Objects.equals(tagMemberUuid, senderUuid);
                        // 본인이 아니고, 차단내역도 없으면
                        if (!chekBlock && !checkMe) {
                            // 토큰 조회 위해 푸시 DTO 세팅 - 이미지 내 태그 푸시 타입 : 11
                            PushDto pushDto = PushDto.builder()
                                    .receiverUuid(tagMemberUuid)
                                    .typeIdx(11)
                                    .build();

                            // 푸시 받을 회원 토큰 리스트(fcm 토큰이 정상이고,알람 설정한 회원)
                            pushTokenlist = pushService.getPushTokenList(pushDto);

                        }
                        if (pushTokenlist != null && !pushTokenlist.isEmpty()) {
                            JSONObject data = new JSONObject();
                            data.put("body", senderNick);
                            data.put("type", type);
                            data.put("contents_idx", contentsIdx);
                            data.put("comment_idx", 0);
                            data.put("image", img);
                            data.put("TokenList", pushTokenlist);

                            dpId = senderUuid + ".modify." + contentsIdx + "." + tagMemberUuid;
                            snsService.publish(data.toString(), arn, "PushAdmin", "admin3", dpId);
                        }
                    }
                }
            }

            // 컨텐츠에 맨션 된 회원 목록 조회
            List<String> metionMemberList = getMentionMember(contentsWriterInfo);

            // 멘션된 회원이 있다면
            if (metionMemberList != null && !metionMemberList.isEmpty()) {
                // 타입 타이틀 - 컨텐츠 멘션 : 5 [ sns_push_type ]
                type = pushService.getPushTypeTitle(5);

                // 한명씩 돌면서 토큰 조회
                for (String mentionMemberUuid : metionMemberList) {
                    // 토큰 리스트
                    List<String> pushTokenlist = null;
                    // 차단 내역 가져오기 ( 한명이라도 차단 했으면 true )
                    boolean chekBlock = super.bChkBlock(mentionMemberUuid, senderUuid);
                    // 본인이 본인을 멘션했는지 체크
                    boolean checkMe = Objects.equals(mentionMemberUuid, senderUuid);
                    // 본인이 아니고, 차단내역도 없으면
                    if (!chekBlock && !checkMe) {
                        // 토큰 조회 위해 푸시 DTO 세팅 - 컨텐츠 멘션 : 5
                        PushDto pushDto = PushDto.builder()
                                .receiverUuid(mentionMemberUuid)
                                .typeIdx(5)
                                .build();

                        // 야간 시간 체크
                        boolean checkNightTime = super.checkNightTime();
                        if (checkNightTime) {
                            // 푸시 받을 회원 토큰 리스트(fcm 토큰이 정상이고,알람 설정한 회원,야간알림 설정한 회원)
                            pushTokenlist = pushService.getNightPushTokenList(pushDto);
                        } else {
                            // 푸시 받을 회원 토큰 리스트(fcm 토큰이 정상이고,알람 설정한 회원)
                            pushTokenlist = pushService.getPushTokenList(pushDto);
                        }
                    }
                    if (pushTokenlist != null && !pushTokenlist.isEmpty()) {
                        JSONObject data = new JSONObject();
                        data.put("body", senderNick);
                        data.put("type", type);
                        data.put("contents_idx", contentsIdx);
                        data.put("comment_idx", 0);
                        data.put("image", img);
                        data.put("TokenList", pushTokenlist);

                        dpId = senderUuid + ".modify." + contentsIdx + "." + mentionMemberUuid;

                        snsService.publish(data.toString(), arn, "PushAdmin", "admin3", dpId);

                    }
                }
            }
        }
    }

    /*****************************************************
     *  SubFunction - select
     ****************************************************/
    /**
     * 컨텐츠에 이미지 태그 된 회원 리스트 가져오기
     *
     * @param contentsPushDto 컨텐츠IDX
     * @return 이미지 태그 된 회원 리스트
     */
    public List<String> getImgTagMember(ContentsPushDto contentsPushDto) {
        return contentspushDaoSub.getImgTagMember(contentsPushDto);
    }

    /**
     * 수정 전 컨텐츠에 태그 되었던 이미지 태그 리스트
     *
     * @param contentsPushDto
     * @return
     */
    public List<String> getPrevImgTagMember(ContentsPushDto contentsPushDto) {
        return contentspushDaoSub.getPrevImgTagMember(contentsPushDto);
    }


    /**
     * 컨텐츠에 멘션 된 회원 리스트 가져오기
     *
     * @param contentsPushDto 컨텐츠IDX
     * @return 컨텐츠에 멘션 회원 리스트
     */
    public List<String> getMentionMember(ContentsPushDto contentsPushDto) {
        return contentspushDaoSub.getMentionMember(contentsPushDto);
    }

    /**
     * 글 작성자의 팔로워 목록 가져오기
     *
     * @param memberUuid 글 작성자
     * @return 팔로워 목록
     */
    public List<String> getFollowerMember(String memberUuid, String regDate) {
        ContentsPushDto contentsPushDto = ContentsPushDto.builder()
                .regDate(regDate)
                .memberUuid(memberUuid)
                .build();

        return contentspushDaoSub.getFollowerMember(contentsPushDto);
    }

    /**
     * 컨텐츠 작성자 정보 가져오기
     *
     * @param contentsIdx 컨텐츠idx
     * @return memeberidx nick regdate
     */
    public ContentsPushDto getContentsWriterInfo(Long contentsIdx) {
        return contentspushDaoSub.getContentsWriterInfo(contentsIdx);
    }

    /**
     * 첫번째 이미지 가져오기
     *
     * @param contentsIdx 컨텐츠idx
     * @return img url
     */
    public String getContentsImg(Long contentsIdx) {
        return contentspushDaoSub.getContentsImg(contentsIdx);
    }

}
