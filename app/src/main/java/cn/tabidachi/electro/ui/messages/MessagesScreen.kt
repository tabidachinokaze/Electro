package cn.tabidachi.electro.ui.messages

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MessagesScreen(
    state: MessagesState = MessagesState(),
    actions: MessagesActions = MessagesActions()
) {

}

@Composable
@Preview(name = "Messages")
private fun MessagesScreenPreview() {
    MessagesScreen()
}

