package com.pk

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.pk.User.Companion.TABLE_NAME


@DynamoDBTable(tableName = TABLE_NAME)
data class User(
    @DynamoDBHashKey var id: String = "",
    @DynamoDBAttribute var firstName: String = "",
    @DynamoDBAttribute var lastName: String = "",
    @DynamoDBAttribute var integerSetAttribute: Set<Int> = setOf()
) {
    companion object {
        const val TABLE_NAME = "User"
    }
}