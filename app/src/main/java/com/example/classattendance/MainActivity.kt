```kotlin
package com.example.classattendance

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classattendance.data.*
import kotlinx.coroutines.*
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContent {
            App()
        }
    }
}

@Composable
fun App(vm: VM = viewModel()) {
    val classes by vm.classes.collectAsState(initial = emptyList())
    var selected by remember { mutableStateOf<SchoolClass?>(null) }
    var settings by remember { mutableStateOf(false) }

    MaterialTheme {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            if (settings) {
                SettingsScreen(vm) {
                    settings = false
                }
            } else if (selected == null) {
                Home(
                    classes = classes,
                    vm = vm,
                    open = { selected = it },
                    settings = { settings = true }
                )
            } else {
                ClassPage(
                    c = selected!!,
                    vm = vm
                ) {
                    selected = null
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    classes: List<SchoolClass>,
    vm: VM,
    open: (SchoolClass) -> Unit,
    settings: () -> Unit
) {
    var add by remember { mutableStateOf(false) }
    var n by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("حضور و غیاب کلاسی — نسخه ۳")
                },
                actions = {
                    IconButton(onClick = settings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "تنظیمات"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { add = true }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "افزودن"
                )
            }
        }
    ) { p ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(p),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = classes,
                key = { it.id }
            ) { c ->
                Card(
                    onClick = { open(c) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = c.name,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                vm.deleteClass(c)
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "حذف"
                            )
                        }
                    }
                }
            }
        }
    }

    if (add) {
        DialogSimple(
            title = "کلاس جدید",
            label = "نام کلاس",
            value = n,
            onValue = { n = it },
            ok = {
                if (n.isNotBlank()) {
                    vm.addClass(n)
                }
                n = ""
                add = false
            },
            cancel = {
                add = false
            }
        )
    }
}

@Composable
fun DialogSimple(
    title: String,
    label: String,
    value: String,
    onValue: (String) -> Unit,
    ok: () -> Unit,
    cancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = cancel,
        title = {
            Text(title)
        },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValue,
                label = {
                    Text(label)
                },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = ok
            ) {
                Text("تأیید")
            }
        },
        di
```
