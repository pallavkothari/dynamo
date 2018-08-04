package com.pk

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest
class DynamoApplicationTests {

    private val CAPACITY = 5L

    @Autowired
    private val amazonDynamoDB: AmazonDynamoDB? = null

    @Before
    @Throws(Exception::class)
    fun init() {
        // Delete User table in case it exists
        amazonDynamoDB!!.listTables().tableNames.stream().filter { tableName -> tableName == User.TABLE_NAME }
            .forEach { tableName -> amazonDynamoDB.deleteTable(tableName) }

        //Create User table
        amazonDynamoDB.createTable(
            DynamoDBMapper(amazonDynamoDB)
                .generateCreateTableRequest(User::class.java)
                .withProvisionedThroughput(ProvisionedThroughput(CAPACITY, CAPACITY))
        )
    }

    @Test
    fun testSavingAndAppending() {
        val user = User("pk")
        with(user) {
            firstName = "p"
            lastName = "k"
            integerSetAttribute = setOf(1)
        }

        val dbMapper = DynamoDBMapper(amazonDynamoDB)
        dbMapper.save(user)
        val loaded = dbMapper.load(user)
        assertThat(loaded.integerSetAttribute).isEqualTo(setOf(1))

        loaded.integerSetAttribute = loaded.integerSetAttribute.plus(2).plus(3)
        dbMapper.save(loaded)
        assertThat(dbMapper.load(user).integerSetAttribute).isEqualTo(setOf(1, 2, 3))
    }
}
