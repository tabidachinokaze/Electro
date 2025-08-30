package cn.tabidachi.electro.ui.imagepreview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreviewScreen(
    state: ImagePreviewState = ImagePreviewState(),
    actions: ImagePreviewActions = ImagePreviewActions()
) {
    Box(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(text = state.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }, navigationIcon = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = null
                    )
                }
            }, actions = {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null)
                    DropdownMenu(expanded = false, onDismissRequest = { /*TODO*/ }) {
                        ImagePreviewMenu.values().forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = it.text))
                                }, leadingIcon = {
                                    Icon(imageVector = it.icon, contentDescription = null)
                                }, onClick = {
                                    when (it) {
                                        ImagePreviewMenu.SAVE -> {

                                        }
                                        ImagePreviewMenu.SHARE -> {

                                        }
                                        ImagePreviewMenu.COPY -> {

                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        )
        AsyncImage(model = "", contentDescription = null)
    }
}

@Composable
@Preview(name = "ImagePreview")
private fun ImagePreviewScreenPreview() {
    ImagePreviewScreen()
}

