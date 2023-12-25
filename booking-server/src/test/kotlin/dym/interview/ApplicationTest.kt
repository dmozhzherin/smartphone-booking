package dym.interview

import dym.interview.persistence.dto.Booking
import dym.interview.persistence.dto.NewBooking
import dym.interview.persistence.dto.Smartphone
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import io.ktor.util.encodeBase64
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApplicationTest : DatabaseTest() {
    @Test
    fun endToEndTestBookings() = testApplication {

        // GET /bookings - no bookings yet
        client.get("/bookings").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(0, Json.decodeFromString<List<Booking>>(bodyAsText()).size)
        }

        // GET /phones
        val phones = client.get("/phones").run {
            assertEquals(HttpStatusCode.OK, status)
            Json.decodeFromString<List<Smartphone>>(bodyAsText()).let { phones ->
                assertEquals(10, phones.size)
                phones.forEach { phone ->
                    assertNotNull(phone.guid)
                    assertNotNull(phone.manufacturer)
                    assertNotNull(phone.model)
                }
                phones
            }
        }

        // GET /phones/{guid}
        val smartphone = phones.first()
        client.get("/phones/${smartphone.guid}").apply {
            assertEquals(HttpStatusCode.OK, status)
            Json.decodeFromString<Smartphone>(bodyAsText()).let { phone ->
                assertEquals(smartphone.guid, phone.guid)
                assertEquals(smartphone.manufacturer, phone.manufacturer)
                assertEquals(smartphone.model, phone.model)
                assertEquals(smartphone.available, phone.available)
            }
        }

        // Try to create a new booking without authorization
        client.post("/bookings") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(NewBooking(smartphone.guid, "")))
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }

        // Create a new booking
        val newBooking = client.post("/bookings") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(NewBooking(smartphone.guid, "")))
            headers.append("Authorization", "Basic " + "test:test".encodeBase64())
        }.run {
            assertEquals(HttpStatusCode.Created, status)
            Json.decodeFromString<Booking>(bodyAsText()).also { booking ->
                assertNotNull(booking.guid)
                assertEquals(smartphone.manufacturer, booking.manufacturer)
                assertEquals(smartphone.model, booking.model)
                assertEquals("test", booking.username)
                assertEquals("Test User", booking.name)
                assertNotNull(booking.bookedAt)
            }
        }

        // Try to create a new booking for the same phone
        client.post("/bookings") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(NewBooking(smartphone.guid, "")))
            headers.append("Authorization", "Basic " + "test:test".encodeBase64())
        }.run {
            assertEquals(HttpStatusCode.Conflict, status)
        }

        // GET existing /bookings/{guid}
        client.get("/bookings/${newBooking.guid}").apply {
            assertEquals(HttpStatusCode.OK, status)
            Json.decodeFromString<Booking>(bodyAsText()).let { booking ->
                assertEquals(newBooking.guid, booking.guid)
                assertEquals(newBooking.manufacturer, booking.manufacturer)
                assertEquals(newBooking.model, booking.model)
                assertEquals(newBooking.username, booking.username)
                assertEquals(newBooking.name, booking.name)
            }
        }

        // GET non-existing /bookings/{guid}
        client.get("/bookings/non-existing-booking").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }

        // Try to delete a booking without authorization
        client.delete("/bookings/${newBooking.guid}").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }

        // Try to delete a booking with wrong authorization
        client.delete("/bookings/${newBooking.guid}") {
            headers.append("Authorization", "Basic " + "user:password".encodeBase64())
        }.apply {
            assertEquals(HttpStatusCode.Forbidden, status)
        }

        // Delete the booking
        client.delete("/bookings/${newBooking.guid}") {
            headers.append("Authorization", "Basic " + "test:test".encodeBase64())
        }.apply {
            assertEquals(HttpStatusCode.NoContent, status)
        }

        // Was it really deleted?
        client.get("/bookings/${newBooking.guid}").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

}
