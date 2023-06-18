package cn.tabidachi.electro.ui.sessions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.database.entity.User

@Preview(locale = "zh-rCN", device = "spec:width=1080px,height=1920px,dpi=440")
@Composable
fun SessionsDrawerSheetPreview(
    modifier: Modifier = Modifier,
    onAvatarClick: () -> Unit = {},
    onDrawerItemClick: (DrawerSheetItem) -> Unit = {},
    addAccount: () -> Unit = {},
    switchAccount: () -> Unit = {}
) {
    val viewState = SessionsViewState(user = User(0, "kaze", "kaze@tabidachi.cn", ""))
    var isExpand by remember {
        mutableStateOf(true)
    }
    val users = remember {
        mutableStateListOf<User>().apply {
            add(User(0, "kaze", "kaze@tabidachi.cn", ""))
            add(User(1, "hana", "hana@tabidachi.cn", ""))
//            add(User(2, "yuki", "yuki@tabidachi.cn", ""))
//            add(User(3, "tuki", "tuki@tabidachi.cn", ""))
        }
    }
    ModalDrawerSheet(
        windowInsets = WindowInsets.statusBars.only(WindowInsetsSides.Start),
        drawerShape = RectangleShape,
    ) {
        Column(
            modifier = modifier
        ) {
            UserCardPreview(user = viewState.user, onAvatarClick = onAvatarClick, isExpand = isExpand) {
                isExpand = !isExpand
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
                    .navigationBarsPadding()
            ) {
                AnimatedVisibility(visible = isExpand) {
                    Column {
                        Column {
                            users.forEach {
                                ListItem(
                                    headlineContent = {
                                        Text(text = it.username)
                                    }, leadingContent = {
                                        Image(
                                            painter = painterResource(id = R.drawable.transparent_akkarin),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .size(32.dp)
                                        )
                                    }, modifier = Modifier.clickable {
                                    }
                                )
                            }
                        }
                        ListItem(
                            headlineContent = {
                                Text(text = stringResource(id = R.string.add_account))
                            }, leadingContent = {
                                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                            }, modifier = Modifier.clickable {
                                addAccount()
                            }
                        )
                        Divider()
                    }
                }
                DrawerSheetItem.values().forEach {
                    ListItem(
                        headlineContent = {
                            Text(text = stringResource(id = it.text))
                        }, leadingContent = {
                            Icon(imageVector = it.icon, contentDescription = null)
                        }, modifier = Modifier.clickable {
                            onDrawerItemClick(it)
                        }
                    )
                }
            }
        }
    }
}
