package com.practice.controller;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
public class SNSController {

    private static final Logger logger = LoggerFactory.getLogger(SNSController.class);

    @Autowired
    private AmazonSNSClient snsClient;

//    @Autowired
//    private ObjectMapper objectMapper;

    @Value("${sns.arn}")
    private String arn;

    @GetMapping("/{phoneNumber}")
    public ResponseEntity<ApiResponse> createSubscription(@PathVariable String phoneNumber) {
        ApiResponse apiResponse = new ApiResponse();

        if (!phoneNumber.contains("+91")) {
            phoneNumber = "+91" + phoneNumber;
        }
        SubscribeRequest request = new SubscribeRequest()
                .withTopicArn(arn)
                .withProtocol("sms")
                .withEndpoint(phoneNumber);

        try {
            SubscribeResult result = snsClient.subscribe(request);

            if (result.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.OK.value()) {
                apiResponse.setData("");
                apiResponse.setMessage("Phone number subscribed");
                apiResponse.setStatusCode(HttpStatus.OK.value());
            } else {
                apiResponse.setData(null);
                apiResponse.setMessage("Failed to subscribe");
                apiResponse.setStatusCode(result.getSdkHttpMetadata().getHttpStatusCode());
            }
        } catch (Exception e) {
            logger.error("Exception {}", e.getMessage());
            apiResponse.setData(null);
            apiResponse.setMessage("Something went wrong while subscribing");
            apiResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return new ResponseEntity<>(apiResponse, HttpStatusCode.valueOf(apiResponse.getStatusCode()));
    }

    @GetMapping("/send-message")
    public ResponseEntity<ApiResponse> sendMessage(@RequestBody Map<String, Object> messageData) {
        ApiResponse apiResponse = new ApiResponse();

        Map<String, MessageAttributeValue> messageAttributeValueMap = new HashMap<>();
        messageAttributeValueMap.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()
                .withStringValue("mySenderId")
                .withDataType("String"));
        messageAttributeValueMap.put("AWS.SNS.SMS.MaxPrice", new MessageAttributeValue()
                .withStringValue("0.50")
                .withDataType("Number"));
        messageAttributeValueMap.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()
                .withStringValue("Transactional")
                .withDataType("String"));

        PublishRequest request = new PublishRequest()
                .withMessageAttributes(messageAttributeValueMap)
//                .withTopicArn(arn)
                .withPhoneNumber("+918160039232")
                .withMessage((String) messageData.get("message"));

        try {
            PublishResult result = snsClient.publish(request);

            if (result.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.OK.value()) {
                apiResponse.setData("");
                apiResponse.setMessage("Message sent to all the subscribers");
                apiResponse.setStatusCode(HttpStatus.OK.value());
            } else {
                apiResponse.setData(null);
                apiResponse.setMessage("Failed to sent message to all the subscribers");
                apiResponse.setStatusCode(result.getSdkHttpMetadata().getHttpStatusCode());
            }
        } catch (Exception e) {
            logger.error("Exception {}", e.getMessage());
            apiResponse.setData(null);
            apiResponse.setMessage("Something went wrong while sending message to all the subscribers");
            apiResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return new ResponseEntity<>(apiResponse, HttpStatusCode.valueOf(apiResponse.getStatusCode()));
    }
}
