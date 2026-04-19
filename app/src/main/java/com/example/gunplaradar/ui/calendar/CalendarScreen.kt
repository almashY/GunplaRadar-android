package com.example.gunplaradar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gunplaradar.data.entity.GunplaItemEntity
import com.example.gunplaradar.data.entity.PatrolPlanEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (Long) -> Unit,
    onStockDiffClick: () -> Unit = {}
) {
    val cal = Calendar.getInstance().apply {
        set(uiState.currentYear, uiState.currentMonth, 1)
    }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    val today = Calendar.getInstance()

    val restockDates = uiState.itemsWithRestock.mapNotNull { item ->
        item.restockDate?.let { ts ->
            val c = Calendar.getInstance().apply { timeInMillis = ts }
            Triple(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)) to item
        }
    }.groupBy({ it.first }, { it.second })

    val patrolDates = uiState.patrolPlans.map { plan ->
        val c = Calendar.getInstance().apply { timeInMillis = plan.date }
        Triple(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
    }.toSet()

    var selectedDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("再販カレンダー") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onStockDiffClick) {
                Icon(Icons.Default.Add, contentDescription = "品出し差分登録")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Month navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "前月")
                }
                Text(
                    text = "${uiState.currentYear}年${uiState.currentMonth + 1}月",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "次月")
                }
            }

            // Day of week headers
            val dayLabels = listOf("日", "月", "火", "水", "木", "金", "土")
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                dayLabels.forEachIndexed { index, label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = when (index) {
                            0 -> Color(0xFFE53935)
                            6 -> Color(0xFF1E88E5)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        fontSize = 13.sp
                    )
                }
            }

            Divider(modifier = Modifier.padding(horizontal = 4.dp))

            // Calendar grid
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val day = cellIndex - firstDayOfWeek + 1
                            val isCurrentMonth = day in 1..daysInMonth
                            val dateKey = Triple(uiState.currentYear, uiState.currentMonth, day)
                            val hasRestock = isCurrentMonth && restockDates.containsKey(dateKey)
                            val hasPatrol = isCurrentMonth && patrolDates.contains(dateKey)
                            val isToday = isCurrentMonth &&
                                    today.get(Calendar.YEAR) == uiState.currentYear &&
                                    today.get(Calendar.MONTH) == uiState.currentMonth &&
                                    today.get(Calendar.DAY_OF_MONTH) == day
                            val isSelected = isCurrentMonth && selectedDay == day

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .then(
                                        if (isSelected && isCurrentMonth)
                                            Modifier
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                        else if (isToday)
                                            Modifier
                                                .clip(CircleShape)
                                                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        else Modifier
                                    )
                                    .clickable(enabled = isCurrentMonth) {
                                        if (isCurrentMonth) {
                                            selectedDay = day
                                            val clickedCal = Calendar.getInstance().apply {
                                                set(uiState.currentYear, uiState.currentMonth, day, 0, 0, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            onDayClick(clickedCal.timeInMillis)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCurrentMonth) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = 13.sp,
                                            color = when {
                                                hasPatrol -> Color(0xFFE53935)
                                                col == 0 -> Color(0xFFE53935)
                                                col == 6 -> Color(0xFF1E88E5)
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
                                            fontWeight = if (hasPatrol) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (hasRestock) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(top = 8.dp))

            // Items with restock on selected day
            val selectedDateKey = Triple(uiState.currentYear, uiState.currentMonth, selectedDay)
            val selectedItems = restockDates[selectedDateKey] ?: emptyList()
            val selectedPatrolPlans = uiState.patrolPlans.filter { plan ->
                val c = Calendar.getInstance().apply { timeInMillis = plan.date }
                c.get(Calendar.YEAR) == uiState.currentYear &&
                        c.get(Calendar.MONTH) == uiState.currentMonth &&
                        c.get(Calendar.DAY_OF_MONTH) == selectedDay
            }

            Text(
                text = "${uiState.currentYear}年${uiState.currentMonth + 1}月${selectedDay}日",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (selectedPatrolPlans.isNotEmpty()) {
                    item {
                        Text(
                            "巡回予定",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(selectedPatrolPlans) { plan ->
                        val sdf = SimpleDateFormat("HH:mm", Locale.JAPAN)
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Text(
                                text = "巡回 ${sdf.format(Date(plan.time))}",
                                modifier = Modifier.padding(8.dp),
                                color = Color(0xFFE53935)
                            )
                        }
                    }
                }
                if (selectedItems.isNotEmpty()) {
                    item {
                        Text(
                            "再販予定",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(selectedItems) { item ->
                        RestockItemRow(item)
                    }
                }
                if (selectedItems.isEmpty() && selectedPatrolPlans.isEmpty()) {
                    item {
                        Text(
                            "この日の予定はありません",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RestockItemRow(item: GunplaItemEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Medium)
                Text(
                    item.grade,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.price != null) {
                Text(
                    "¥${"%,d".format(item.price)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
