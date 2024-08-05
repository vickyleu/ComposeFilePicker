package com.github.jing332.filepicker.utils

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator


@Suppress("RestrictedApi")
fun NavController.navigate(
    route: String,
    argsBuilder: Map<String, Any>.() -> Unit = {},
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    val map = mapOf<String, Any>()
    argsBuilder.invoke(map)
    navigate(route, map, navOptions, navigatorExtras)
}

/*
* 可传递 Bundle 到 Navigation
* */
@Suppress("RestrictedApi")
expect fun NavController.navigate(
    route: String,
    args: Map<String, Any>,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
)