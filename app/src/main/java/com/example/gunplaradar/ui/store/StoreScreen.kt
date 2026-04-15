package com.example.gunplaradar.ui.store

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gunplaradar.data.entity.StoreEntity
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    uiState: StoreUiState,
    onSearchQueryChange: (String) -> Unit,
    onAddStore: () -> Unit,
    onListClick: () -> Unit,
    onMarkerClick: (StoreEntity) -> Unit
) {
    val defaultLocation = LatLng(35.6812, 139.7671) // 東京
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("店舗分析") },
                actions = {
                    IconButton(onClick = onListClick) {
                        Icon(Icons.Default.List, contentDescription = "一覧")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddStore) {
                Icon(Icons.Default.Add, contentDescription = "店舗追加")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                uiState.stores.forEach { store ->
                    Marker(
                        state = MarkerState(position = LatLng(store.latitude, store.longitude)),
                        title = store.name,
                        snippet = "平均遅延: ${"%.1f".format(store.averageDelayHours)}時間",
                        onClick = {
                            onMarkerClick(store)
                            false
                        }
                    )
                }
            }

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                placeholder = { Text("店舗名で検索") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}
