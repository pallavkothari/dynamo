package com.pk

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class DynamoDbConfig {

    @Value("\${dynamo_endpoint}")
    val amazonDynamoDBEndpoint: String? = null

    @Value("\${aws_access_key}")
    val amazonAWSAccessKey: String? = null

    @Value("\${aws_secret_key}")
    val amazonAWSSecretKey: String? = null

    @Bean
    fun amazonDynamoDB(awsCredentials: AWSCredentials): AmazonDynamoDB {
        return AmazonDynamoDBClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(awsCredentials))
            .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(amazonDynamoDBEndpoint, "us-east-1"))
            .build()
    }

    @Bean
    fun amazonAWSCredentials(): AWSCredentials {
        // Or use an AWSCredentialsProvider/AWSCredentialsProviderChain
        return BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey)
    }
}
