package dym.interview.service

import dym.interview.fonoapi.FonoApiClient
import dym.interview.fonoapi.SmartphoneSpecs
import dym.interview.persistence.dao.PhoneDao
import dym.interview.persistence.dto.Smartphone
import dym.interview.plugins.Context
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * @author dym
 */
@ExtendWith(MockKExtension::class)
class SmartphoneSpecsServiceTest {

    @MockK
    lateinit var phoneDaoMock: PhoneDao

    @MockK
    lateinit var fonoApiClientMock: FonoApiClient

    @BeforeEach
    fun setUp() {
        val uuid = slot<UUID>()
        every { phoneDaoMock.getSmartphone(capture(uuid)) }.answers {
            SmartphoneSpecs.getSmartphoneSpecs("Samsung", "Galaxy S8")?.let {
                Smartphone(
                    id = 1,
                    guid = uuid.captured.toString(),
                    manufacturer = it.manufacturer,
                    model = it.model,
                    technology = null,
                    bands2g = null,
                    bands3g = null,
                    bands4g = null,
                    version = 1,
                    available = true,
                    bookedBy = null,
                    bookedByName = null,
                    bookedAt = null
                )
            }
        }
        Context.phoneDao = phoneDaoMock
    }

    @Test
    fun testWithFakeApiUp() {
        val manufacturer = slot<String>()
        val model = slot<String>()
        coEvery { fonoApiClientMock.getSmartphoneSpecs(capture(manufacturer), capture(model)) } coAnswers {
            SmartphoneSpecs.getSmartphoneSpecs(manufacturer.captured, model.captured)
        }
        Context.fonoApiClient = fonoApiClientMock

        val updatedSmartphone = slot<Smartphone>()
        every { Context.phoneDao.updateSmartphone(capture(updatedSmartphone)) } answers { updatedSmartphone.captured }

        runTest {
            SmartphoneSpecsService().getUpdatedSmartphone(UUID.randomUUID())?.let { updatedSmartphone ->
                SmartphoneSpecs.getSmartphoneSpecs("Samsung", "Galaxy S8")?.let {
                    assertEquals(it.technology, updatedSmartphone.technology)
                    assertEquals(it.bands2g, updatedSmartphone.bands2g)
                    assertEquals(it.bands3g, updatedSmartphone.bands3g)
                    assertEquals(it.bands4g, updatedSmartphone.bands4g)
                } ?: fail("This is odd")
            } ?: fail("Smartphone not found, not updated, all is lost")
        }
    }

    @Test
    fun testWithFakeApiDown() {
        coEvery { fonoApiClientMock.getSmartphoneSpecs(any(), any()) } coAnswers { throw Exception("Fake API is down") }
        Context.fonoApiClient = fonoApiClientMock

        runTest {
            SmartphoneSpecsService().getUpdatedSmartphone(UUID.randomUUID())?.let { updatedSmartphone ->
                assertNull(updatedSmartphone.technology)
                assertNull(updatedSmartphone.bands2g)
                assertNull(updatedSmartphone.bands3g)
                assertNull(updatedSmartphone.bands4g)
            } ?: fail("Smartphone not found, not updated, all is lost")
        }

        verify(exactly = 0) { phoneDaoMock.updateSmartphone(any()) }
    }

}