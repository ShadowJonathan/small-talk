package app.dapk.st.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import app.dapk.st.core.Lce
import app.dapk.st.core.StartObserving
import app.dapk.st.core.components.CenteredLoading
import app.dapk.st.core.components.Header
import app.dapk.st.design.components.SettingsTextRow
import app.dapk.st.design.components.Spider
import app.dapk.st.design.components.SpiderPage
import app.dapk.st.design.components.TextRow
import app.dapk.st.navigator.Navigator
import app.dapk.st.settings.SettingsEvent.*
import app.dapk.st.settings.eventlogger.EventLogActivity

@Composable
internal fun SettingsScreen(viewModel: SettingsViewModel, onSignOut: () -> Unit, navigator: Navigator) {
    viewModel.ObserveEvents(onSignOut)
    LaunchedEffect(true) {
        viewModel.start()
    }

    val onNavigate: (SpiderPage<out Page>?) -> Unit = {
        when (it) {
            null -> navigator.navigate.upToHome()
            else -> viewModel.goTo(it)
        }
    }
    Spider(currentPage = viewModel.state.page, onNavigate = onNavigate) {
        item(Page.Routes.root) {
            RootSettings(it) { viewModel.onClick(it) }
        }
        item(Page.Routes.encryption) {
            Encryption(viewModel, it)
        }
        item(Page.Routes.importRoomKeys) {
            when (it.importProgress) {
                null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(0.8f),
                        ) {
                            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                                it?.let {
                                    viewModel.fileSelected(it)
                                }
                            }

                            Button(modifier = Modifier.fillMaxWidth(), onClick = { launcher.launch("text/*") }) {
                                Text(text = "SELECT FILE".uppercase())
                            }

                            if (it.selectedFile != null) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Importing file: ${it.selectedFile.name}", overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(24.dp))

                                var passphrase by rememberSaveable { mutableStateOf("") }
                                var passwordVisibility by rememberSaveable { mutableStateOf(false) }
                                val startImportAction = { viewModel.importFromFileKeys(it.selectedFile.uri, passphrase) }

                                TextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = passphrase,
                                    onValueChange = { passphrase = it },
                                    label = { Text("Passphrase") },
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Outlined.Lock, contentDescription = null)
                                    },
                                    keyboardActions = KeyboardActions(onDone = { startImportAction() }),
                                    keyboardOptions = KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
                                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        val image = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                            Icon(imageVector = image, contentDescription = null)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = startImportAction,
                                    enabled = passphrase.isNotEmpty()
                                ) {
                                    Text(text = "Import".uppercase())
                                }
                            }
                        }
                    }
                }
                is Lce.Content -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Import success")
                            Button(onClick = { navigator.navigate.upToHome() }) {
                                Text(text = "Close".uppercase())
                            }
                        }
                    }
                }
                is Lce.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Import failed")
                            Button(onClick = { navigator.navigate.upToHome() }) {
                                Text(text = "Close".uppercase())
                            }
                        }
                    }
                }
                is Lce.Loading -> CenteredLoading()
            }
        }
    }
}

@Composable
private fun RootSettings(page: Page.Root, onClick: (SettingItem) -> Unit) {
    when (val content = page.content) {
        is Lce.Content -> {
            LazyColumn(
                Modifier
                    .padding(bottom = 12.dp)
                    .fillMaxWidth()
            ) {
                items(content.value) { item ->
                    when (item) {
                        is SettingItem.Text -> {
                            val itemOnClick = onClick.takeIf { item.id != SettingItem.Id.Ignored }?.let {
                                { it.invoke(item) }
                            }

                            SettingsTextRow(item.content, item.subtitle, itemOnClick)
                        }
                        is SettingItem.AccessToken -> {
                            Row(
                                Modifier
                                    .padding(start = 24.dp, end = 24.dp)
                                    .clickable { onClick(item) }) {
                                Column {
                                    Spacer(Modifier.height(24.dp))
                                    Text(text = item.content, fontSize = 24.sp)
                                    Spacer(Modifier.height(24.dp))
                                    Divider(
                                        color = Color.Gray, modifier = Modifier
                                            .height(1.dp)
                                            .alpha(0.2f)
                                    )
                                }
                            }
                        }
                        is SettingItem.Header -> Header(item.label)
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun Encryption(viewModel: SettingsViewModel, page: Page.Security) {
    Column {
        TextRow("Import room keys", includeDivider = false, onClick = { viewModel.goToImportRoom() })
    }
}

@Composable
private fun SettingsViewModel.ObserveEvents(onSignOut: () -> Unit) {
    val context = LocalContext.current
    StartObserving {
        this@ObserveEvents.events.launch {
            when (it) {
                SignedOut -> onSignOut()
                is CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("dapk token", it.content))
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
                is SettingsEvent.Toast -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                OpenEventLog -> {
                    context.startActivity(Intent(context, EventLogActivity::class.java))
                }
                is OpenUrl -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW).apply { data = it.url.toUri() })
                }
            }
        }
    }
}
