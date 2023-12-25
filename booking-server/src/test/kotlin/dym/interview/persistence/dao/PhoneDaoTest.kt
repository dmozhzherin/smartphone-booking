package dym.interview.persistence.dao

import dym.interview.DatabaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * @author dym
 */
class PhoneDaoTest : DatabaseTest() {

    private val phoneDao: PhoneDao by lazy { PhoneDao() }

    @Test
    fun testGetSmartphones() {
        phoneDao.getSmartphones().let {
            assertTrue(it.isNotEmpty())

            it.first().let { smartphone ->
                assertNotNull(smartphone.guid)
                assertNotNull(smartphone.manufacturer)
                assertNotNull(smartphone.model)
                assertTrue(smartphone.available)

                //not included in test data
                assertNull(smartphone.technology)
                assertNull(smartphone.bands2g)
                assertNull(smartphone.bands3g)
                assertNull(smartphone.bands4g)

                //not booked
                assertNull(smartphone.bookedBy)
                assertNull(smartphone.bookedByName)
                assertNull(smartphone.bookedAt)
            }
        }
    }

    @Test
    fun testUpdateSmartphone() {
        phoneDao.getSmartphones().first().let { smartphone ->
            val newSmartphone = smartphone.copy(
                technology = "GSM / HSPA / LTE",
                bands2g = "GSM 850 / 900 / 1800 / 1900 - SIM 1 & SIM 2 (dual-SIM model only)",
                bands3g = "HSDPA 850 / 900 / 1700(AWS) / 1900 / 2100",
                bands4g = "LTE band 1(2100), 2(1900), 3(1800), 4(1700/2100), 5(850), 7(2600), 8(900), 12(700), 13(700), 17(700), 18(800), 19(800), 20(800), 25(1900), 26(850), 28(700), 32(1500), 38(2600), 39(1900), 40(2300), 41(2500), 66(1700/2100)",
            )

            phoneDao.updateSmartphone(newSmartphone)?.let { updatedSmartphone ->
                assertEquals(newSmartphone.manufacturer, updatedSmartphone.manufacturer)
                assertEquals(newSmartphone.model, updatedSmartphone.model)
                assertEquals(newSmartphone.technology, updatedSmartphone.technology)
                assertEquals(newSmartphone.bands2g, updatedSmartphone.bands2g)
                assertEquals(newSmartphone.bands3g, updatedSmartphone.bands3g)
                assertEquals(newSmartphone.bands4g, updatedSmartphone.bands4g)
            }?: fail("Smartphone was not updated")
        }
    }

    @Test
    fun failUpdateIfVersionMismatches() {
        phoneDao.getSmartphones().first().let { smartphone ->
            val newSmartphone = smartphone.copy(
                technology = "GSM / HSPA / LTE",
                bands2g = "GSM 850 / 900 / 1800 / 1900 - SIM 1 & SIM 2 (dual-SIM model only)",
                bands3g = "HSDPA 850 / 900 / 1700(AWS) / 1900 / 2100",
            )

            phoneDao.updateSmartphone(newSmartphone)?.let { updatedSmartphone ->
                assertEquals(newSmartphone.manufacturer, updatedSmartphone.manufacturer)
                assertEquals(newSmartphone.model, updatedSmartphone.model)
                assertEquals(newSmartphone.technology, updatedSmartphone.technology)
                assertEquals(newSmartphone.bands2g, updatedSmartphone.bands2g)
                assertEquals(newSmartphone.bands3g, updatedSmartphone.bands3g)
                assertEquals(newSmartphone.bands4g, updatedSmartphone.bands4g)
            }?: fail("Smartphone was not updated")


            //it retains the old version
            val newSmartphone2 = newSmartphone.copy(
                technology = "5G",
                bands2g = "GSM 850",
                bands3g = "HSDPA 850",
            )

            assertNull(phoneDao.updateSmartphone(newSmartphone2))
        }
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun init() {
            initDatasource()
        }
    }
}