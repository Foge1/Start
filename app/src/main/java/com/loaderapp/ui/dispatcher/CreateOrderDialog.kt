package com.loaderapp.ui.dispatcher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderDialog(
    onDismiss: () -> Unit,
    onCreate: (address: String, dateTime: Long, cargo: String, price: Double) -> Unit
) {
    var address by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var cargo by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый заказ") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Адрес") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Дата (дд.мм.гггг)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("15.02.2026") }
                    )
                    
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Время (чч:мм)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("14:00") }
                    )
                }
                
                OutlinedTextField(
                    value = cargo,
                    onValueChange = { cargo = it },
                    label = { Text("Описание груза") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Цена за час (₽)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                if (showError) {
                    Text(
                        text = "Заполните все поля корректно",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        if (address.isNotBlank() && 
                            date.isNotBlank() && 
                            time.isNotBlank() && 
                            cargo.isNotBlank() && 
                            price.isNotBlank()) {
                            
                            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            val dateTime = dateFormat.parse("$date $time")?.time ?: System.currentTimeMillis()
                            val priceValue = price.toDoubleOrNull() ?: 0.0
                            
                            if (priceValue > 0) {
                                onCreate(address, dateTime, cargo, priceValue)
                            } else {
                                showError = true
                            }
                        } else {
                            showError = true
                        }
                    } catch (e: Exception) {
                        showError = true
                    }
                }
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
