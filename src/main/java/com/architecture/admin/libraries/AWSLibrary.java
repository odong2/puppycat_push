package com.architecture.admin.libraries;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.architecture.admin.config.AWSConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

import java.util.UUID;

/*
    AWS 자격 증명 서비스
 */
@Service
@RequiredArgsConstructor
public class AWSLibrary {

    private final AWSConfig awsConfig;
    private final ObjectMapper objectMapper;
    private final AmazonSQS amazonSQS;

    public AwsCredentialsProvider getAwsCredentials(String accessKeyID, String secretAccessKey) {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKeyID, secretAccessKey);
        return () -> awsBasicCredentials;
    }

    // SNS Client info
    public SnsClient getSnsClient() {
        return SnsClient.builder()
                .credentialsProvider(
                        getAwsCredentials(awsConfig.getAccessKey(), awsConfig.getSecretKey())
                ).region(Region.of(awsConfig.getRegion()))
                .build();
    }

    // Message send action function
    public SendMessageResult sendMessage(Object sendData, String url, String groupId) throws JsonProcessingException {
        // data String 변환
        String jsonData = objectMapper.writeValueAsString(sendData);
        // message send
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(url)
                .withMessageGroupId(groupId)
                .withMessageBody(jsonData)
                .withMessageDeduplicationId(UUID.randomUUID().toString());

        return amazonSQS.sendMessage(sendMessageRequest);
    }
}
