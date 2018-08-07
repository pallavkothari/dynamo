package com.pk

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior.APPEND_SET
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression
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
    private val key = User("pk")

    @Autowired
    private val amazonDynamoDB: AmazonDynamoDB? = null

    private lateinit var dbMapper: DynamoDBMapper

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

        dbMapper = DynamoDBMapper(amazonDynamoDB)
    }

    @Test
    fun simpleSave() {
        val toSave = key.copy()
        with(toSave) {
            firstName = "p"
            lastName = "k"
            integerSetAttribute = setOf(1)
        }

        dbMapper.save(toSave)

        val (_, firstName, lastName, integerSetAttribute) = dbMapper.load(key)
        assertThat(integerSetAttribute).isEqualTo(setOf(1))
        assertThat(firstName).isEqualTo("p")  // did not get clobbered
        assertThat(lastName).isEqualTo("k")
    }

    @Test
    fun defaultBehavior() {
        simpleSave()
        val toSave = key.copy(firstName = "newFirstName", integerSetAttribute = setOf(1))
        dbMapper.save(toSave)
        val (_, firstName, lastName, integerSetAttribute) = dbMapper.load(key)
        assertThat(firstName).isEqualTo("newFirstName")
        assertThat(integerSetAttribute).isEqualTo(setOf(1))
        assertThat(lastName).isEmpty()      // gets clobbered because of the default save behavior
    }

    @Test
    fun appendSet() {
        simpleSave()
        val toSave = key.copy(integerSetAttribute = setOf(2))
        // see https://aws.amazon.com/blogs/developer/using-the-savebehavior-configuration-for-the-dynamodbmapper/
        val appendSetBehavior = DynamoDBMapperConfig.builder().withSaveBehavior(APPEND_SET).build()
        dbMapper.save(toSave, appendSetBehavior)
        val (_, firstName, lastName, integerSetAttribute) = dbMapper.load(key)
        assertThat(firstName).isEqualTo("p")
        assertThat(lastName).isEqualTo("k")
        assertThat(integerSetAttribute).isEqualTo(setOf(1, 2))
    }

    @Test
    fun updateSkipNulls() {
        // there's no delete so just update the set with update_skip_nulls
        simpleSave()
        val toSave = key.copy(integerSetAttribute = setOf(2))
        val updateSkipNullsBehavior = DynamoDBMapperConfig.builder().withSaveBehavior(UPDATE_SKIP_NULL_ATTRIBUTES).build()
        dbMapper.save(toSave, updateSkipNullsBehavior)
        val (_, firstName, lastName, integerSetAttribute) = dbMapper.load(key)
        assertThat(integerSetAttribute).isEqualTo(setOf(2))
        assertThat(firstName).isEqualTo("p")
        assertThat(lastName).isEqualTo("k")
    }

    @Test
    fun scan() {
        for (i in 1..10) {
            dbMapper.save(User("user$i", integerSetAttribute = setOf(i)))
        }
        val paginatedScanList = dbMapper.scan(User::class.java, DynamoDBScanExpression())
        println("found ${paginatedScanList.size} items")
        assertThat(paginatedScanList.size).isEqualTo(10)
        paginatedScanList.forEach {
            println(it)
        }
    }

}
