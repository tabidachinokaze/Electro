package cn.tabidachi.electro.ui.profile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import cn.tabidachi.electro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navHostController: NavHostController
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
    val username = viewState.username
    val email = viewState.email
    val password = viewState.password
    LaunchedEffect(key1 = Unit, block = {
        viewModel.getUser()
    })
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.profile))
                }, navigationIcon = {
                    IconButton(onClick = {
                        navHostController.navigateUp()
                    }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }, actions = {
                    IconButton(onClick = {
                        viewModel.done {
                            navHostController.navigateUp()
                        }
                    }) {
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
                    value = username, onValueChange = viewModel::onUsernameChange,
                    label = {
                        Text(text = stringResource(id = R.string.username))
                    }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }
            item {
                TextField(
                    value = email, onValueChange = viewModel::onEmailChange,
                    label = {
                        Text(text = stringResource(id = R.string.email))
                    }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }
            item {
                TextField(
                    value = password,
                    onValueChange = viewModel::onPasswordChange,
                    label = {
                        Text(text = stringResource(id = R.string.password))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = if (viewState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = viewModel::onVisibleChange) {
                            if (viewState.passwordVisible) {
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