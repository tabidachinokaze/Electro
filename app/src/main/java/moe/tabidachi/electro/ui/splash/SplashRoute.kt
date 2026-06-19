package moe.tabidachi.electro.ui.splash

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute : NavKey

fun NavGraphBuilder.splash() = composable<SplashRoute> {}

fun EntryProviderScope<NavKey>.splash() = entry<SplashRoute> {}
