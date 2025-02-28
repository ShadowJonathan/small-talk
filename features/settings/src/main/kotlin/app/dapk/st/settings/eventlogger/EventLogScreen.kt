package app.dapk.st.settings.eventlogger

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dapk.st.core.AppLogTag
import app.dapk.st.core.Lce
import app.dapk.st.matrix.common.MatrixLogTag

private val filterItems = listOf<String?>(null) + (MatrixLogTag.values().map { it.key } + AppLogTag.values().map { it.key }).distinct()

@Composable
fun EventLogScreen(viewModel: EventLoggerViewModel) {
    LaunchedEffect(true) {
        viewModel.start()
    }

    val state = viewModel.state
    when (val keys = state.logs) {
        is Lce.Content -> {
            when (state.selectedState) {
                null -> {
                    LogKeysList(keys.value) {
                        viewModel.selectLog(it, filter = null)
                    }
                }
                else -> {
                    Events(
                        selectedPageContent = state.selectedState,
                        onExit = { viewModel.exitLog() },
                        onSelectTag = { viewModel.selectLog(state.selectedState.selectedPage, it) }
                    )
                }
            }
        }
    }

}

@Composable
private fun LogKeysList(keys: List<String>, onSelected: (String) -> Unit) {
    LazyColumn {
        items(keys) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(8.dp).clickable {
                    onSelected(it)
                },
                text = it,
                fontSize = 32.sp,
            )
        }
    }
}

@Composable
private fun Events(selectedPageContent: SelectedState, onExit: () -> Unit, onSelectTag: (String?) -> Unit) {
    BackHandler(onBack = onExit)
    when (val content = selectedPageContent.content) {
        is Lce.Content -> {
            Column {
                Row {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Text(
                            modifier = Modifier.clickable { expanded = true }.padding(8.dp),
                            text = "Filter: ${selectedPageContent.filter ?: "all"}",
                            fontSize = 20.sp,
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            filterItems.forEachIndexed { index, item ->
                                DropdownMenuItem(
                                    onClick = {
                                        expanded = false
                                        onSelectTag(filterItems[index])
                                    }
                                ) {
                                    Text(item ?: "all")
                                }
                            }
                        }
                    }
                }

                LazyColumn(Modifier.weight(1f)) {
                    items(content.value) {
                        val text = when (selectedPageContent.filter) {
                            null -> "${it.time}: ${it.tag}: ${it.content}"
                            else -> "${it.time}: ${it.content}"
                        }

                        Text(
                            text = text,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }
        is Lce.Error -> TODO()
        is Lce.Loading -> {
            // TODO
        }
    }
}