package com.loaderapp.ui.loader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loaderapp.data.model.Order
import com.loaderapp.data.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoaderScreen(
    viewModel: LoaderViewModel,
    userName: String,
    onSwitchRole: () -> Unit
) {
    val availableOrders by viewModel.availableOrders.collectAsState()
    val myOrders by viewModel.myOrders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showSwitchDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Доступные", "Мои заказы")
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Column {
                            Text("Грузчик")
                            Text(
                                text = userName,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSwitchDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Сменить роль"
                            )
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> AvailableOrdersList(
                    orders = availableOrders,
                    onTakeOrder = { viewModel.takeOrder(it) }
                )
                1 -> MyOrdersList(
                    orders = myOrders,
                    onCompleteOrder = { viewModel.completeOrder(it) }
                )
            }
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
    
    if (showSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchDialog = false },
            title = { Text("Сменить роль?") },
            text = { Text("Вы хотите выйти из режима грузчика?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSwitchDialog = false
                        onSwitchRole()
                    }
                ) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSwitchDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}

@Composable
fun AvailableOrdersList(
    orders: List<Order>,
    onTakeOrder: (Order) -> Unit
) {
    if (orders.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Нет доступных заказов",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                AvailableOrderCard(
                    order = order,
                    onTake = { onTakeOrder(order) }
                )
            }
        }
    }
}

@Composable
fun MyOrdersList(
    orders: List<Order>,
    onCompleteOrder: (Order) -> Unit
) {
    if (orders.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "У вас нет заказов",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                MyOrderCard(
                    order = order,
                    onComplete = { onCompleteOrder(order) }
                )
            }
        }
    }
}

@Composable
fun AvailableOrderCard(
    order: Order,
    onTake: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = order.address,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Дата: ${dateFormat.format(Date(order.dateTime))}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Груз: ${order.cargoDescription}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Text(
                text = "Оплата: ${order.pricePerHour} ₽/час",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Button(
                onClick = onTake,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("Взять заказ", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun MyOrderCard(
    order: Order,
    onComplete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.address,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                StatusChip(status = order.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Дата: ${dateFormat.format(Date(order.dateTime))}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Груз: ${order.cargoDescription}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Text(
                text = "Оплата: ${order.pricePerHour} ₽/час",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            if (order.status == OrderStatus.TAKEN) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Завершить", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: OrderStatus) {
    val (text, color) = when (status) {
        OrderStatus.AVAILABLE -> "Доступен" to MaterialTheme.colorScheme.primary
        OrderStatus.TAKEN -> "В работе" to MaterialTheme.colorScheme.tertiary
        OrderStatus.COMPLETED -> "Завершен" to MaterialTheme.colorScheme.secondary
        OrderStatus.CANCELLED -> "Отменен" to MaterialTheme.colorScheme.error
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = color
        )
    }
}
