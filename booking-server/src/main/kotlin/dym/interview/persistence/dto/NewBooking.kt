package dym.interview.persistence.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author dym
 */
@Serializable
data class NewBooking(
    @SerialName("phone_guid")
    val phoneGuid: String,
    val username: String,
)
