package com.studo.campusqr.endpoints

import com.studo.campusqr.common.AllCheckIn
import com.studo.campusqr.database.BackendLocation
import com.studo.campusqr.database.CheckIn
import com.studo.campusqr.extensions.*
import com.studo.campusqr.utils.AuthenticatedApplicationCall

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