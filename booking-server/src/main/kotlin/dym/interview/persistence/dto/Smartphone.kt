package dym.interview.persistence.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * A data class representing a smartphone in the booking system.
 *
 * @author dym
 */
@Serializable
data class Smartphone(
    @Transient
    val id: Long = 0,
    val guid: String,
    val manufacturer: String,
    val model: String,
    val technology: String?,
    val bands2g: String?,
    val bands3g: String?,
    val bands4g: String?,
    @Transient
    val version: Int = 0,
    val available: Boolean,
    @SerialName("booked_by")
    val bookedBy: String?,
    @SerialName("booked_by_name")
    val bookedByName: String?,
    @SerialName("booked_at")
    val bookedAt: String?,
)
