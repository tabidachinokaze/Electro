package cn.tabidachi.electro.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.core.view.ViewCompat
import cn.tabidachi.electro.R
import io.ktor.http.ContentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageTextField(
    modifier: Modifier = Modifier,
    text: String,
    onTextValueChange: (String) -> Unit,
    onAttachment: () -> Unit = {},
    onSend: () -> Unit = {},
) {
    val view = LocalView.current
    LaunchedEffect(key1 = view, block = {
        ViewCompat.setOnReceiveContentListener(view, arrayOf(ContentType.Image.Any.toString())) { view, payload ->
            println(payload)
            payload
        }
    })
    BasicTextField(
        value = text,
        onValueChange = onTextValueChange,
        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
        maxLines = 6,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
            onSend = {
                onSend()
            }
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = {
            TextFieldDefaults.DecorationBox(
                value = text,
                innerTextField = it,
                enabled = true,
                singleLine = false,
                visualTransformation = VisualTransformation.None,
                interactionSource = remember {
                    MutableInteractionSource()
                },
//                leadingIcon = {
//                    IconButton(onClick = onAttachment) {
//                        Icon(
//                            imageVector = Icons.Rounded.Attachment,
//                            contentDescription = null
//                        )
//                    }
//                },
//                trailingIcon = {
//                    IconButton(onClick = onSend) {
//                        Icon(imageVector = Icons.Rounded.Send, contentDescription = null)
//                    }
//                },
                placeholder = {
                    Text(text = stringResource(id = R.string.message))
                },
                container = {
                    /*
                    Box(
                        modifier = Modifier.background(
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.2f
                            ), shape = RoundedCornerShape(28.dp)
                        )
                    )
                    */
                },
            )
        },
        modifier = modifier.fillMaxWidth()
    )
}