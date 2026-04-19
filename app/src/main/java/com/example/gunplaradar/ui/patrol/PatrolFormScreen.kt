package com.example.gunplaradar.ui.patrol

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gunplaradar.data.entity.GunplaItemEntity
import com.example.gunplaradar.data.entity.PatrolPlanEntity
import com.example.gunplaradar.data.entity.StoreEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatrolFormScreen(
    stores: List<StoreEntity>,
    items: List<GunplaItemEntity>,
    onSave: (PatrolPlanEntity) -> Unit,
    onBack: () -> Unit
) {
    var selectedStore by remember { mutableStateOf<StoreEntity?>(null) }
    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    var selectedItemIds by remember { mutableStateOf(setOf<String>()) }
    var notifyEnabled by remember { mutableStateOf(true) }
    var storeExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val dateInteractionSource = remember { MutableInteractionSource() }
    val isDatePressed by dateInteractionSource.collectIsPressedAsState()
    LaunchedEffect(isDatePressed) {
        if (isDatePressed) showDatePicker = true
    }

    val sdf = remember { SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN) }
    val tdf = remember { SimpleDateFormat("HH:mm", Locale.JAPAN) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dateText = sdf.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("キャンセル") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("巡回予定を作成") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Store selection
            Text("店舗", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = storeExpanded,
                onExpandedChange = { storeExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedStore?.name ?: "選択してください",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = storeExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = storeExpanded,
                    onDismissRequest = { storeExpanded = false }
                ) {
                    stores.forEach { store ->
                        DropdownMenuItem(
                            text = { Text(store.name) },
                            onClick = {
                                selectedStore = store
                                storeExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = dateText,
                onValueChange = {},
                label = { Text("日付") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                interactionSource = dateInteractionSource,
                trailingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = "カレンダーを開く")
                }
            )

            OutlinedTextField(
                value = timeText,
                onValueChange = { timeText = it },
                label = { Text("時間 (HH:mm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Target items
            Text("対象アイテム", style = MaterialTheme.typography.labelLarge)
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${item.name} (${item.grade})",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Checkbox(
                        checked = selectedItemIds.contains(item.id),
                        onCheckedChange = { checked ->
                            selectedItemIds = if (checked) {
                                selectedItemIds + item.id
                            } else {
                                selectedItemIds - item.id
                            }
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("通知を有効にする", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = notifyEnabled, onCheckedChange = { notifyEnabled = it })
            }

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val store = selectedStore
                    if (store == null) {
                        errorMessage = "店舗を選択してください"
                        return@Button
                    }
                    val date = try {
                        sdf.parse(dateText)?.time
                    } catch (e: Exception) { null }
                    val time = try {
                        tdf.parse(timeText)?.time
                    } catch (e: Exception) { null }
                    if (date == null || time == null) {
                        errorMessage = "日付・時間の形式が正しくありません"
                        return@Button
                    }
                    val plan = PatrolPlanEntity(
                        date = date,
                        time = time,
                        storeId = store.id,
                        targetItemIds = selectedItemIds.joinToString(","),
                        notifyEnabled = notifyEnabled
                    )
                    onSave(plan)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("作成する")
            }
        }
    }
}
