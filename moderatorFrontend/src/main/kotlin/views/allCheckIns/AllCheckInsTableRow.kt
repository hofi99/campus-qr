package views.allCheckIns

import kotlinext.js.js
import react.*
import util.Strings
import util.get
import views.common.spacer
import webcore.materialUI.*

interface AllCheckInsTableRowProps : RProps {
  class Config(
    val email: String,
    val checkInDate: String,
  )

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
        +props.config.email
      }
      mTableCell {
        +props.config.checkInDate
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
