package cn.tabidachi.electro.ui.server

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerScreen(
    state: ServerState,
    actions: ServerActions,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "服务器地址设置")
                }, navigationIcon = {
                    IconButton(
                        onClick = actions.onNavigateUp
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
            Text(
                text = "Electro 服务器",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )
            ListItem(
                headlineContent = {
                    Text(text = "服务器地址")
                }, supportingContent = {
                    Text(text = state.url)
                }, modifier = Modifier.clickable {
                    actions.showDialog(ServerDialogType.ElectroUrl)
                }
            )
            ListItem(
                headlineContent = {
                    Text(text = "端口")
                }, supportingContent = {
                    Text(text = state.port)
                }, modifier = Modifier.clickable {
                    actions.showDialog(ServerDialogType.ElectroPort)
                }
            )
            Text(
                text = "MinIO 服务器",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )
            ListItem(
                headlineContent = {
                    Text(text = "服务器地址")
                }, supportingContent = {
                    Text(text = state.minioUrl)
                }, modifier = Modifier.clickable {
                    actions.showDialog(ServerDialogType.MinioUrl)
                }
            )
            ListItem(
                headlineContent = {
                    Text(text = "端口")
                }, supportingContent = {
                    Text(text = state.minioPort)
                }, modifier = Modifier.clickable {
                    actions.showDialog(ServerDialogType.MinioPort)
                }
            )
        }
    }
    if (state.dialogVisible) AlertDialog(
        onDismissRequest = actions.hideDialog,
        confirmButton = {
            TextButton(
                onClick = actions.onSave
            ) {
                Text(text = "保存")
            }
        }, dismissButton = {
            TextButton(
                onClick = actions.hideDialog
            ) {
                Text(text = "取消")
            }
        }, title = {
            Text(text = state.dialogType.title)
        }, text = {
            OutlinedTextField(
                value = state.dialogValue,
                onValueChange = actions.onDialogValueChange,
                label = {
                    Text(text = state.dialogType.label)
                }
            )
        }
    )
}

enum class ServerDialogType(
    val title: String,
    val label: String,
) {
    ElectroUrl("Electro 服务器地址", "地址"),
    ElectroPort("Electro 服务器端口", "端口"),
    MinioUrl("MinIO 服务器地址", "地址"),
    MinioPort("MinIO 服务器端口", "端口")
}

@Composable
@Preview(name = "Server")
private fun ServerScreenPreview() {
    ServerScreen(
        state = ServerState(
            url = "http://electro.tabidachi.moe",
            port = "23333",
            minioUrl = "http://minio.tabidachi.moe",
            minioPort = "9000",
            dialogVisible = true,
            dialogValue = "http://www.tabidachi.moe"
        ),
        actions = ServerActions()
    )
}

