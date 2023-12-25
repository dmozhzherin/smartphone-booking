package dym.interview.persistence.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author dym
 */
@Serializable
data class Booking(
    val guid: String,
    @SerialName("phone_guid")
    val phoneGuid: String,
    val manufacturer: String,
    val model: String,
    @SerialName("booked_by")
    val username: String,
    @SerialName("booked_by_name")
    val name: String,
    @SerialName("booked_at")
    val bookedAt: String?,
)
