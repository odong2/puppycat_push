package com.architecture.admin.services;

import com.architecture.admin.libraries.AWSLibrary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;


@Slf4j
@RequiredArgsConstructor
@Component
public class SNSService extends BaseService {
    private final AWSLibrary awsLibrary;

    /**
     * SQS 메세지 발신 성공 유무
     *
     * @param publishResponse
     * @return String
     */
    private String getResultSendMsg(PublishResponse publishResponse) {
        String sqsId = publishResponse.messageId();
        String sMessage;

        if ("".equals(sqsId) || sqsId == null) {
            sMessage = super.langMessage("lang.admin.exception.sqs.send_fail");
        } else {
            sMessage = super.langMessage("lang.admin.success.sqs.send");
        }
        return sMessage;
    }

    public String publish(String message, String arn, String subject, String messageGroupId, String dpId) {
        SnsClient snsClient = awsLibrary.getSnsClient();

        final PublishRequest publishRequest = PublishRequest.builder()
                .topicArn(arn)
                .subject(subject) // 제목
                .message(message) // 실제 사용할 Json data
                .messageGroupId(messageGroupId) // Group ID
                .messageDeduplicationId(dpId)
                .build();

        PublishResponse publishResponse = snsClient.publish(publishRequest);
        snsClient.close();

        // SQS 통신 성공 유무
        return getResultSendMsg(publishResponse);
    }

    public String publish(String message, String arn, String subject) {
        SnsClient snsClient = awsLibrary.getSnsClient();

        final PublishRequest publishRequest = PublishRequest.builder()
                .topicArn(arn)
                .subject(subject) // 제목
                .message(message) // 실제 사용할 Json data
                .build();

        PublishResponse publishResponse = snsClient.publish(publishRequest);
        snsClient.close();

        // SQS 통신 성공 유무
        return getResultSendMsg(publishResponse);
    }

}
