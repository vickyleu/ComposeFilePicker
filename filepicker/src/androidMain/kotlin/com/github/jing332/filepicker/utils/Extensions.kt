package com.github.jing332.filepicker.utils


import android.os.Bundle
import android.os.Parcelable
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

//@Suppress("RestrictedApi")
//fun NavController.navigateImpl(
//    route: String,
//    argsBuilder: Map<String, Any>.() -> Unit = {},
//    navOptions: NavOptions? = null,
//    navigatorExtras: Navigator.Extras? = null
//) {
//    val map = mapOf<String,Any>()
//    argsBuilder.invoke(map)
//    navigateImpl(route, args = map, navOptions, navigatorExtras)
//}

@Suppress("RestrictedApi")
fun NavHostController.navigateImpl(
    route: String,
    args: Map<String, Any>,
    navOptions: NavOptions?,
    navigatorExtras: Navigator.Extras?
) {
    val bundle = args.toBundle()
    // 拼接参数到 route
    val appendedRoute = args.entries.joinToString("&", "$route?") {
        "${it.key}=${
            URLEncoder.encode(
                "${it.value}",
                StandardCharsets.UTF_8.toString()
            )
        }"
    }
    val routeLink = NavDeepLinkRequest
        .Builder
        .fromUri(NavDestination.createRoute(route).toUri())
        .build()
    val deepLinkMatch = graph.matchDeepLink(routeLink)
    if (deepLinkMatch != null) {
        val destination = deepLinkMatch.destination
        val id = destination.id
        navigate(id, bundle, navOptions, navigatorExtras)
    } else {
        navigate(appendedRoute, navOptions, navigatorExtras)
    }
}

private fun <K, V> Map<K, V>.toBundle(): Bundle {
    val bundle = Bundle()
    for ((key, value) in this) {
        when (value) {
            is Int -> bundle.putInt(key as String, value)
            is Long -> bundle.putLong(key as String, value)
            is String -> bundle.putString(key as String, value)
            is Float -> bundle.putFloat(key as String, value)
            is Double -> bundle.putDouble(key as String, value)
            is Boolean -> bundle.putBoolean(key as String, value)
            is Bundle -> bundle.putBundle(key as String, value)
            is Parcelable -> bundle.putParcelable(key as String, value)
            // 添加更多类型处理
            else -> throw IllegalArgumentException("Unsupported type: ${value!!::class.java}")
        }
    }
    return bundle
}
