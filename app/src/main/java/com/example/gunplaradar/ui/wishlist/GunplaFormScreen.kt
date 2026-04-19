package com.example.gunplaradar.ui.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gunplaradar.data.entity.GunplaItemEntity
import java.text.SimpleDateFormat
import java.util.*

private fun Long.toUtcMidnight(): Long {
    val local = Calendar.getInstance()
    local.timeInMillis = this
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
    utc.set(Calendar.MILLISECOND, 0)
    return utc.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GunplaFormScreen(
    existingItem: GunplaItemEntity? = null,
    onSave: (GunplaItemEntity) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(existingItem?.name ?: "") }
    var grade by remember { mutableStateOf(existingItem?.grade ?: "") }
    var priceText by remember { mutableStateOf(existingItem?.price?.toString() ?: "") }
    var url by remember { mutableStateOf(existingItem?.url ?: "") }
    var priority by remember { mutableIntStateOf(existingItem?.priority ?: 2) }
    var tagColor by remember { mutableIntStateOf(existingItem?.tagColor ?: 0) }

    val sdf = remember { SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN) }

    var releaseDateText by remember {
        mutableStateOf(
            existingItem?.releaseDate?.let { sdf.format(Date(it)) } ?: ""
        )
    }
    var restockDateText by remember {
        mutableStateOf(
            existingItem?.restockDate?.let { sdf.format(Date(it)) } ?: ""
        )
    }
    var nameError by remember { mutableStateOf(false) }
    var gradeError by remember { mutableStateOf(false) }

    var showReleaseDatePicker by remember { mutableStateOf(false) }
    var showRestockDatePicker by remember { mutableStateOf(false) }

    val releaseDateInteractionSource = remember { MutableInteractionSource() }
    val isReleaseDatePressed by releaseDateInteractionSource.collectIsPressedAsState()
    LaunchedEffect(isReleaseDatePressed) {
        if (isReleaseDatePressed) showReleaseDatePicker = true
    }

    val restockDateInteractionSource = remember { MutableInteractionSource() }
    val isRestockDatePressed by restockDateInteractionSource.collectIsPressedAsState()
    LaunchedEffect(isRestockDatePressed) {
        if (isRestockDatePressed) showRestockDatePicker = true
    }

    val releaseDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = existingItem?.releaseDate?.toUtcMidnight()
    )
    val restockDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = existingItem?.restockDate?.toUtcMidnight()
    )

    val tagColors = listOf(
        Color(0xFF9E9E9E),
        Color(0xFFE53935),
        Color(0xFF43A047),
        Color(0xFF1E88E5),
        Color(0xFFFDD835),
        Color(0xFF8E24AA)
    )
    val priorityLabels = listOf("最高", "高", "中", "低")

    if (showReleaseDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showReleaseDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    releaseDatePickerState.selectedDateMillis?.let {
                        releaseDateText = sdf.format(Date(it))
                    }
                    showReleaseDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showReleaseDatePicker = false }) { Text("キャンセル") }
            }
        ) {
            DatePicker(state = releaseDatePickerState)
        }
    }

    if (showRestockDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showRestockDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    restockDatePickerState.selectedDateMillis?.let {
                        restockDateText = sdf.format(Date(it))
                    }
                    showRestockDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRestockDatePicker = false }) { Text("キャンセル") }
            }
        ) {
            DatePicker(state = restockDatePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingItem == null) "ガンプラを追加" else "ガンプラを編集") },
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
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("名前 *") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                supportingText = if (nameError) ({ Text("名前は必須です") }) else null,
                singleLine = true
            )

            OutlinedTextField(
                value = grade,
                onValueChange = {
                    grade = it
                    gradeError = false
                },
                label = { Text("グレード *") },
                placeholder = { Text("例: HG, MG, RG, PG") },
                modifier = Modifier.fillMaxWidth(),
                isError = gradeError,
                supportingText = if (gradeError) ({ Text("グレードは必須です") }) else null,
                singleLine = true
            )

            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                label = { Text("価格 (円)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text("¥") },
                singleLine = true
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = releaseDateText,
                onValueChange = {},
                label = { Text("発売日") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                interactionSource = releaseDateInteractionSource,
                trailingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = "カレンダーを開く")
                }
            )

            OutlinedTextField(
                value = restockDateText,
                onValueChange = {},
                label = { Text("再販日") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                interactionSource = restockDateInteractionSource,
                trailingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = "カレンダーを開く")
                }
            )

            // 優先度
            Text("優先度", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                priorityLabels.forEachIndexed { index, label ->
                    FilterChip(
                        selected = priority == index,
                        onClick = { priority = index },
                        label = { Text(label) }
                    )
                }
            }

            // タグカラー
            Text("タグカラー", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tagColors.forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { tagColor = index }
                            .then(
                                if (tagColor == index)
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    nameError = name.isBlank()
                    gradeError = grade.isBlank()
                    if (nameError || gradeError) return@Button

                    val releaseDate = try {
                        if (releaseDateText.isNotBlank()) sdf.parse(releaseDateText)?.time else null
                    } catch (e: Exception) { null }
                    val restockDate = try {
                        if (restockDateText.isNotBlank()) sdf.parse(restockDateText)?.time else null
                    } catch (e: Exception) { null }

                    val item = (existingItem ?: GunplaItemEntity(name = "", grade = "")).copy(
                        name = name.trim(),
                        grade = grade.trim(),
                        price = priceText.toIntOrNull(),
                        url = url.trim().ifBlank { null },
                        releaseDate = releaseDate,
                        restockDate = restockDate,
                        priority = priority,
                        tagColor = tagColor
                    )
                    onSave(item)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (existingItem == null) "追加する" else "保存する")
            }
        }
    }
}
