package dym.interview.service

import dym.interview.fonoapi.FonoApiClient
import dym.interview.persistence.dao.PhoneDao
import dym.interview.persistence.dto.Smartphone
import dym.interview.plugins.Context
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import java.util.UUID

/**
 * @author dym
 */
class SmartphoneSpecsService(
    private val phoneDao: PhoneDao = Context.phoneDao,
    private val fonoApiClient: FonoApiClient = Context.fonoApiClient
) {

    private val circuitBreaker = CircuitBreaker.ofDefaults("smartphoneSpecsCircuitBreaker")

    suspend fun getUpdatedSmartphone(guid: UUID): Smartphone? =
        phoneDao.getSmartphone(guid)?.let { smartphone ->
            try {
                circuitBreaker.executeSuspendFunction() {
                    fonoApiClient.getSmartphoneSpecs(smartphone.manufacturer, smartphone.model)
                }?.let { specs ->
                    smartphone.copy(
                        technology = specs.technology,
                        bands2g = specs.bands2g,
                        bands3g = specs.bands3g,
                        bands4g = specs.bands4g
                    ).let { updatedSmartphone ->
                        // update smartphone in database, if successful return updated smartphone, otherwise return what's in database
                        phoneDao.updateSmartphone(updatedSmartphone) ?: phoneDao.getSmartphone(guid)
                    }
                } ?: smartphone
            } catch (e: Exception) {
                smartphone
            }
        }
}