package moe.tabidachi.electro.ui.profile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import moe.tabidachi.electro.R
import moe.tabidachi.electro.ui.preview.PreviewSurface
import moe.tabidachi.electro.ui.preview.Previews
import moe.tabidachi.electro.ui.profile.ProfileContract.Event
import moe.tabidachi.electro.ui.profile.ProfileContract.State

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: State,
    event: (Event) -> Unit
) {
    val username = state.username
    val email = state.email
    val password = state.password
    LaunchedEffect(Unit) {
        event(Event.GetUser)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.profile))
                },
                navigationIcon = {
                    IconButton(
                        onClick = { event(Event.NavigateUp) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { event(Event.Done) }
                    ) {
                        Icon(imageVector = Icons.Rounded.Done, contentDescription = null)
                    }
                }
            )
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = it.calculateTopPadding())
        ) {
            item {
                TextField(
                    value = username,
                    onValueChange = {
                        event(Event.OnUsernameChange(it))
                    },
                    label = {
                        Text(text = stringResource(id = R.string.username))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }
            item {
                TextField(
                    value = email,
                    onValueChange = {
                        event(Event.OnEmailChange(it))
                    },
                    label = {
                        Text(text = stringResource(id = R.string.email))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }
            item {
                TextField(
                    value = password,
                    onValueChange = {
                        event(Event.OnPasswordChange(it))
                    },
                    label = {
                        Text(text = stringResource(id = R.string.password))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { event(Event.OnVisibleChange) }
                        ) {
                            if (state.passwordVisible) {
                                Icon(
                                    imageVector = Icons.Rounded.VisibilityOff,
                                    contentDescription = null
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Visibility,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
@Previews
private fun ProfileScreenPreview() {
    PreviewSurface {
        ProfileScreen(
            state = State(),
            event = {}
        )
    }
}
