package com.architecture.admin.services.push.admin;

import com.architecture.admin.config.NotiConfig;
import com.architecture.admin.services.BaseService;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "use.sqs.listener.enabled", havingValue = "true")
public class AdminPushSendService extends BaseService {

    private final FirebaseMessaging firebaseMessaging;
    private static final Logger logger = LoggerFactory.getLogger(AdminPushSendService.class);

    @SqsListener(value = {"${cloud.aws.sqs.push.all.admin.pushSend.listener}", "${cloud.aws.sqs.push.all.admin.pushSend.listener2}", "${cloud.aws.sqs.push.all.admin.pushSend.listener3}"}, deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void sendFcm(@Payload String message) {
        // 큐 data
        JSONObject obj = new JSONObject(message);
        String token = obj.getString("token");

        // notification builder
        Notification notification = Notification.builder()
                .setTitle(obj.getString("title"))
                .setBody(obj.getString("body"))
                .setImage(obj.getString("image"))
                .build();

        //apnsConfig Set 하기
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setContentAvailable(true)
                        .build()).build();

        Map<String, String> msg = new HashMap<>();
        msg.put("push_idx", obj.getString("push_idx"));
        msg.put("title", obj.getString("title"));
        msg.put("body", obj.getString("body"));
        msg.put("type", obj.getString("type"));
        msg.put("contents_idx", obj.getString("contents_idx"));
        msg.put("contents_type", obj.getString("contents_type"));
        msg.put("image", obj.getString("image"));

        Message fcmMsg = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .setApnsConfig(apnsConfig)
                .putAllData(msg)
                .build();

        try {
            firebaseMessaging.send(fcmMsg);
            logger.info(obj.getString("push_idx")+"|success|"+obj.getString("token"));
            // 전송 성공 처리
        } catch (FirebaseMessagingException e) {
            // 전송 실패 처리
            String errorMessage = e.getMessage();
            logger.error(obj.getString("push_idx")+"|error|"+obj.getString("token")+"|"+errorMessage);
        }
    }
}
