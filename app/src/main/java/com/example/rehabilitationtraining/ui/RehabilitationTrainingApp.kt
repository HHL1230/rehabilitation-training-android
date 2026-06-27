package com.example.rehabilitationtraining.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rehabilitationtraining.data.TrainingRecordEntity
import com.example.rehabilitationtraining.data.TrainingType
import com.example.rehabilitationtraining.sharing.RecordShareManager
import com.example.rehabilitationtraining.sharing.TrainingRecordExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val AppTypography = Typography(
    titleLarge = TextStyle(fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 22.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = 20.sp, lineHeight = 30.sp),
    bodyMedium = TextStyle(fontSize = 18.sp, lineHeight = 27.sp),
    labelLarge = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),
)

private val DateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RehabilitationTrainingApp(viewModelFactory: ViewModelProvider.Factory) {
    val viewModel: TrainingViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.setReminderEnabled(true)
        } else {
            viewModel.showStatus("需要通知權限才能顯示每日提醒")
        }
    }

    MaterialTheme(typography = AppTypography) {
        Surface(modifier = Modifier.fillMaxSize()) {
            var selectedTab by rememberSaveable { mutableStateOf(0) }
            val tabs = listOf("紀錄", "統計", "提醒", "分享")

            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("復健訓練") })
                },
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    SafetyNotice()
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) },
                            )
                        }
                    }

                    when (selectedTab) {
                        0 -> RecordEntryScreen(state, viewModel)
                        1 -> DashboardScreen(state)
                        2 -> ReminderScreen(
                            state = state,
                            onToggleReminder = { enabled ->
                                if (
                                    enabled &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setReminderEnabled(enabled)
                                }
                            },
                            onHourChange = viewModel::updateReminderHour,
                            onMinuteChange = viewModel::updateReminderMinute,
                            onSaveTime = viewModel::saveReminderTime,
                        )
                        3 -> ShareScreen(state, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun SafetyNotice() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Text(
            text = "請依照醫師或物理治療師建議調整訓練量；若疼痛或不適，請停止並詢問專業人員。",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun RecordEntryScreen(state: TrainingUiState, viewModel: TrainingViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = "今天要記錄哪一項訓練？") {
                DateSelector(
                    selectedDateEpochDay = state.selectedDateEpochDay,
                    onDateSelected = viewModel::setSelectedDate,
                )
                Spacer(Modifier.height(12.dp))
                ExerciseTypeSelector(
                    selectedType = state.selectedType,
                    onTypeSelected = viewModel::selectType,
                )
            }
        }

        item {
            SectionCard(title = "訓練內容") {
                RecordFields(state, viewModel)
            }
        }

        item {
            StatusMessages(state)
            Button(
                onClick = viewModel::saveRecord,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
            ) {
                Text("儲存紀錄")
            }
        }

        item {
            RecentRecords(records = state.records.take(5))
        }
    }
}

@Composable
private fun DateSelector(
    selectedDateEpochDay: Long,
    onDateSelected: (Long) -> Unit,
) {
    Text("日期：${formatDate(selectedDateEpochDay)}", style = MaterialTheme.typography.bodyLarge)
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { onDateSelected(0) },
            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
        ) {
            Text("今天")
        }
        OutlinedButton(
            onClick = { onDateSelected(-1) },
            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
        ) {
            Text("昨天")
        }
        OutlinedButton(
            onClick = { onDateSelected(-2) },
            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
        ) {
            Text("前天")
        }
    }
}

@Composable
private fun ExerciseTypeSelector(
    selectedType: TrainingType,
    onTypeSelected: (TrainingType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TrainingType.entries.forEach { type ->
            if (type == selectedType) {
                Button(
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                ) {
                    Text(type.displayName)
                }
            } else {
                OutlinedButton(
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                ) {
                    Text(type.displayName)
                }
            }
        }
    }
}

@Composable
private fun RecordFields(state: TrainingUiState, viewModel: TrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (state.selectedType) {
            TrainingType.BAND_LEG_CURL -> {
                NumericField(
                    label = "訓練時間（分鐘）",
                    value = state.durationMinutes,
                    onValueChange = viewModel::updateDurationMinutes,
                )
            }

            TrainingType.LEG_EXTENSION -> {
                NumericField(
                    label = "組數",
                    value = state.sets,
                    onValueChange = viewModel::updateSets,
                )
                NumericField(
                    label = "每組次數",
                    value = state.reps,
                    onValueChange = viewModel::updateReps,
                )
                NumericField(
                    label = "重量負荷（公斤）",
                    value = state.weightKg,
                    onValueChange = viewModel::updateWeightKg,
                    keyboardType = KeyboardType.Decimal,
                )
            }

            TrainingType.RESISTED_CYCLING -> {
                NumericField(
                    label = "訓練時間（分鐘）",
                    value = state.durationMinutes,
                    onValueChange = viewModel::updateDurationMinutes,
                )
                NumericField(
                    label = "阻力等級（1 到 20）",
                    value = state.resistanceLevel,
                    onValueChange = viewModel::updateResistanceLevel,
                )
            }
        }

        OutlinedTextField(
            value = state.notes,
            onValueChange = viewModel::updateNotes,
            label = { Text("備註（可不填）") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
        )
    }
}

