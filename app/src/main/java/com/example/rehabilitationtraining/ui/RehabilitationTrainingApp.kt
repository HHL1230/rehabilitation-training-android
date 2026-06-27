package com.example.rehabilitationtraining.ui

import android.Manifest
import android.content.Context
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
import androidx.compose.material3.ColorScheme
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
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

private enum class AppThemePalette(
    val primary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondaryContainer: Color,
    val surfaceVariant: Color,
) {
    OCEAN(
        primary = Color(0xFF1565C0),
        primaryContainer = Color(0xFFD7E3FF),
        onPrimaryContainer = Color(0xFF001B3F),
        secondaryContainer = Color(0xFFE0E7F5),
        surfaceVariant = Color(0xFFF1F4FA),
    ),
    FOREST(
        primary = Color(0xFF2E7D32),
        primaryContainer = Color(0xFFD9F0D3),
        onPrimaryContainer = Color(0xFF092100),
        secondaryContainer = Color(0xFFE3EEDB),
        surfaceVariant = Color(0xFFF2F7EF),
    ),
    SUNRISE(
        primary = Color(0xFFB85C00),
        primaryContainer = Color(0xFFFFDDBE),
        onPrimaryContainer = Color(0xFF331A00),
        secondaryContainer = Color(0xFFF5E4D2),
        surfaceVariant = Color(0xFFFFF4EA),
    ),
    LAVENDER(
        primary = Color(0xFF6A4FB3),
        primaryContainer = Color(0xFFE8DDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondaryContainer = Color(0xFFECE5F8),
        surfaceVariant = Color(0xFFF8F3FF),
    ),
    ROSE(
        primary = Color(0xFF9D4059),
        primaryContainer = Color(0xFFFFD9E2),
        onPrimaryContainer = Color(0xFF3F0018),
        secondaryContainer = Color(0xFFF8E0E6),
        surfaceVariant = Color(0xFFFFF1F4),
    );

    fun colorScheme(): ColorScheme = lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onPrimaryContainer,
        surface = Color(0xFFFFFBFF),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = Color(0xFF1C1B1F),
        background = Color(0xFFFFFBFF),
        onBackground = Color(0xFF1C1B1F),
    )
}

private val DateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

private const val THEME_PREFS_NAME = "app_theme_settings"
private const val THEME_COUNTER_KEY = "theme_counter"

private fun nextThemePaletteIndex(context: Context): Int {
    val preferences = context.applicationContext.getSharedPreferences(
        THEME_PREFS_NAME,
        Context.MODE_PRIVATE,
    )
    val nextCounter = preferences.getInt(THEME_COUNTER_KEY, -1) + 1
    preferences.edit().putInt(THEME_COUNTER_KEY, nextCounter).apply()
    return nextCounter % AppThemePalette.entries.size
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RehabilitationTrainingApp(viewModelFactory: ViewModelProvider.Factory) {
    val viewModel: TrainingViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val paletteIndex = remember { mutableStateOf(nextThemePaletteIndex(context)) }
    val launchPalette = AppThemePalette.entries[paletteIndex.value]
    var pendingReminderTypeName by rememberSaveable { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner, context) {
        var shouldChangeOnResume = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (shouldChangeOnResume) {
                    paletteIndex.value = nextThemePaletteIndex(context)
                } else {
                    shouldChangeOnResume = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val type = TrainingType.entries.firstOrNull { it.name == pendingReminderTypeName }
            if (type != null) {
                viewModel.setReminderEnabled(type, true)
            }
        } else {
            viewModel.showStatus("需要通知權限才能顯示每日提醒")
        }
        pendingReminderTypeName = null
    }

    MaterialTheme(
        colorScheme = launchPalette.colorScheme(),
        typography = AppTypography,
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            var selectedTab by rememberSaveable { mutableStateOf(0) }
            val tabs = listOf("紀錄", "統計", "提醒", "分享")

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("腿部復健訓練") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                },
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
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
                            onToggleReminder = { type, enabled ->
                                if (
                                    enabled &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    pendingReminderTypeName = type.name
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setReminderEnabled(type, enabled)
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
            Text(
                text = "今天",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        OutlinedButton(
            onClick = { onDateSelected(-1) },
            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
        ) {
            Text(
                text = "昨天",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        OutlinedButton(
            onClick = { onDateSelected(-2) },
            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
        ) {
            Text(
                text = "前天",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
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
        Text(
            text = "紀錄時間：${state.recordHour.padStart(2, '0')}:${state.recordMinute.padStart(2, '0')}",
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumericField(
                label = "小時（0-23）",
                value = state.recordHour,
                onValueChange = viewModel::updateRecordHour,
                modifier = Modifier.weight(1f),
            )
            NumericField(
                label = "分鐘（0-59）",
                value = state.recordMinute,
                onValueChange = viewModel::updateRecordMinute,
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider()

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
    onToggleReminder: (TrainingType, Boolean) -> Unit,
    onHourChange: (TrainingType, String) -> Unit,
    onMinuteChange: (TrainingType, String) -> Unit,
    onSaveTime: (TrainingType) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard(title = "每項訓練各自提醒") {
            Text(
                text = "可以為彈力帶彎腿、阻力伸腿、阻力騎腳踏車分別開啟提醒與設定時間。",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "提醒訊息會包含訓練名稱，方便知道現在該做哪一項。",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        TrainingType.entries.forEach { type ->
            val settings = state.reminderSettingsByType[type] ?: com.example.rehabilitationtraining.reminder.ReminderSettings()
            val input = state.reminderTimeInputsByType[type] ?: ReminderTimeInput()

            SectionCard(title = "${type.displayName}提醒") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("開啟${type.displayName}提醒", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "目前設定：${if (settings.enabled) "每日 ${settings.formattedTime}" else "未開啟"}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Switch(
                        checked = settings.enabled,
                        onCheckedChange = { enabled -> onToggleReminder(type, enabled) },
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumericField(
                        label = "小時（0-23）",
                        value = input.hour,
                        onValueChange = { value -> onHourChange(type, value) },
                        modifier = Modifier.weight(1f),
                    )
                    NumericField(
                        label = "分鐘（0-59）",
                        value = input.minute,
                        onValueChange = { value -> onMinuteChange(type, value) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onSaveTime(type) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                ) {
                    Text("儲存${type.displayName}提醒時間")
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "訊息：要做「${type.displayName}」訓練囉，要有耐心，一定會進步，恢復行動自如，加油！",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
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
            Button(
                onClick = {
                    share(
                        records = viewModel.recordsFromLast7Days(),
                        title = "近 7 天復健訓練紀錄",
                        emptyMessage = "近 7 天沒有可分享的紀錄",
                    )
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Text("分享近 7 天紀錄")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
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
