package com.example.kbuddy_backend.s3.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
* naver cloud -> s3로 전환이 필요함.
* */
@Configuration
public class BucketConfig {


    @Value("${cloud.naver.s3.endPoint}")
    private String endPoint;

    @Value("${cloud.naver.s3.region}")
    private String regionName;

    @Value("${cloud.naver.s3.accessKey}")
    private String accessKey;

    @Value("${cloud.naver.s3.secretKey}")
    private String secretKey;

    @Bean
    public AmazonS3 amazonS3Client() {
        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return  AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, regionName))
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .build();
    }

}
