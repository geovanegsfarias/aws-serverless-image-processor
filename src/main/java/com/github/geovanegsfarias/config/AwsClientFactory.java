package com.github.geovanegsfarias.config;

import com.github.geovanegsfarias.model.Image;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

public class AwsClientFactory {
    private static final String ENDPOINT = System.getenv("ENDPOINT_URL"); // http://host.docker.internal:4566
    private static final String ACCESS_KEY_ID = System.getenv("ACCESS_KEY_ID");
    private static final String SECRET_ACCESS_KEY = System.getenv("SECRET_ACCESS_KEY");
    private static final String TABLE_NAME = "images_table";

    public static S3Client getS3Client() {
        S3ClientBuilder s3ClientBuilder = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY_ID, SECRET_ACCESS_KEY)));

        if (ENDPOINT != null) {
            s3ClientBuilder
                    .endpointOverride(URI.create(ENDPOINT))
                    .forcePathStyle(true);
        }

        return s3ClientBuilder.build();
    }

    public static DynamoDbClient getDynamoDbClient() {
        DynamoDbClientBuilder dynamoDbClientBuilder = DynamoDbClient.builder()
                .region(Region.US_EAST_1);

        if (ENDPOINT != null) {
            dynamoDbClientBuilder
                    .endpointOverride(URI.create(ENDPOINT))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(ACCESS_KEY_ID, SECRET_ACCESS_KEY)
                            )
                    );
        }

        return dynamoDbClientBuilder.build();
    }

    public static DynamoDbEnhancedClient getDynamoDbEnhancedClient() {
        DynamoDbClient dynamoDbClient = getDynamoDbClient();

        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    public static DynamoDbTable<Image> getDynamoDbTable(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        return dynamoDbEnhancedClient
                .table(TABLE_NAME, TableSchema.fromBean(Image.class));
    }

}
