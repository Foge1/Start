package com.loaderapp.ui.dispatcher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
fun DispatcherScreen(
    viewModel: DispatcherViewModel,
    userName: String,
    onSwitchRole: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSwitchDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Панель диспетчера")
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Создать заказ") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        text = "Нет заказов",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Создайте первый заказ",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders) { order ->
                        OrderCard(
                            order = order,
                            onCancel = { viewModel.cancelOrder(it) }
                        )
                    }
                }
            }
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
    
    if (showCreateDialog) {
        CreateOrderDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { address, dateTime, cargo, price ->
                viewModel.createOrder(address, dateTime, cargo, price)
                showCreateDialog = false
            }
        )
    }
    
    if (showSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchDialog = false },
            title = { Text("Сменить роль?") },
            text = { Text("Вы хотите выйти из режима диспетчера?") },
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
fun OrderCard(
    order: Order,
    onCancel: (Order) -> Unit
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
            
            if (order.status == OrderStatus.AVAILABLE) {
                Button(
                    onClick = { onCancel(order) },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Отменить")
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: OrderStatus) {
    val (text, color) = when (status) {
        OrderStatus.AVAILABLE -> "Доступен" to MaterialTheme.colorScheme.primary
        OrderStatus.TAKEN -> "Занят" to MaterialTheme.colorScheme.tertiary
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
