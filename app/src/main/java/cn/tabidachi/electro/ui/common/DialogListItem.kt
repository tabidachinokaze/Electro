package cn.tabidachi.electro.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.ext.timeFormat
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogListItem(
    modifier: Modifier = Modifier,
    image: String?,
    title: String?,
    subtitle: String?,
    date: Long?,
    unread: Int?
) {
    ListItem(
        headlineContent = {
            Row {
                Text(
                    text = title ?: "",
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = date?.timeFormat() ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
        }, supportingContent = {
            Row {
                Text(
                    text = subtitle ?: "",
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
                unread?.let {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(text = it.toString(), maxLines = 1)
                    }
                }
            }
        }, leadingContent = {
            Surface(
                modifier = Modifier
                    .size(48.dp),
                shape = CircleShape,
                tonalElevation = 2.dp,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (image) {
                        null -> {
                            if (!title.isNullOrEmpty()) {
                                Text(text = title[0].toString())
                            }
                        }

                        else -> {
                            AsyncImage(
                                model = image,
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }, modifier = modifier
    )
}