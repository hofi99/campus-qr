package views.allCheckIns

import app.routeContext
import com.studo.campusqr.common.*
import kotlinext.js.js
import kotlinx.browser.window
import react.*
import util.Strings
import util.apiBase
import util.fileDownload
import util.get
import views.common.spacer
import webcore.MenuItem
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialMenu
import webcore.materialUI.*

interface AllCheckInsTableRowProps : RProps {
  class Config(
    val checkIn: AllCheckIn?,
    val onDeleteFinished: (response: String?) -> Unit,
    val userData: UserData,
  ) {
    val clientUser: ClientUser get() = userData.clientUser!!
  }

  var config: Config
  var classes: AllCheckInsTableRowClasses
}

interface AllCheckInsTableRowState : RState {
  var showProgress: Boolean
}

class AllCheckInsTableRow(props: AllCheckInsTableRowProps) : RComponent<AllCheckInsTableRowProps, AllCheckInsTableRowState>(props) {

  override fun AllCheckInsTableRowState.init(props: AllCheckInsTableRowProps) {
    showProgress = false
  }

  override fun componentWillReceiveProps(nextProps: AllCheckInsTableRowProps) {
    setState { init(nextProps) }
  }

  override fun RBuilder.render() {
    mTableRow {
      mTableCell {
        +props.config.checkIn?.email!!
      }
      mTableCell {
        +props.config.checkIn?.checkInDate!!
      }
      if (props.config.clientUser.canEditUsers == true) {
        mTableCell {
          routeContext.Consumer { routeContext ->
            if (state.showProgress) {
              circularProgress {}
            } else {
              materialMenu(
                menuItems = listOfNotNull(
                  MenuItem(text = Strings.checkin_delete.get(), icon = deleteIcon, onClick = {
                    if (window.confirm(Strings.checkin_delete_are_you_sure.get())) {
                      launch {
                        val response = NetworkManager.post<String>(
                          "$apiBase/allCheckIns/${props.config.checkIn?.id}/delete",
                          params = null
                        )
                        props.config.onDeleteFinished(response)
                      }
                    }
                  }),
                )
              )
            }
          }

        }
      }
    }
  }
}

interface AllCheckInsTableRowClasses {}

private val style = { _: dynamic ->
  js {}
}

private val styled = withStyles<AllCheckInsTableRowProps, AllCheckInsTableRow>(style)

fun RBuilder.renderAllCheckInsTableRow(config: AllCheckInsTableRowProps.Config) = styled {
  attrs.config = config
}
