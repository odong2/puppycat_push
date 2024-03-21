package com.architecture.admin.services.push.comment;

import com.architecture.admin.models.dao.push.CommentPushDao;
import com.architecture.admin.models.daosub.push.CommentPushDaoSub;
import com.architecture.admin.models.dto.push.CommentPushDto;
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
public class CommentPushPubService extends BaseService {

    private final CommentPushDao commentPushDao;
    private final CommentPushDaoSub commentPushDaoSub;
    private final PushService pushService;
    private final SNSService snsService;
    @Value("${cloud.aws.sns.push.comment.topic.arn}")
    private String snsTopicARN;
    @Value("${cloud.aws.sns.push.comment.topic.arn2}")
    private String snsTopicARN2;
    @Value("${cloud.aws.sns.push.comment.topic.arn3}")
    private String snsTopicARN3;

    /*****************************************************
     *  Modules
     ****************************************************/
    @SqsListener(value = {"${cloud.aws.sqs.push.all.comment.pushPub.listener}", "${cloud.aws.sqs.push.all.comment.pushPub.listener2}", "${cloud.aws.sqs.push.all.comment.pushPub.listener3}"}, deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void conmmentPush(@Payload String message) {
        // 푸시 타입
        String type;
        // sns messageDeduplicationId
        String dpId;

        // 큐 data 꺼내오기
        JSONObject obj = new JSONObject(message);
        String action = obj.getString("action");
        long commentIdx = obj.getLong("comment_idx");

        // 댓글 IDX 를 3으로 나눠서 QUE에 분산 시킴
        Long remain = commentIdx % 3;
        String arn;

        if (remain == 0) {
            arn = snsTopicARN;
        } else if (remain == 1) {
            arn = snsTopicARN2;
        } else {
            arn = snsTopicARN3;
        }

        // 댓글 작성 정보
        CommentPushDto commentWriterInfo = selectCommentWriterInfo(commentIdx);
        String senderUuid = commentWriterInfo.getMemberUuid();           // 댓글 작성자 uuid
        String senderNick = commentWriterInfo.getNick();                 // 댓글 작성자 닉네임
        long contentsIdx = commentWriterInfo.getContentsIdx();           // 댓글이 작성된 컨텐츠 idx
        long parentIdx = commentWriterInfo.getParentIdx();               // 부모댓글 idx

        // 댓글 등록 START
        if (Objects.equals(action, "regist")) {
            // 댓글에 맨션 된 회원 목록 조회
            List<String> metionMemberList = getMentionMember(commentIdx);

            // 멘션된 회원이 있다면
            if (metionMemberList != null && !metionMemberList.isEmpty()) {
                // 타입 타이틀 - 댓글 멘션 : 7 [ sns_push_type ]
                type = pushService.getPushTypeTitle(7);

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
                        // 토큰 조회 위해 푸시 DTO 세팅 - 댓글 멘션 : 7
                        PushDto pushDto = PushDto.builder()
                                .receiverUuid(mentionMemberUuid)
                                .typeIdx(7)
                                .build();
                        // 푸시 받을 회원 토큰 리스트(fcm 토큰이 정상이고,알람 설정한 회원)
                        pushTokenlist = pushService.getPushTokenList(pushDto);

                    }

                    if (pushTokenlist != null && !pushTokenlist.isEmpty()) {
                        JSONObject data = new JSONObject();
                        data.put("body", senderNick);
                        data.put("type", type);
                        data.put("contents_idx", contentsIdx);
                        data.put("comment_idx", commentIdx);
                        data.put("image", "");
                        data.put("TokenList", pushTokenlist);

                        dpId = senderUuid + ".regist." + contentsIdx + "." + commentIdx + "." + mentionMemberUuid;

                        snsService.publish(data.toString(), arn, "PushAdmin", "admin3", dpId);
                    }
                }
            }
            // 멘션푸시 end

            // 댓글 작성 푸시
            String receiverUuid;
            int typeIdx;
            // 댓글이면 글작성자에게 푸시
            if (parentIdx <= 0) {
                receiverUuid = selectContentsWriter(commentIdx);
                // 타입 타이틀 - 새 댓글 : 6 [ sns_push_type ]
                typeIdx = 6;
                type = pushService.getPushTypeTitle(typeIdx);

            }
            //  대댓글이면 댓글 작성자에게 푸시
            else {
                receiverUuid = selectParentCommentWriter(parentIdx);
                // 타입 타이틀 - 새 대댓글 : 8 [ sns_push_type ]
                typeIdx = 8;
                type = pushService.getPushTypeTitle(typeIdx);
            }

            if (receiverUuid != null && !receiverUuid.equals("")) {
                // 차단 내역 가져오기 ( 한명이라도 차단 했으면 true )
                boolean chekBlock = super.bChkBlock(receiverUuid, senderUuid);
                // 본인이 본인을 멘션했는지 체크
                boolean checkMe = Objects.equals(receiverUuid, senderUuid);
                // 토큰 리스트
                List<String> pushTokenlist = null;
                // 본인이 아니고, 차단내역도 없으면
                if (!chekBlock && !checkMe) {
                    // 토큰 조회 위해 푸시 DTO 세팅 - 이미지 내 태그 푸시 타입 : 11
                    PushDto pushDto = PushDto.builder()
                            .receiverUuid(receiverUuid)
                            .typeIdx(typeIdx)
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
                    data.put("comment_idx", commentIdx);
                    data.put("image", "");
                    data.put("TokenList", pushTokenlist);

                    dpId = senderUuid + ".regist." + contentsIdx + "." + commentIdx + "." + receiverUuid;

                    snsService.publish(data.toString(), arn, "PushAdmin", "admin3", dpId);
                }
            }
        }
        // 댓글 등록 END
        // 댓글 수정 START
        else if (Objects.equals(action, "modify")) {

            // 댓글 수정 전 한번이라도 멘션된 회원 목록 조회 ( 상태값 신경 X )
            List<String> prevMentionMemberList = getPrevMentionMember(commentWriterInfo);

            // 댓글 멘션된 회원 IDX 리스트 조회
            List<String> mentionMemberList = getMentionMember(commentIdx);

            if (mentionMemberList != null && !mentionMemberList.isEmpty()) {
                // 타입 타이틀 - 이미지 내 태그 푸시 타입 : 11 [ sns_push_type ]
                type = pushService.getPushTypeTitle(7);

                // 한명씩 돌면서 토큰 조회
                for (String mentionMemberUuid : mentionMemberList) {

                    // 기존에 멘션된 회원이 아니라면 푸시 보내기
                    if (!prevMentionMemberList.contains(mentionMemberUuid)) {
                        // 토큰 리스트
                        List<String> pushTokenlist = null;
                        // 차단 내역 가져오기 ( 한명이라도 차단 했으면 true )
                        boolean chekBlock = super.bChkBlock(mentionMemberUuid, senderUuid);
                        // 본인이 본인을 멘션했는지 체크
                        boolean checkMe = Objects.equals(mentionMemberUuid, senderUuid);
                        // 본인이 아니고, 차단내역도 없으면
                        if (!chekBlock && !checkMe) {

                            // 토큰 조회 위해 푸시 DTO 세팅 - 댓글 멘션 타입 : 7
                            PushDto pushDto = PushDto.builder()
                                    .receiverUuid(mentionMemberUuid)
                                    .typeIdx(7)
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
                            data.put("comment_idx", commentIdx);
                            data.put("image", "");
                            data.put("TokenList", pushTokenlist);

                            dpId = senderUuid + ".modify." + contentsIdx + "." + commentIdx + "." + mentionMemberUuid;

                            snsService.publish(data.toString(), arn, "PushAdmin", "admin3", dpId);
                        }
                    }
                }
            }
        }
    }


    /*****************************************************
     *  SubFunction - select
     ****************************************************/
    /**
     * 댓글 작성자  정보 가져오기
     *
     * @param commentIdx 댓글 idx
     * @return memberIdx nick contentsIdx parentIdx
     */
    public CommentPushDto selectCommentWriterInfo(Long commentIdx) {
        return commentPushDaoSub.getCommentWriterInfo(commentIdx);
    }

    /**
     * 댓글에 멘션 된 회원 리스트 가져오기
     *
     * @param commentIdx 댓글IDX
     * @return 댓글 멘션 회원 리스트
     */
    public List<String> getMentionMember(Long commentIdx) {
        return commentPushDaoSub.getMentionMember(commentIdx);
    }


    /**
     * 댓글 수정전 멘션 된 회원 리스트
     *
     * @param commentWriterInfo
     * @return
     */
    private List<String> getPrevMentionMember(CommentPushDto commentWriterInfo) {
        return commentPushDaoSub.getPrevMentionMember(commentWriterInfo);

    }

    /**
     * 댓글 시 컨텐츠 작성자 uuid가져오기
     *
     * @param commentIdx 댓글IDX
     * @return 컨텐츠 작성자 uuid
     */
    public String selectContentsWriter(Long commentIdx) {
        return commentPushDaoSub.getContentsMember(commentIdx);
    }


    /**
     * 대댓글 시 댓글 작성자 idx가져오기
     *
     * @param commentIdx 부모댓글IDX
     * @return 부모댓글 작성자 uuid
     */
    public String selectParentCommentWriter(Long commentIdx) {
        return commentPushDaoSub.getParentCommentMember(commentIdx);
    }
}
