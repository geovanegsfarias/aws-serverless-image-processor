package com.github.geovanegsfarias;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.github.geovanegsfarias.config.AwsClientFactory;
import com.github.geovanegsfarias.model.Image;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Handler implements RequestHandler<S3Event, String> {
    private final S3Client s3Client;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<Image> dynamoDbTable;

    public Handler() {
        this.s3Client = AwsClientFactory.getS3Client();
        this.dynamoDbEnhancedClient = AwsClientFactory.getDynamoDbEnhancedClient();
        this.dynamoDbTable = AwsClientFactory.getDynamoDbTable(dynamoDbEnhancedClient);
    }

    @Override
    public String handleRequest(S3Event input, Context context) {
        LambdaLogger logger = context.getLogger();

        logger.log("Request received");

        List<S3EventNotification.S3EventNotificationRecord> records = input.getRecords();

        records.forEach(r -> {
            try {
                saveImageMetadata(r);
            } catch (Exception e) {
                logger.log("Erro ao processar " + r.getS3().getObject().getKey() + ": " + e.getMessage());
            }
        });

        return "ok";
    }

    private void saveImageMetadata(S3EventNotification.S3EventNotificationRecord eventNotificationRecord) {
        String bucketName = eventNotificationRecord.getS3().getBucket().getName();

        String objectKey = eventNotificationRecord.getS3().getObject().getKey();

        String objectName = objectKey.substring(objectKey.lastIndexOf('/') + 1);

        HeadObjectRequest request = HeadObjectRequest.builder().bucket(bucketName).key(objectKey).build();

        HeadObjectResponse response = s3Client.headObject(request);

        String contentType = response.contentType();

        Long contentLength = response.contentLength();

        Instant lastModified = response.lastModified();

        Image image = new Image(UUID.randomUUID().toString(), objectName, objectKey, contentType, contentLength, lastModified);

        dynamoDbTable.putItem(image);
    }
}