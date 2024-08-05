package com.github.jing332.filepicker.utils

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

@Suppress("RestrictedApi")
actual fun NavController.navigate(
    route: String,
    args: Map<String, Any>,
    navOptions: NavOptions?,
    navigatorExtras: Navigator.Extras?
) {

}