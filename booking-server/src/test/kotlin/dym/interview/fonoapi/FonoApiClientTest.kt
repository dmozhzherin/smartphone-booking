package dym.interview.fonoapi

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * @author dym
 */
class FonoApiClientTest {

    val apiClient = FonoApiClient()

    @Test
    fun testFailGetSmartphoneSpecs() {
        runTest {
            var successCounter = 0
            var failureCounter = 0
            for (i in 1..30) {
                runCatching {
                    // this will fail or return null (there is no smartphone with this name)
                    apiClient.getSmartphoneSpecs("Samsung", "Galaxy S20")
                }.onFailure {
                    assertEquals("FonoAPI is down", it.message)
                    failureCounter++
                }.onSuccess {
                    assertNull(it)
                    successCounter++
                }
            }

            assertTrue(successCounter > FonoApiClient.SUCCESS_THRESHOLD)
            assertTrue(failureCounter >= FonoApiClient.FAILURE_THRESHOLD)
        }
    }

    @Test
    fun testGetSmartphoneSpecs() {
        runTest {
            var successCounter = 0
            var failureCounter = 0
            for (i in 1..30) {
                runCatching {
                    // this will succeed or not (because of random failure)
                    apiClient.getSmartphoneSpecs("Samsung", "Galaxy S8")
                }.onFailure {
                    assertEquals("FonoAPI is down", it.message)
                    failureCounter++
                }.onSuccess {
                    assertNotNull(it)
                    SmartphoneSpecs.getSmartphoneSpecs("Samsung", "Galaxy S8")?.let { specs ->
                        assertEquals(specs, it)
                    }
                    successCounter++
                }
            }

            assertTrue(successCounter > FonoApiClient.SUCCESS_THRESHOLD)
            assertTrue(failureCounter >= FonoApiClient.FAILURE_THRESHOLD)
        }
    }
}