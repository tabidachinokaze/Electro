package cn.tabidachi.electro.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudCircle
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.twotone.Cloud
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.ElectroNavigationActions

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    navigationActions: ElectroNavigationActions,
    navHostController: NavHostController
) {
    val viewState by authViewModel.viewState.collectAsState()
    val hostState = authViewModel.hostState

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Electro")
                }, actions = {
                    TextButton(onClick = {
                        authViewModel.changeAuthMethod()
                    }) {
                        AnimatedContent(targetState = viewState.method.toggle(), label = "") {
                            Text(text = stringResource(id = it.id))
                        }
                    }
                    IconButton(onClick = {
                        navigationActions.navigateToServer()
                    }) {
                        Icon(imageVector = Icons.Outlined.Cloud, contentDescription = null)
                    }
                    IconButton(onClick = {
                        authViewModel.languageMenuExpandedChange(true)
                    }) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "Language")
                    }
                    DropdownMenu(expanded = viewState.isLanguageMenuExpanded, onDismissRequest = {
                        authViewModel.languageMenuExpandedChange(false)
                    }) {
                        DropdownMenuItem(text = {
                            Text(text = stringResource(id = R.string.chinese))
                        }, onClick = {
                            authViewModel.languageMenuExpandedChange(false)
                        })
                        DropdownMenuItem(text = {
                            Text(text = stringResource(id = R.string.english))
                        }, onClick = {
                            authViewModel.languageMenuExpandedChange(false)
                        })
                    }
                }, scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        }, floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    AnimatedContent(targetState = viewState.method, label = "") {
                        Text(text = stringResource(id = it.id))
                    }
                },
                icon = {
                    when (viewState.isProcessing) {
                        true -> CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )

                        false -> AnimatedContent(
                            targetState = viewState.method, label = ""
                        ) {
                            val imageVector = when (it) {
                                AuthMethod.LOGIN -> Icons.Rounded.ArrowForward
                                AuthMethod.REGISTER -> Icons.Rounded.PersonAdd
                            }
                            Icon(imageVector = imageVector, contentDescription = null)
                        }
                    }
                },
                onClick = authViewModel::auth, expanded = !viewState.isProcessing,
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
            )
        }, snackbarHost = {
            SnackbarHost(hostState = hostState)
        }, contentWindowInsets = WindowInsets.statusBars
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(OuterPadding))
            Card(
                modifier = Modifier
                    .padding(BottomOuterPadding)
            ) {
                Column(
                    modifier = Modifier
                        .padding(OuterPadding)
                ) {
                    Authentication(
                        method = viewState.method,
                        request = viewState.request,
                        passwordVisible = viewState.passwordVisible,
                        onVisibleChange = authViewModel::onPasswordVisibleChange,
                        onChange = authViewModel::onRequestChange,
                        errorState = viewState.errorState,
                        onErrorStateChange = authViewModel::errorStateChange,
                        buttonText = viewState.buttonText ?: stringResource(id = R.string.captcha),
                        buttonEnabled = viewState.buttonEnabled,
                        onCodeRequest = authViewModel::onCodeRequest,
                        onDone = authViewModel::auth
                    )
                }
            }
        }
    }
}

val OuterPadding = 16.dp

val TopOuterPadding =
    PaddingValues(start = OuterPadding, top = OuterPadding, end = OuterPadding)

val BottomOuterPadding =
    PaddingValues(start = OuterPadding, end = OuterPadding, bottom = OuterPadding)

val HorizontalOuterPadding = PaddingValues(horizontal = OuterPadding)