package com.konigsoftware.kontext

import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import com.konigsoftware.kontext.ExampleApiClient.BalanceResponse

internal class KonigKontextImplementationIT {
    @Nested
    inner class KotlinStackIT {
        private val localKotlinApiClient = ExampleApiClient("http://127.0.0.1:8080")

        @Test
        fun `Given single user id and customer id, when calling example api GET balance on kotlin stack, then the correct balance is returned`() =
            runBlocking {
                val userId = "test_user_id_9876"
                val customerId = "test_customer_id_1234"
                val expectedBalance = "3301.42"

                val balanceResponse = localKotlinApiClient.getBalance(userId, customerId)

                assertNotNull(balanceResponse)
                assertEquals(expectedBalance, balanceResponse.balance)
            }

        @Test
        fun `Given two different user ids and customer id, when calling example api GET balance on kotlin stack at the same time, then the correct balances are returned`() =
            runBlocking {
                val userId1 = "test_user_id_9876"
                val customerId1 = "test_customer_id_1234"
                val expectedBalance1 = "3301.42"

                val userId2 = "test_user_id_0000"
                val customerId2 = "test_customer_id_0909"
                val expectedBalance2 = "19.87"

                lateinit var balanceResponse1: BalanceResponse
                lateinit var balanceResponse2: BalanceResponse

                val request1 = launch {
                    balanceResponse1 = localKotlinApiClient.getBalance(userId1, customerId1) ?: fail("Request 1 failed")
                }
                val request2 = launch {
                    balanceResponse2 = localKotlinApiClient.getBalance(userId2, customerId2) ?: fail("Request 2 failed")
                }

                request1.join()
                request2.join()

                assertNotNull(balanceResponse1)
                assertEquals(expectedBalance1, balanceResponse1.balance)

                assertNotNull(balanceResponse2)
                assertEquals(expectedBalance2, balanceResponse2.balance)
            }

        @Test
        fun `Given different user ids and customer ids, when calling example api GET balance on kotlin stack many times concurrently at the same time, then the correct balances are returned`() =
            runBlocking {
                val customerAndUserToExpectedBalance = linkedMapOf(
                    ("test_customer_id_1234" to "test_user_id_9876") to "3301.42",
                    ("test_customer_id_0909" to "test_user_id_0000") to "19.87",
                    ("test_customer_id_9281" to "test_user_id_0091") to "8310.921",
                    ("test_customer_id_9032" to "test_user_id_3802") to "889.223",
                    ("test_customer_id_1123" to "test_user_id_8731") to "11.19",
                    ("test_customer_id_8013" to "test_user_id_3385") to "71239.91",
                    ("test_customer_id_1859" to "test_user_id_8358") to "89134.12",
                    ("test_customer_id_8302" to "test_user_id_9090") to "83.1",
                    ("test_customer_id_0909" to "test_user_id_0091") to "1.91",
                    ("test_customer_id_1234" to "test_user_id_8921") to "8310.921",
                )

                val requestIndexToExpectedBalanceAndActualBalance = LinkedHashMap<Int, Pair<String, String>>(1000)
                val requestJobs = mutableListOf<Job>()

                for (i in 0..1000) {
                    val randomBalanceRequest =
                        customerAndUserToExpectedBalance.entries.toList()[Random.nextInt(
                            customerAndUserToExpectedBalance.size
                        )]
                    val customerId = randomBalanceRequest.key.first
                    val userId = randomBalanceRequest.key.second
                    val expectedBalance = randomBalanceRequest.value

                    val request = launch {
                        val balanceResponse = localKotlinApiClient.getBalance(userId, customerId)

                        assertNotNull(
                            balanceResponse,
                            "Getting balance for user: $userId and customer: $customerId failed"
                        )

                        requestIndexToExpectedBalanceAndActualBalance[i] = expectedBalance to balanceResponse.balance
                    }

                    requestJobs.add(request)
                }

                requestJobs.joinAll()

                requestIndexToExpectedBalanceAndActualBalance.values.forEach {
                    assertEquals(it.first, it.second, "Expected balance: ${it.first} but got balance: ${it.second}")
                }
            }
    }

