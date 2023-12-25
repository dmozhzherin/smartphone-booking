package dym.interview.plugins

import dym.interview.fonoapi.FonoApiClient
import dym.interview.persistence.Datasource
import dym.interview.persistence.dao.BookingDao
import dym.interview.persistence.dao.PhoneDao
import dym.interview.persistence.dao.UserDao
import dym.interview.plugins.Context.bookingDao
import dym.interview.plugins.Context.datasource
import dym.interview.plugins.Context.fonoApiClient
import dym.interview.plugins.Context.phoneDao
import dym.interview.plugins.Context.smartPhoneSpecsService
import dym.interview.plugins.Context.userDao
import dym.interview.service.SmartphoneSpecsService
import io.ktor.server.application.Application


/**
 * Instead of a DI framework since this is a small project.
 * Everything could be objects, but they are difficult to mock.
 * Although MockK can do it.
 * @author dym
 */

object Context {
    lateinit var datasource: Datasource
    lateinit var userDao: UserDao
    lateinit var phoneDao: PhoneDao
    lateinit var bookingDao: BookingDao
    lateinit var smartPhoneSpecsService: SmartphoneSpecsService
    lateinit var fonoApiClient: FonoApiClient
}

fun Application.createSingletons() {
    datasource = Datasource(environment.config)
    userDao = UserDao()
    phoneDao = PhoneDao()
    bookingDao = BookingDao()
    fonoApiClient = FonoApiClient()
    smartPhoneSpecsService = SmartphoneSpecsService()
}

