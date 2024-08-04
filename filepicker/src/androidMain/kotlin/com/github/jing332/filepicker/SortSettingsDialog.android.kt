package com.github.jing332.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewSortDialog() {
    var show by remember { mutableStateOf(true) }
    var sortConfig by rememberSaveable { mutableStateOf(SortConfig()) }
    if (show)
        SortSettingsDialog(onDismissRequest = { show = false }, sortConfig, { sortConfig = it })
}