    @Nested
    inner class JavaStackIT {
        private val localJavaApiClient = ExampleApiClient("http://localhost:8081")

        @Test
        fun `Given single user id and customer id, when calling example api GET balance on java stack, then the correct balance is returned`() =
            runBlocking {
                val userId = "test_user_id_9876"
                val customerId = "test_customer_id_1234"
                val expectedBalance = "3301.42"

                val balanceResponse = localJavaApiClient.getBalance(userId, customerId)

                assertNotNull(balanceResponse)
                assertEquals(expectedBalance, balanceResponse.balance)
            }

        @Test
        fun `Given two different user ids and customer id, when calling example api GET balance on java stack at the same time, then the correct balances are returned`() =
            runBlocking {
                val userId1 = "test_user_id_9876"
                val customerId1 = "test_customer_id_1234"
                val expectedBalance1 = "3301.42"

                val userId2 = "test_user_id_0000"
                val customerId2 = "test_customer_id_0909"
                val expectedBalance2 = "19.87"

                lateinit var balanceResponse1: BalanceResponse
                lateinit var balanceResponse2: BalanceResponse

                val request1 = launch {
                    balanceResponse1 = localJavaApiClient.getBalance(userId1, customerId1) ?: fail("Request 1 failed")
                }
                val request2 = launch {
                    balanceResponse2 = localJavaApiClient.getBalance(userId2, customerId2) ?: fail("Request 2 failed")
                }

                request1.join()
                request2.join()

                assertNotNull(balanceResponse1)
                assertEquals(expectedBalance1, balanceResponse1.balance)

                assertNotNull(balanceResponse2)
                assertEquals(expectedBalance2, balanceResponse2.balance)
            }

        @Test
        fun `Given different user ids and customer ids, when calling example api GET balance on java stack many times concurrently at the same time, then the correct balances are returned`() =
            runBlocking {
                val customerAndUserToExpectedBalance = linkedMapOf(
                    ("test_customer_id_1234" to "test_user_id_9876") to "3301.42",
                    ("test_customer_id_0909" to "test_user_id_0000") to "19.87",
                    ("test_customer_id_9281" to "test_user_id_0091") to "8310.921",
                    ("test_customer_id_9032" to "test_user_id_3802") to "889.223",
                    ("test_customer_id_1123" to "test_user_id_8731") to "11.19",
                    ("test_customer_id_8013" to "test_user_id_3385") to "71239.91",
                    ("test_customer_id_1859" to "test_user_id_8358") to "89134.12",
                    ("test_customer_id_8302" to "test_user_id_9090") to "83.1",
                    ("test_customer_id_0909" to "test_user_id_0091") to "1.91",
                    ("test_customer_id_1234" to "test_user_id_8921") to "8310.921",
                )

                val requestIndexToExpectedBalanceAndActualBalance = LinkedHashMap<Int, Pair<String, String>>(1000)
                val requestJobs = mutableListOf<Job>()

                for (i in 0..1000) {
                    val randomBalanceRequest =
                        customerAndUserToExpectedBalance.entries.toList()[Random.nextInt(
                            customerAndUserToExpectedBalance.size
                        )]
                    val customerId = randomBalanceRequest.key.first
                    val userId = randomBalanceRequest.key.second
                    val expectedBalance = randomBalanceRequest.value

                    val request = launch {
                        val balanceResponse = localJavaApiClient.getBalance(userId, customerId)

                        assertNotNull(
                            balanceResponse,
                            "Getting balance for user: $userId and customer: $customerId failed"
                        )

                        requestIndexToExpectedBalanceAndActualBalance[i] = expectedBalance to balanceResponse.balance
                    }

                    requestJobs.add(request)
                }

                requestJobs.joinAll()

                requestIndexToExpectedBalanceAndActualBalance.values.forEach {
                    assertEquals(it.first, it.second, "Expected balance: ${it.first} but got balance: ${it.second}")
                }
            }
    }
}
