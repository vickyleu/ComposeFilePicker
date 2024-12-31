package com.github.jing332.filepicker

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.LocalPlatformContext
import com.github.jing332.filepicker.Contants.ARG_PATH
import com.github.jing332.filepicker.Contants.ROUTE_PAGE
import com.github.jing332.filepicker.base.FileImpl
import com.github.jing332.filepicker.listpage.FileListPage
import com.github.jing332.filepicker.model.IFileModel
import com.github.jing332.filepicker.model.NormalFile
import compose_filepicker.filepicker.generated.resources.Res
import compose_filepicker.filepicker.generated.resources.error_permission_denied
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource

internal actual fun String.formatImpl(vararg args: Any): String {
    return this.format(*args)
}

fun navControllerSetup(navController: NavHostController) {
    navController.enableOnBackPressed(false)
}

@Composable
fun FilePicker(
    state: FilePickerState = rememberFilePickerState(),
    config: FilePickerConfiguration = remember { FilePickerConfiguration() },
    close: () -> Unit={},
    onSelectFile: ((IFileModel) -> Unit)? = null,
) {
    val rootPath = state.rootPath
    val rootName = state.rootName
    val context = LocalPlatformContext.current
    val navController = state.navController
    val stackEntry by navController.currentBackStackEntryAsState()
    val navBarItems = remember { mutableStateListOf<NavBarItem>() }

    var currentPath by remember {
        mutableStateOf(
            stackEntry?.arguments?.getString(ARG_PATH) ?: rootPath
        )
    }
    LaunchedEffect(stackEntry) {
        val path = stackEntry?.arguments?.getString(ARG_PATH) ?: rootPath
        currentPath = path
    }

    LaunchedEffect(Unit) {
        snapshotFlow { currentPath }
            .distinctUntilChanged()
            .collect {
                toNavBarItems(
                    rootPath = rootPath,
                    rootName = rootName,
                    path = it
                ).also {
                    navBarItems.clear();
                    navBarItems.addAll(it)
                }
            }
    }
    fun popBack() {
        navController.popBackStack()
    }
    PermissionGrant(state)
    Column(Modifier) {
        val selectedItems = state.currentListState?.findSelectedItems() ?: emptyList()
        val flagCloseSearch = remember { mutableStateOf(false) }
        val deniedStr = stringResource(Res.string.error_permission_denied)
        FilePickerToolbar(
            modifier = Modifier.fillMaxWidth(),
            title = navBarItems.toList().lastOrNull()?.name ?: "",
            sortConfig = config.sortConfig,
            onSortConfigChange = {
                config.sortConfig = it
                state.reload()
            },
            viewType = config.viewType,
            onSwitchViewType = {
                config.viewType = it
                state.reload()
            },
            selectedCount = selectedItems.size,
            closeSearch = flagCloseSearch,
            onSearch = { type, text ->
                state.currentListState?.search(type, text)
            },
            onRefresh = {
                state.reload()
            }
        )
        NavHost(
            modifier = Modifier.weight(1f),
            navController = navController,
            startDestination = ROUTE_PAGE
        ) {
            composable(Contants.ROUTE_PAGE) { entry ->
                navControllerSetup(navController)

                println("enterFile.path: entry.arguments: ${entry.arguments}  ${entry.destination.arguments}" +
                        "entry.destination: ${entry.destination.route} ${entry.id}")
                val path = entry.arguments?.getString(Contants.ARG_PATH) ?: rootPath

                println("enterFile.path: $path")
                val fileListState = state.getListState(path).apply {
                    sortConfig = config.sortConfig
                    viewType = config.viewType
                }

                LaunchedEffect(key1 = Unit) {
                    flagCloseSearch.value = true
                    state.currentPath = path

                }
                BackHandler(true) {
                    if(path != rootPath){
                        popBack()
                    }else {close.invoke()}

                }

                val file = FileImpl(path)
                FileListPage(
                    modifier = Modifier
                        .fillMaxSize(),
                    file = NormalFile(file),
                    state = fileListState,
                    config = config,
                    onBack = {
                        popBack()
                    },
                    onEnter = { enterFile ->
                        println("enterFile.path222: ${enterFile.path}")
                        state.navigate(enterFile.path)
                        navBarItems += NavBarItem(
                            name = enterFile.name,
                            path = enterFile.path
                        )
                    },
                    onSelect = {
                        onSelectFile?.invoke(it)
                    }
                )

            }
        }
    }
}

@Composable
actual fun startPickerHandler(
    scope: CoroutineScope,
    callback: ((IFileModel) -> Unit), close: () -> Unit
) {
    val context = LocalPlatformContext.current
    FilePicker(
        state = rememberFilePickerState(),
        config = FilePickerConfiguration(
            fileFilter = {
                if (it.name.startsWith(".") ||
                    (it.isDirectory.not() && it.name.contains(".").not())
                ) {
                    false
                } else {
                    if (it.isDirectory.not() && it.name.contains(".")) {
                        val extension = it.name.split(".").lastOrNull()?.lowercase() ?: ""
                        val isImage = extension in listOf(
                            "jpg",
                            "jpeg",
                            "png",
                            "gif",
                            "bmp",
                            "webp",
                            "thumbnail"
                        )
                        val isCompressionFile =
                            extension in listOf("zip", "rar", "7z", "tar", "gz", "tgz")
                        val isDesignFile = extension in listOf(
                            "psd",
                            "sketch",
                            "dwg",
                            "ai",
                            "pdf",
                            "cdr",
                            "indd",
                            "eps",
                            "dae"
                        )
                        val isTextFile =
                            extension in listOf("txt", "rtf", "xml", "csv", "log", "md")
                        val isVideo = extension in listOf("mp4", "avi", "mov", "mkv", "flv", "wmv")
                        val isAudio = extension in listOf("mp3", "wav", "aac", "flac", "ogg", "m4a")
                        val isDocument =
                            extension in listOf("doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf")
                        if (isImage) return@FilePickerConfiguration false
                        if (isCompressionFile || isDesignFile || isTextFile || isVideo || isAudio || isDocument) {
                            return@FilePickerConfiguration true
                        }
                        return@FilePickerConfiguration false
                    }
                    true
                }
            },
            fileSelector = { checkedList, check ->
                val pass = !check.isDirectory
                if (pass) {
                    listOf(check)
                } else {
                    showToast(
                        context,
                        "Only  file can be selected.",
                    )
                    checkedList
                }
            }
        ),
        close=close,
        onSelectFile = {
            callback.invoke(it)
        }
    )

}