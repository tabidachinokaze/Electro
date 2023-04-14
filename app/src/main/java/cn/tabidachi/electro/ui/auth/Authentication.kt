package cn.tabidachi.electro.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.R

@Composable
fun Authentication(
    modifier: Modifier = Modifier,
    method: AuthMethod,
    passwordVisible: Boolean = false,
    onVisibleChange: (Boolean) -> Unit,
    request: Triple<String, String, String>,
    onChange: (Triple<String, String, String>) -> Unit,
    errorState: ErrorState,
    onErrorStateChange: (ErrorState) -> Unit,
    buttonEnabled: Boolean,
    buttonText: String,
    onCodeRequest: () -> Unit,
    onDone: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember {
        FocusRequester()
    }
    val (email, password, code) = request
    LaunchedEffect(key1 = Unit, block = {
        focusRequester.requestFocus()
    })

    Column(modifier = modifier) {
        OutlinedTextField(
            value = email,
            onValueChange = {
                onChange(request.copy(first = it))
                onErrorStateChange(errorState.copy(email = false))
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            label = {
                Text(text = stringResource(id = R.string.email))
            },
            singleLine = true,
            isError = errorState.email,
            trailingIcon = when (method) {
                AuthMethod.LOGIN -> null
                AuthMethod.REGISTER -> {
                    val a: @Composable () -> Unit = {
                        TextButton(onClick = onCodeRequest, enabled = buttonEnabled) {
                            Text(text = buttonText)
                        }
                    }
                    a
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                autoCorrect = false
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                onChange(request.copy(second = it))
                onErrorStateChange(errorState.copy(password = false))
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = stringResource(id = R.string.password))
            }, singleLine = true,
            isError = errorState.password,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (method == AuthMethod.LOGIN) ImeAction.Done else ImeAction.Next,
                autoCorrect = false
            ), keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onDone()
                }
            ), trailingIcon = {
                IconButton(onClick = {
                    onVisibleChange(!passwordVisible)
                }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = null
                    )
                }
            }
        )
        AnimatedVisibility(visible = method == AuthMethod.REGISTER) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = code,
                onValueChange = {
                    onChange(request.copy(third = it))
                    onErrorStateChange(errorState.copy(code = false))
                },
                label = {
                    Text(text = stringResource(id = R.string.captcha))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onDone()
                    }
                ), isError = errorState.code,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

data class ErrorState(
    val email: Boolean = false,
    val password: Boolean = false,
    val code: Boolean = false
)