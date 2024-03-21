package com.architecture.admin.services;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.architecture.admin.libraries.AWSLibrary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class SQSService extends BaseService {
    private final AWSLibrary awsLibrary;
    private final ObjectMapper objectMapper;
    private final AmazonSQS amazonSQS;

    @Value("${cloud.aws.sqs.queue.url}")
    private String url;

    /**
     * @param message
     * @return String
     * @throws JsonProcessingException
     */
    public String send(@RequestBody Map<String, Object> message) throws JsonProcessingException {
        SendMessageResult sendMessageResult = awsLibrary.sendMessage(message, url, "test");

        // SQS 통신 성공 유무
        return getResultSendMsg(sendMessageResult);
    }


    /**
     * SQS 메세지 발신 성공 유무
     *
     * @param sendMessageResult
     * @return String
     */
    private String getResultSendMsg(SendMessageResult sendMessageResult) {
        String sqsId = sendMessageResult.getMessageId();
        String sMessage = "";

        if ("".equals(sqsId) || sqsId == null) {
            sMessage = super.langMessage("lang.admin.exception.sqs.send_fail");
        } else {
            sMessage = super.langMessage("lang.admin.success.sqs.send");
        }
        return sMessage;
    }

    /**
     *
     * @param message
     * @param headers
     *
     * value = queue 이름
     *
     * SqsMessageDeletionPolicy Option
     *   메소드로 Message 들어오면 무조건 삭제요청을 보낸다
     *   [ALWAYS]
     *   절대 삭제 요청을 보내지 않는다.
     *
     *   Acknowledgment 파라미터를 바인딩 받아 ack 메소드를 호출할때 삭제 요청을 보낸다
     *   [NEVER]
     *
     *   Red rive policy(DLQ)가 정의되지 않았으면 메시지를 삭제한다.
     *   [NO_REDRIVE]
     *
     *   SqsListener 어노테이션이 붙은 메소드에서 에러가 나지 않으면 메시지를 삭제한다.
     *   에러가 난 경우에는 메시지를 삭제하지 않는다.
     *   [ON_SUCCESS]
     *
     *   value 속성은 full url 으로 지정해줘야함
     */
//    @SqsListener(value = "outMember.fifo", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
//    public void listenContents(@Payload String message, @Headers Map<String, String> headers) {
//
//        JSONObject obj = new JSONObject(message);
//        System.out.printf("여기 = " + obj.get("number"));
//        System.out.printf("여기1 = " + obj.get("message"));
//    }

//
//    @SqsListener(value = "info.fifo", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS )
//    public void listenInfo(@Payload String message, @Headers Map<String, String> headers) {
//
//        JSONObject obj = new JSONObject(message);
//        System.out.printf("여기 = " + obj.get("number"));
//        System.out.printf("여기1 = " + obj.get("message"));
//    }
}
