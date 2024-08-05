package com.github.jing332.filepicker.utils

import androidx.core.bundle.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator


@Suppress("RestrictedApi")
fun NavController.navigate(
    route: String,
    argsBuilder: Bundle.() -> Unit = {},
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    navigate(route, Bundle().apply(argsBuilder), navOptions, navigatorExtras)
}

/*
* 可传递 Bundle 到 Navigation
* */
@Suppress("RestrictedApi")
fun NavController.navigate(
    route: String,
    args: Bundle,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    /*val routeLink = NavDeepLink.Builder()
        .setUriPattern(route)
        .build()
    //  kmm是上面的*/

    /*//以下是Android的
    val routeLink = NavDeepLinkRequest
        .Builder
        .fromUri(NavDestination.createRoute(route).toUri())
        .build()*/

    val deepLinkMatch = graph.matchDeepLink(route)
    if (deepLinkMatch != null) {
        val destination = deepLinkMatch.destination
        val route = destination.route ?: return
        navigate(route, args, navOptions, navigatorExtras)
    } else {
        navigate(route, navOptions, navigatorExtras)
    }
}