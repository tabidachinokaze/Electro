package moe.tabidachi.electro.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.autofill.contentType
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import moe.tabidachi.electro.R
import moe.tabidachi.electro.ui.auth.AuthContract.Event
import moe.tabidachi.electro.ui.auth.AuthContract.State
import moe.tabidachi.electro.ui.preview.PreviewSurface
import moe.tabidachi.electro.ui.preview.Previews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    state: State,
    event: (Event) -> Unit,
    hostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                actions = {
                    TextButton(
                        onClick = { event(Event.ChangeAuthMethod) }
                    ) {
                        AnimatedContent(targetState = state.method.toggle(), label = "") {
                            Text(text = stringResource(id = it.id))
                        }
                    }
                    IconButton(
                        onClick = {
                            event(Event.NavigateToServer)
                        }
                    ) {
                        Icon(imageVector = Icons.Outlined.Cloud, contentDescription = null)
                    }
                    IconButton(
                        onClick = { event(Event.NavigateToLocaleSettings) }
                    ) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "Language")
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    AnimatedContent(targetState = state.method, label = "") {
                        Text(text = stringResource(id = it.id))
                    }
                },
                icon = {
                    when (state.isProcessing) {
                        true -> CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )

                        false -> AnimatedContent(
                            targetState = state.method,
                            label = ""
                        ) {
                            val imageVector = when (it) {
                                AuthMethod.LOGIN -> Icons.AutoMirrored.Rounded.ArrowForward
                                AuthMethod.REGISTER -> Icons.Rounded.PersonAdd
                            }
                            Icon(imageVector = imageVector, contentDescription = null)
                        }
                    }
                },
                onClick = {
                    event(Event.Auth)
                },
                expanded = !state.isProcessing,
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = hostState)
        },
        contentWindowInsets = WindowInsets.statusBars
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(OuterPadding)
            ) {
                SubcomposeLayout { constrains ->
                    val contentPlaceable = subcompose("content") {
                        AuthContent(
                            method = state.method,
                            request = state.request,
                            passwordVisible = state.passwordVisible,
                            onVisibleChange = { event(Event.OnPasswordVisibleChange(it)) },
                            onChange = { event(Event.OnRequestChange(it)) },
                            errorState = state.errorState,
                            onErrorStateChange = { event(Event.ErrorStateChange(it)) },
                            buttonText = state.buttonText ?: stringResource(id = R.string.captcha),
                            buttonEnabled = state.buttonEnabled,
                            onCodeRequest = { event(Event.OnCodeRequest) },
                            onDone = { event(Event.Auth) },
                            modifier = Modifier.padding(OuterPadding)
                        )
                    }[0].measure(constrains)
                    val contentHeight = contentPlaceable.height
                    val imagePlaceable = subcompose("image") {
                        val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
                        Image(
                            painter = painterResource(R.drawable.raiden_shogun_background),
                            contentDescription = null,
                            modifier = Modifier.drawWithContent {
                                drawContent()
                                drawRect(color = surfaceContainer.copy(alpha = 0.7f))
                            },
                            contentScale = ContentScale.Crop
                        )
                    }[0].measure(constrains.copy(maxHeight = contentHeight))
                    layout(constrains.maxWidth, contentHeight) {
                        imagePlaceable.place(0, 0)
                        contentPlaceable.place(0, 0)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AuthContent(
    modifier: Modifier = Modifier,
    method: AuthMethod,
    passwordVisible: Boolean = false,
    onVisibleChange: (Boolean) -> Unit,
    request: Triple<TextFieldValue, TextFieldValue, TextFieldValue>,
    onChange: (Triple<TextFieldValue, TextFieldValue, TextFieldValue>) -> Unit,
    errorState: ErrorState,
    onErrorStateChange: (ErrorState) -> Unit,
    buttonEnabled: Boolean,
    buttonText: String,
    onCodeRequest: () -> Unit,
    onDone: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val (email, password, code) = request
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    Column(modifier = modifier) {
        OutlinedTextField(
            value = email,
            onValueChange = {
                onChange(request.copy(first = it))
                onErrorStateChange(errorState.copy(email = false))
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .contentType(ContentType.EmailAddress),
            label = {
                Text(text = stringResource(id = R.string.email))
            },
            singleLine = true,
            isError = errorState.email,
            trailingIcon = when (method) {
                AuthMethod.LOGIN -> null
                AuthMethod.REGISTER -> {
                    {
                        TextButton(onClick = onCodeRequest, enabled = buttonEnabled) {
                            Text(text = buttonText)
                        }
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                onChange(request.copy(second = it))
                onErrorStateChange(errorState.copy(password = false))
            },
            modifier = Modifier
                .fillMaxWidth()
                .contentType(ContentType.Password),
            label = {
                Text(text = stringResource(id = R.string.password))
            }, singleLine = true,
            isError = errorState.password,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (method == AuthMethod.LOGIN) ImeAction.Done else ImeAction.Next,
                autoCorrect = false
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onDone()
                }
            ),
            trailingIcon = {
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
                ),
                isError = errorState.code,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Previews
@Composable
private fun AuthScreenPreview() {
    PreviewSurface {
        AuthScreen(
            state = State(method = AuthMethod.LOGIN),
            event = {},
            hostState = SnackbarHostState()
        )
    }
}

val OuterPadding = 16.dp

val TopOuterPadding =
    PaddingValues(start = OuterPadding, top = OuterPadding, end = OuterPadding)

val BottomOuterPadding =
    PaddingValues(start = OuterPadding, end = OuterPadding, bottom = OuterPadding)

val HorizontalOuterPadding = PaddingValues(horizontal = OuterPadding)
