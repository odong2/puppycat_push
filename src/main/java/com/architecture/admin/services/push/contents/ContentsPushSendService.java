package com.architecture.admin.services.push.contents;

import com.architecture.admin.config.NotiConfig;
import com.architecture.admin.models.dto.push.PushDto;
import com.architecture.admin.services.BaseService;
import com.architecture.admin.services.push.PushService;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "use.sqs.listener.enabled", havingValue = "true")
public class ContentsPushSendService extends BaseService {

    private final FirebaseMessaging firebaseMessaging;
    private final PushService pushService;

    @SqsListener(value = {"${cloud.aws.sqs.push.contents.pushSend.listener}", "${cloud.aws.sqs.push.contents.pushSend.listener2}", "${cloud.aws.sqs.push.contents.pushSend.listener3}"}, deletionPolicy = SqsMessageDeletionPolicy.NO_REDRIVE)
    public void contentsSendFcm(@Payload String message) {
        // 큐 data
        JSONObject obj = new JSONObject(message);

        JSONArray token = obj.getJSONArray("TokenList");
        List<String> tokenList = new ArrayList<>();
        for (int i = 0; i < token.length(); i++) {
            String element = token.getString(i);
            tokenList.add(element);
        }

        // notification builder
        Notification notification = Notification.builder()
                .setTitle(NotiConfig.getNotiTitle().get(obj.getString("type")))
                .setBody(obj.getString("body") + NotiConfig.getNotiBody().get(obj.getString("type")))
                .setImage(obj.getString("image"))
                .build();

        //apnsConfig Set 하기
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setContentAvailable(true)
                        .build()).build();

        Map<String, String> msg = new HashMap<>();
        msg.put("body", obj.getString("body"));
        msg.put("type", obj.getString("type"));
        msg.put("contents_idx", String.valueOf(obj.getLong("contents_idx")));
        msg.put("comment_idx", String.valueOf(obj.getLong("comment_idx")));
        msg.put("image", obj.getString("image"));

        // 푸시 세팅
        MulticastMessage.Builder builder = MulticastMessage.builder();
        MulticastMessage fcmMsg = builder
                .addAllTokens(tokenList)
                .setNotification(notification)
                .setApnsConfig(apnsConfig)
                .putAllData(msg)
                .build();
        try {
            // 푸시 보내기
            BatchResponse response = firebaseMessaging.sendMulticast(fcmMsg);
            if (response != null) {
                List<SendResponse> responses;
                responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    // 토큰 세팅
                    String sToken = (tokenList.get(i));
                    if (!responses.get(i).isSuccessful()) {
                        PushDto pushDto = PushDto.builder()
                                .typeTitle(obj.getString("type"))
                                .contentsIdx(obj.getLong("contents_idx"))
                                .commentIdx(obj.getLong("comment_idx"))
                                .body(obj.getString("body"))
                                .img(obj.getString("image"))
                                .fcmToken(sToken)
                                .errorCode("비 정상적인 토큰")
                                .build();
                        // 실패 토큰 로그 쌓기
                        pushService.insertFailLog(pushDto);
                    }
                }
            }
        } catch (FirebaseMessagingException e) {
            PushDto pushDto = PushDto.builder()
                    .typeTitle(obj.getString("type"))
                    .contentsIdx(obj.getLong("contents_idx"))
                    .commentIdx(obj.getLong("comment_idx"))
                    .body(obj.getString("body"))
                    .img(obj.getString("image"))
                    .fcmToken(tokenList.toString())
                    .errorCode(e.getMessage())
                    .build();

            // 실패 토큰 로그 쌓기
            pushService.insertFailLog(pushDto);
        }
    }
}