@Composable
private fun NumericField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Number,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
    )
}

@Composable
private fun DashboardScreen(state: TrainingUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = "完成狀態") {
                Text(
                    text = if (state.stats.todayRecordCount > 0) {
                        "今天已有 ${state.stats.todayRecordCount} 筆訓練紀錄。"
                    } else {
                        "今天還沒有訓練紀錄。"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        item {
            SectionCard(title = "近 7 天") {
                StatLine("紀錄筆數", "${state.stats.last7RecordCount} 筆")
                StatLine("總訓練時間", "${state.stats.last7DurationMinutes} 分鐘")
            }
        }

        item {
            SectionCard(title = "近 30 天") {
                StatLine("紀錄筆數", "${state.stats.last30RecordCount} 筆")
                StatLine("總訓練時間", "${state.stats.last30DurationMinutes} 分鐘")
            }
        }

        item {
            SectionCard(title = "各項訓練統計") {
                TrainingType.entries.forEachIndexed { index, type ->
                    val stats = state.stats.byType.getValue(type)
                    StatLine(type.displayName, "${stats.recordCount} 筆，${stats.totalDurationMinutes} 分鐘")
                    if (index != TrainingType.entries.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }

        item {
            RecentRecords(records = state.records.take(10))
        }
    }
}

@Composable
private fun ReminderScreen(
    state: TrainingUiState,
    onToggleReminder: (Boolean) -> Unit,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
    onSaveTime: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard(title = "每日提醒") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("開啟提醒", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "訊息：要做訓練囉，要有耐心，一定會進步，恢復行動自如，加油！",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Switch(
                    checked = state.reminderSettings.enabled,
                    onCheckedChange = onToggleReminder,
                )
            }
        }

        SectionCard(title = "提醒時間") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumericField(
                    label = "小時（0-23）",
                    value = state.reminderHour,
                    onValueChange = onHourChange,
                    modifier = Modifier.weight(1f),
                )
                NumericField(
                    label = "分鐘（0-59）",
                    value = state.reminderMinute,
                    onValueChange = onMinuteChange,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onSaveTime,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Text("儲存提醒時間")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "目前設定：${if (state.reminderSettings.enabled) "每日 ${state.reminderSettings.formattedTime}" else "未開啟"}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        StatusMessages(state)
    }
}

@Composable
private fun ShareScreen(state: TrainingUiState, viewModel: TrainingViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun share(records: List<TrainingRecordEntity>, title: String, emptyMessage: String) {
        if (records.isEmpty()) {
            viewModel.showStatus(emptyMessage)
            return
        }

        coroutineScope.launch {
            val chooserIntent = withContext(Dispatchers.IO) {
                RecordShareManager.createShareIntent(context, records, title)
            }
            context.startActivity(chooserIntent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard(title = "分享給家人或照護者") {
            Text(
                text = "App 會產生照護者可讀的摘要文字與 CSV 檔，透過 Android 分享面板傳送。若手機有安裝 LINE，可直接選 LINE 分享。",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    share(
                        records = viewModel.recordsFromLast30Days(),
                        title = "近 30 天復健訓練紀錄",
                        emptyMessage = "近 30 天沒有可分享的紀錄",
                    )
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Text("分享近 30 天紀錄")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    share(
                        records = state.records,
                        title = "全部復健訓練紀錄",
                        emptyMessage = "目前沒有可分享的紀錄",
                    )
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Text("分享全部紀錄")
            }
        }

        StatusMessages(state)
        RecentRecords(records = state.records.take(5))
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecentRecords(records: List<TrainingRecordEntity>) {
    SectionCard(title = "最近紀錄") {
        if (records.isEmpty()) {
            Text("尚無訓練紀錄。", style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                records.forEach { record ->
                    Text(
                        text = TrainingRecordExporter.formatRecord(record),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusMessages(state: TrainingUiState) {
    if (state.validationMessages.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                state.validationMessages.forEach { message ->
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    state.statusMessage?.let { message ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun formatDate(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).format(DateFormatter)
