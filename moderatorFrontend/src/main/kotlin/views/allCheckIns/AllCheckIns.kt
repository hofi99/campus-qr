package views.allCheckIns

import com.studo.campusqr.common.*
import kotlinext.js.js
import kotlinx.browser.window
import react.*
import react.dom.div
import util.*
import views.common.*
import webcore.*
import webcore.extensions.launch
import webcore.materialUI.*
import kotlin.js.Date

interface AllCheckInsProps : RProps {
  var classes: AllCheckInsClasses
  var userData: UserData
}

interface AllCheckInsState : RState {
  var locationFetchInProgress: Boolean
  var locationNameToLocationMap: Map<String, ClientLocation>
  var checkIns: List<AllCheckIn?>
  var showProgress: Boolean
  var snackbarText: String
  var selectedLocation: ClientLocation?
}

class AllCheckIns : RComponent<AllCheckInsProps, AllCheckInsState>() {

  override fun AllCheckInsState.init() {
    locationFetchInProgress = false
    locationNameToLocationMap = emptyMap()
    checkIns = emptyList()
    showProgress = false
    snackbarText = ""
    selectedLocation = null
  }

  override fun componentDidMount() {
    getAllCheckIns()
    fetchLocations()
  }

  private fun handleDeleteCheckInResponse(response: String?, successText: String) {
    setState {
      snackbarText = when (response) {
        "ok" -> {
          getAllCheckIns()
          successText
        }
        else -> Strings.error_try_again.get()
      }
    }
  }

  private fun fetchLocations() = launch {
    setState {
      locationFetchInProgress = true
    }
    val response = NetworkManager.get<Array<ClientLocation>>("$apiBase/location/list")
    setState {
      if (response != null) {
        locationNameToLocationMap = response.associateBy { it.name }
      }
      locationFetchInProgress = false
    }
  }

  private fun RBuilder.renderSnackbar() = mbSnackbar(
    MbSnackbarProps.Config(
      show = state.snackbarText.isNotEmpty(),
      message = state.snackbarText,
      onClose = {
        setState {
          snackbarText = ""
        }
      })
  )

  private fun getAllCheckIns() = launch {
    setState { showProgress = true }
    val response = (NetworkManager.get<Array<AllCheckIn>>("$apiBase/allCheckIns"))?.toList()
    setState {
      if (response == null) {
        snackbarText = Strings.all_checkins_error.get()
        checkIns = emptyList()
      } else {
        checkIns = response
      }
      showProgress = false
    }
  }

  override fun RBuilder.render() {
    renderSnackbar()
    typography {
      attrs.variant = "h5"
      attrs.className = props.classes.content
      +Strings.all_check_ins.get()
    }

    val checkIns = state.checkIns

    when {
      checkIns.isNotEmpty() && state.locationNameToLocationMap.isNotEmpty() -> {
        renderLinearProgress(state.showProgress)
        div(props.classes.content) {
          renderToolbarView(
            ToolbarViewProps.Config(
              title = Strings.locations.get(),
              buttons = listOfNotNull(
                ToolbarViewProps.ToolbarButton(
                  text = Strings.checkin_element_download_csv.get(), // canViewCheckIns check would be redundant
                  variant = "outlined",
                  onClick = {
                    launch {
                      setState {
                        showProgress = true
                      }
                      val checkInsData =
                        NetworkManager.get<CheckInsData>("$apiBase/allCheckIns/checkInsCsv")
                          ?: return@launch
                      fileDownload(data = checkInsData.csvData, fileName = checkInsData.csvFileName)
                      setState {
                        showProgress = false
                      }
                    }
                  }
                ),
              )
            )
          )

          muiAutocomplete {
            attrs.value = state.selectedLocation?.name ?: "" // TODO: "" is not a valid option
            attrs.onChange = { _, target: String?, _ ->
              setState {
                selectedLocation = target?.let { locationNameToLocationMap[it] }
              }
            }
            attrs.openOnFocus = true
            attrs.options = state.locationNameToLocationMap.keys.toTypedArray()
            attrs.getOptionLabel = { it }
            attrs.renderInput = { params: dynamic ->
              textField {
                attrs.id = params.id
                attrs.InputProps = params.InputProps
                attrs.inputProps = params.inputProps
                attrs.fullWidth = params.fullWidth
                attrs.fullWidth = true
                attrs.variant = "outlined"
                attrs.label = Strings.location_name.get()
              }
            }
          }

          spacer(5)

          mTable {
            mTableHead {
              mTableRow {
                mTableCell { +Strings.all_checkins_checkin_email.get() }
                mTableCell { +Strings.all_checkins_checkin_date.get() }
                if (props.userData.clientUser?.canEditUsers == true) {
                  mTableCell { +Strings.actions.get() }
                }
              }
            }
            mTableBody {
              if (state.selectedLocation == null || state.selectedLocation!!.name.equals("")) {
                checkIns.forEach { checkIn ->
                  if (checkIn != null) {
                    renderAllCheckInsTableRow(
                      AllCheckInsTableRowProps.Config(
                        checkIn = checkIn,
                        onDeleteFinished = { response ->
                          handleDeleteCheckInResponse(response, Strings.checkin_deleted.get())
                        },
                        userData = props.userData,
                      )
                    )
                  }
                }
              } else {
                checkIns.filter{ it?.locationName.equals(state.selectedLocation?.name) }.forEach { checkIn ->
                  if (checkIn != null) {
                    renderAllCheckInsTableRow(
                      AllCheckInsTableRowProps.Config(
                        checkIn = checkIn,
                        onDeleteFinished = { response ->
                          handleDeleteCheckInResponse(response, Strings.checkin_deleted.get())
                        },
                        userData = props.userData,
                      )
                    )
                  }
                }
              }
            }
          }
          // TODO: add table pagination
          /*
          mTablePagination {
            attrs.rowsPerPage = 10
          }*/
        }
      }
      state.showProgress || state.locationFetchInProgress -> centeredProgress()
      checkIns.isEmpty() -> {
        div {
          alert {
            attrs.severity = "info"
            alertTitle {
              +Strings.alert_title_info.get()
            }
            +Strings.all_checkins_none_checkedin.get()
          }
        }
      }
      state.locationNameToLocationMap.isEmpty() -> {
        div {
          alert {
            attrs.severity = "info"
            alertTitle {
              +Strings.alert_title_info.get()
            }
            +Strings.all_checkins_no_locations.get()
          }
        }
      }
    }
  }
}

interface AllCheckInsClasses {
  var content: String
}

private val style = { _: dynamic ->
  js {
    content = js {
      margin = 16
    }
  }
}

private val styled = withStyles<AllCheckInsProps, AllCheckIns>(style)

fun RBuilder.renderAllCheckIns(userData: UserData) = styled {
  // Set component attrs here
  attrs.userData = userData
}
