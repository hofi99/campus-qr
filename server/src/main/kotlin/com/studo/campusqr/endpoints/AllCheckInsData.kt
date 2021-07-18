package com.studo.campusqr.endpoints

import com.moshbit.katerbase.equal
import com.studo.campusqr.common.AllCheckIn
import com.studo.campusqr.common.CheckInsData
import com.studo.campusqr.common.LocationVisitData
import com.studo.campusqr.database.BackendAccess
import com.studo.campusqr.database.BackendLocation
import com.studo.campusqr.database.BackendSeatFilter
import com.studo.campusqr.database.CheckIn
import com.studo.campusqr.extensions.*
import com.studo.campusqr.utils.AuthenticatedApplicationCall
import io.ktor.features.*
import java.util.*

/**
 * This endpoint returns all check-ins.
 */
suspend fun AuthenticatedApplicationCall.listAllCheckIns() {
  if (!user.canViewCheckIns) {
    respondForbidden()
    return
  }

  val checkIns = runOnDb {
    getCollection<CheckIn>()
      .find()
      .sortByDescending(CheckIn::date)
      .toList()
  }

  val locationMap: Map<String, BackendLocation> = checkIns.getLocationMap()

  respondObject(
    checkIns.map { checkIn ->
      AllCheckIn(
        id = checkIn._id,
        locationId = checkIn.locationId,
        locationName = locationMap.getValue(checkIn.locationId).name,
        seat = checkIn.seat,
        checkInDate = checkIn.date.toAustrianTime(yearAtBeginning = false),
        email = checkIn.email
      )
    }
  )
}

suspend fun AuthenticatedApplicationCall.returnCheckInsCsvData() {
  if (!user.canViewCheckIns) {
    respondForbidden()
    return
  }

  val checkIns = runOnDb {
    getCollection<CheckIn>()
      .find()
      .sortByDescending(CheckIn::date)
      .toList()
  }

  val now = Date()

  val locationMap: Map<String, BackendLocation> = checkIns.getLocationMap()

  // TODO: adjust header items of csv to currently selected language
  respondObject(
    CheckInsData(
      csvData = "${CheckIn::email.name};${CheckIn::seat.name};Location;${CheckIn::date.name}\n" + checkIns.joinToString(separator = "\n")
        { "${it.email};${it.seat};${locationMap.getValue(it.locationId).name};${it.date.toAustrianTime()}" },
      csvFileName = "checkIns_${now.toAustrianTimeUnderscore()}.csv"
    )
  )
}

suspend fun AuthenticatedApplicationCall.deleteCheckIn() {
  if (!user.canEditUsers) {
    respondForbidden()
    return
  }

  val checkInId = parameters["id"] ?: throw BadRequestException("No checkInId provided")

  runOnDb {
    getCollection<CheckIn>().deleteOne(CheckIn::_id equal checkInId)
  }

  respondOk()
}

suspend fun getCheckIn(id: String): CheckIn {
  return getCheckInOrNull(id) ?: throw BadRequestException("No check in for id")
}

suspend fun getCheckInOrNull(id: String): CheckIn? {
  return runOnDb { getCollection<CheckIn>().findOne(CheckIn::_id equal id) }
}
