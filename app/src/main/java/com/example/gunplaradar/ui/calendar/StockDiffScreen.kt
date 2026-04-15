package com.example.gunplaradar.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gunplaradar.data.entity.GunplaItemEntity
import com.example.gunplaradar.data.entity.StockDelayRecordEntity
import com.example.gunplaradar.data.entity.StoreEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDiffScreen(
    items: List<GunplaItemEntity>,
    stores: List<StoreEntity>,
    onSave: (StockDelayRecordEntity) -> Unit,
    onBack: () -> Unit
) {
    var selectedItem by remember { mutableStateOf<GunplaItemEntity?>(null) }
    var selectedStore by remember { mutableStateOf<StoreEntity?>(null) }
    var restockDateText by remember { mutableStateOf("") }
    var actualStockDateText by remember { mutableStateOf("") }
    var itemExpanded by remember { mutableStateOf(false) }
    var storeExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("品出し差分登録") },
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
            // Item selection
            Text("ガンプラ選択", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = itemExpanded,
                onExpandedChange = { itemExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedItem?.name ?: "選択してください",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = itemExpanded,
                    onDismissRequest = { itemExpanded = false }
                ) {
                    items.forEach { item ->
                        DropdownMenuItem(
                            text = { Text("${item.name} (${item.grade})") },
                            onClick = {
                                selectedItem = item
                                if (item.restockDate != null) {
                                    restockDateText = sdf.format(Date(item.restockDate))
                                }
                                itemExpanded = false
                            }
                        )
                    }
                }
            }

            // Store selection
            Text("店舗選択", style = MaterialTheme.typography.labelLarge)
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
                value = restockDateText,
                onValueChange = { restockDateText = it },
                label = { Text("再販日 (yyyy/MM/dd)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = actualStockDateText,
                onValueChange = { actualStockDateText = it },
                label = { Text("実際の品出し日 (yyyy/MM/dd)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val item = selectedItem
                    val store = selectedStore
                    if (item == null || store == null) {
                        errorMessage = "ガンプラと店舗を選択してください"
                        return@Button
                    }
                    val restockDate = try {
                        sdf.parse(restockDateText)?.time
                    } catch (e: Exception) { null }
                    val actualDate = try {
                        sdf.parse(actualStockDateText)?.time
                    } catch (e: Exception) { null }
                    if (restockDate == null || actualDate == null) {
                        errorMessage = "日付の形式が正しくありません (yyyy/MM/dd)"
                        return@Button
                    }
                    val delayHours = (actualDate - restockDate).toDouble() / (1000 * 60 * 60)
                    val record = StockDelayRecordEntity(
                        storeId = store.id,
                        itemId = item.id,
                        restockDate = restockDate,
                        actualStockDate = actualDate,
                        delayHours = delayHours
                    )
                    onSave(record)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("登録する")
            }
        }
    }
}
