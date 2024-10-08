package com.github.jing332.filepicker.listpage

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.jing332.filepicker.FileFilter
import com.github.jing332.filepicker.FilePickerConfiguration
import com.github.jing332.filepicker.SearchType
import com.github.jing332.filepicker.SortConfig
import com.github.jing332.filepicker.SortType
import com.github.jing332.filepicker.ViewType
import com.github.jing332.filepicker.base.DateFormatterKmp
import com.github.jing332.filepicker.getExternalStorageDirectory
import com.github.jing332.filepicker.model.BackFileModel
import com.github.jing332.filepicker.model.IFileModel
import com.github.jing332.filepicker.utils.StringUtils.sizeToReadable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.measureTime

@Composable
fun rememberFileListPageState() = remember {
    FileListPageState()
}

class FileListPageState(
    val path: String = getExternalStorageDirectory(),
    initialViewType: Int = ViewType.LIST,
    initialSortConfig: SortConfig = SortConfig(),
    configuration: FilePickerConfiguration = FilePickerConfiguration(),
) {
    companion object {
        private val dateFormatter by lazy {
            DateFormatterKmp("yyyy-MM-dd HH:mm:ss")
        }
    }

    lateinit var file: IFileModel
    var viewType by mutableIntStateOf(ViewType.LIST)
    var sortConfig by mutableStateOf(SortConfig())

    val listState by lazy { LazyListState() }
    val gridState by lazy { LazyGridState() }

    internal var config: FilePickerConfiguration? = null
    internal val items = mutableStateListOf<FileItem>()

    internal fun models() = items.map { it.model }
    internal fun findSelectedItems() = items.filter { it.isChecked.value }

    internal fun check(item: FileItem, checked: Boolean = true) {
        item.isChecked.value = checked
    }

    internal fun selector(item: FileItem): Boolean {
        if (item.isBackType) return false

        if (item.isChecked.value) {
            item.isChecked.value = false
            return false
        }

        val list = config!!.fileSelector.select(findSelectedItems().map { it.model }, item.model)
        for (i in items) {
            i.isChecked.value = list.find { it == i.model } != null
        }

        return list.find { it == item.model } != null
    }


    fun hasChecked(): Boolean {
        return items.any { it.isChecked.value }
    }

    private fun IFileModel.filesSortAndFilter(
        sort: SortConfig,
        filter: FileFilter
    ): List<IFileModel> {
        return this.files().filter { filter.accept(it) }.sortedWith(
            compareBy(
                { !it.isDirectory },
                {
                    val str = when (sort.sortBy) {
                        SortType.NAME -> it.name
                        SortType.SIZE -> it.size.toString()
                        SortType.DATE -> it.time.toString()
                        SortType.TYPE -> it.name.split(".").lastOrNull() ?: ""
                        else -> it.name
                    }
                    str.lowercase()
                }
            )
        ).run {
            if (sort.reverse) reversed() else this
        }
    }

    internal suspend fun updateFiles(file: IFileModel) = coroutineScope {
        items.clear()
        if (file.path != path)
            items += FileItem(BackFileModel(), isBackType = true).apply {
                isCheckable.value = false
            }

        val cost = measureTime {
            items += file.filesSortAndFilter(sortConfig, config!!.fileFilter).map {
                FileItem(it)
            }
        }
        println("load files: $cost ms")

        launch(Dispatchers.Main) {
            for (item in items) {
                item.fileCount.intValue = withContext(Dispatchers.IO) {
                    //config!!.fileFilter
//                    item.model.fileCount
                    item.model.fileCountWithFilter(config!!.fileFilter)
                }
                item.fileSize.value =
                    withContext(Dispatchers.IO) { item.model.size.sizeToReadable() }
                item.fileLastModified.value = withContext(Dispatchers.IO) {
                    dateFormatter.format(item.model.time)
                }

                item.isCheckable.value = config!!.fileSelector.isCheckable(item.model)
            }
        }
    }

    fun createNewFolder(name: String) {
        file.createDirectory(name)
    }

    fun search(type: Int, text: String) {
        for (item in items) {
            item.isVisible.value = when (type) {
                SearchType.ALL -> item.name.contains(text, true)
                SearchType.FILE -> !item.isDirectory && item.name.contains(text, true)
                SearchType.FOLDER -> item.isDirectory && item.name.contains(text, true)
                else -> true
            }
        }
    }
}

internal data class FileItem(
    val model: IFileModel,
    val key: String = model.path,
    val name: String = model.name,
    val isDirectory: Boolean = model.isDirectory,
    val isBackType: Boolean = false,

    val isChecked: MutableState<Boolean> = mutableStateOf(false),
    val isCheckable: MutableState<Boolean> = mutableStateOf(false),
    val isVisible: MutableState<Boolean> = mutableStateOf(true),

    val fileCount: MutableIntState = mutableIntStateOf(0),
    val fileSize: MutableState<String> = mutableStateOf("0"),
    val fileLastModified: MutableState<String> = mutableStateOf(""),

    val icon: @Composable() (() -> Unit)? = null,
)