package cn.tabidachi.electro.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cn.tabidachi.electro.R
import com.microsoft.appcenter.distribute.Distribute
import com.microsoft.appcenter.distribute.ReleaseDetails
import com.microsoft.appcenter.distribute.UpdateAction

@Composable
fun ReleaseDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    releaseDetails: ReleaseDetails?
) {
    if (visible) AlertDialog(
        onDismissRequest = onDismissRequest, confirmButton = {
            TextButton(onClick = {
                Distribute.notifyUpdateAction(UpdateAction.UPDATE)
                onDismissRequest()
            }) {
                Text(text = stringResource(id = R.string.confirm))
            }
        }, dismissButton = {
            TextButton(onClick = {
                Distribute.notifyUpdateAction(UpdateAction.POSTPONE)
                onDismissRequest()
            }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }, title = {
            Text(
                text = stringResource(
                    id = R.string.update_title,
                    releaseDetails?.shortVersion ?: ""
                )
            )
        }, text = {
            Text(text = releaseDetails?.releaseNotes ?: "")
        }
    )
}