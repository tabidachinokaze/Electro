package cn.tabidachi.electro.ui.server

data class ServerState(
    val url: String = "",
    val port: String = "",
    val minioUrl: String = "",
    val minioPort: String = "",
    val dialogVisible: Boolean = false,
    val dialogType: ServerDialogType = ServerDialogType.ElectroUrl,
    val dialogValue: String = ""
)

data class ServerActions(
    val onClick: () -> Unit = {},
    val showDialog: (ServerDialogType) -> Unit = {},
    val hideDialog: () -> Unit = {},
    val onSave: () -> Unit = {},
    val onDialogValueChange: (String) -> Unit = {},
    val onNavigateUp: ()  -> Unit = {}
)