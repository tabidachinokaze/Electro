package cn.tabidachi.electro.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val CornerDp = 12.dp

private val IncomingShape = RoundedCornerShape(
    topStart = CornerDp,
    topEnd = CornerDp,
    bottomEnd = CornerDp,
    bottomStart = 3.dp
)

private val OutgoingShape = RoundedCornerShape(
    topStart = CornerDp,
    topEnd = CornerDp,
    bottomEnd = 3.dp,
    bottomStart = CornerDp
)

enum class MessageBubbleType {
    Incoming, Outgoing
}

@Composable
fun MessageBubble(
    modifier: Modifier = Modifier,
    cornerSize: Dp = 12.dp,
    border: Dp = 1.dp,
    isError: Boolean = false,
    isIncoming: Boolean,
    content: @Composable () -> Unit,
) {
    val configuration = LocalConfiguration.current
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            shape = BubbleShape(
                CornerSize(cornerSize),
                if (isIncoming) BubbleType.Incoming else BubbleType.Outgoing,
                BubbleSpace
            ),
            border = BorderStroke(
                border,
                if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant
            ),
            color = if (isIncoming) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .align(if (isIncoming) Alignment.Start else Alignment.End)
                .widthIn(max = configuration.screenWidthDp.dp / 8 * 7)
        ) {
            Box(
                modifier = Modifier.padding(vertical = 0.dp, horizontal = BubbleSpace),
                content = {
                    content()
                }
            )
        }
    }
}

private val BubbleSpace = 8.dp

@Preview
@Composable
fun MessageBubble() {
    Column {
        Surface {
            Text(text = "Jetpack Compose", modifier = Modifier)
        }
        MessageBubble(isIncoming = true) {
            Text(text = "Jetpack Compose", modifier = Modifier.padding(8.dp))
        }
        Surface(
            shape = BubbleShape(
                CornerSize(12.dp),
                BubbleType.Incoming,
                8.dp
            )
        ) {
            Box(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                Text(text = "Jetpack Compose")
            }
        }
    }
}