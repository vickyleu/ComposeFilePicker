package com.github.jing332.filepicker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.internal.BackHandler
import coil3.compose.LocalPlatformContext
import com.github.jing332.filepicker.Contants.ARG_PATH
import com.github.jing332.filepicker.Contants.ROUTE_PAGE
import com.github.jing332.filepicker.base.FileImpl
import com.github.jing332.filepicker.listpage.FileListPage
import com.github.jing332.filepicker.model.IFileModel
import com.github.jing332.filepicker.model.NormalFile
import compose_filepicker.filepicker.generated.resources.Res
import compose_filepicker.filepicker.generated.resources.error_permission_denied
import compose_filepicker.filepicker.generated.resources.ok
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

private const val TAG = "FilePicker"


@OptIn(InternalVoyagerApi::class)
@Composable
fun FilePicker(
    modifier: Modifier = Modifier,
    state: FilePickerState = rememberFilePickerState(),
    config: FilePickerConfiguration = remember { FilePickerConfiguration() },
    onSaveFile: ((IFileModel, String) -> Boolean)? = null,
    onSelectFile: ((IFileModel) -> Unit)? = null,
    onConfirmSelect: (List<IFileModel>) -> Unit,
    onEnterDirectory: (IFileModel) -> Boolean = {
        if (it.path.startsWith(getExternalStorageDirectory() + "/Android")) {
            println("$TAG onEnterDirectory: $it")
            false
        } else {
            state.navigate(it.path)
            true
        }
    },
) {
    val rootPath = state.rootPath
    val rootName = state.rootName
    val saveMode = onSaveFile != null
    val context = LocalPlatformContext.current
    val navController = state.navController
    val stackEntry by navController.currentBackStackEntryAsState()
    val navBarItems = remember { mutableStateListOf<NavBarItem>() }


    LaunchedEffect(key1 = stackEntry) {
        val path = stackEntry?.arguments?.getString(ARG_PATH) ?: rootPath
        toNavBarItems(
            rootPath = rootPath,
            rootName = rootName,
            path = path
        ).also { navBarItems.clear(); navBarItems.addAll(it) }
    }

    fun popBack() {
        navController.popBackStack()
    }

    val scope = rememberCoroutineScope()
    if (getPlatform() == Platform.IOS) {
        startPickerHandler(scope) {
            onSaveFile?.invoke(it, it.name)
            onSelectFile?.invoke(it)
        }
    } else {
        PermissionGrant()
        Column(modifier) {
            val selectedItems = state.currentListState?.findSelectedItems() ?: emptyList()
            if (selectedItems.isNotEmpty() && saveMode) {
                selectedItems.getOrNull(0)?.name?.let {
                    state.saveFilename = it
                }
                state.currentListState?.uncheckAll()
            }
            val flagCloseSearch = remember { mutableStateOf(false) }

            val deniedStr = stringResource(Res.string.error_permission_denied)

            FilePickerToolbar(
                modifier = Modifier.fillMaxWidth(),
                title = navBarItems.lastOrNull()?.name ?: "",
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
                onCancelSelect = {
                    state.currentListState?.uncheckAll()
                },
                onConfirmSelect = {
                    onConfirmSelect(state.currentListState?.items?.filter { it.isChecked.value }
                        ?.map { it.model } ?: emptyList())
                },
                onNewFolder = {
                    try {
                        state.currentListState?.createNewFolder(it)
                        state.reload()
                    } catch (e: SecurityFromFilePickerException) {
                        println("$TAG createNewFolder:$e")
                        deniedStr.formatImpl(e.message ?: "").let { showToast(context, it) }
                    } catch (e: Exception) {
                        println("$TAG createNewFolder:$e")
                        showToast(context, e.message ?: "")
                    }
                },
                closeSearch = flagCloseSearch,
                onSearch = { type, text ->
                    state.currentListState?.search(type, text)
                },
                onRefresh = {
                    state.reload()
                }
            )

            FileNavBar(
                list = navBarItems,
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = { item ->
                    while (true) {
                        val uri =
                            navController.currentBackStackEntry?.arguments?.getString(ARG_PATH)
                                ?: break
                        if (uri == item.path) break
                        else popBack()
                    }
                }
            )

            NavHost(
                modifier = Modifier.weight(1f),
                navController = navController,
                startDestination = ROUTE_PAGE
            ) {
                composable(ROUTE_PAGE) { entry ->
                    navControllerSetup(navController)

                    val path = entry.arguments?.getString(ARG_PATH) ?: rootPath
                    val fileListState = state.getListState(path).apply {
                        sortConfig = config.sortConfig
                        viewType = config.viewType
                    }


                    LaunchedEffect(key1 = Unit) {
                        flagCloseSearch.value = true
                        state.currentPath = path

                    }
                    BackHandler(path != rootPath) {
                        popBack()
                    }
                    BackHandler(selectedItems.isNotEmpty()) {
                        state.currentListState?.uncheckAll()
                    }

                    val file = FileImpl(path)
                    FileListPage(
                        file = NormalFile(file),
                        state = fileListState,
                        config = config,
                        onBack = {
                            if (fileListState.hasChecked()) {
                                state.currentListState?.uncheckAll()
                            } else
                                popBack()
                        },
                        onEnter = { enterFile ->
                            if (onEnterDirectory(enterFile))
                                navBarItems += NavBarItem(
                                    name = enterFile.name,
                                    path = enterFile.path
                                )
                        }
                    )

                }
            }

            AnimatedVisibility(visible = saveMode) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    DenseTextField(
                        modifier = Modifier.weight(1f),
                        value = state.saveFilename,
                        onValueChange = {
                            state.saveFilename = it
                        },
                        leadingIcon = {
                            val detect =
                                config.fileDetector.detect(NormalFile(FileImpl(state.currentPath + "/" + state.saveFilename)))
                            if (detect == null)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                            else
                                detect.IconContent()
                        }
                    )

                    FilledTonalButton(
                        modifier = modifier.padding(start = 8.dp), onClick = {
                            onSaveFile?.invoke(
                                state.currentListState?.file!!,
                                state.saveFilename
                            )
                        }) {
//                    Icon(Icons.Default.Save, stringResource(id = android.R.string.ok))
                        Text(stringResource(Res.string.ok))
                    }
                }
            }

        }
    }
}

@Composable
expect fun startPickerHandler(scope: CoroutineScope, callback: (NormalFile) -> Unit)
enum class Platform {
    IOS, ANDROID
}

expect fun getPlatform(): Platform

expect fun navControllerSetup(navController: NavHostController)

expect fun String.formatImpl(vararg args: Any): String
