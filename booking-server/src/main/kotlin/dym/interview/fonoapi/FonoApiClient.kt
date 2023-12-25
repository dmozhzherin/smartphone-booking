package dym.interview.fonoapi

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * It's fake. The API is permanently down. There are other APIs that can be used instead, but they are paid.
 * @author dym
 */

class FonoApiClient(val successThreshold: Int = SUCCESS_THRESHOLD, val failureThreshold: Int = FAILURE_THRESHOLD) {

    private var successCounter = AtomicInteger(0)
    private var failureCounter = AtomicInteger(0)
    suspend fun getSmartphoneSpecs(manufacturer: String, model: String): SmartphoneSpecs? {
        //random delay between 0 and 30 seconds
        delay(Random.nextLong(0, 30_000))

        if (successCounter.get() > successThreshold && failureCounter.get() <= failureThreshold) {
            failureCounter.incrementAndGet()
            throw Exception("FonoAPI is down")
        } else if (successCounter.get() > successThreshold && failureCounter.get() > failureThreshold) {
            successCounter.set(0)
            failureCounter.set(0)
            // randomly throw an exception just for fun
            if (Random.nextBoolean()) {
                throw Exception("FonoAPI is down")
            }
        } else {
            successCounter.incrementAndGet()
        }
        return SmartphoneSpecs.getSmartphoneSpecs(manufacturer, model)
    }

    companion object {
        const val SUCCESS_THRESHOLD = 20
        const val FAILURE_THRESHOLD = 8
    }

}