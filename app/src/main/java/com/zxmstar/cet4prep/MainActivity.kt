package com.zxmstar.cet4prep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.zxmstar.cet4prep.data.Cet4Database
import com.zxmstar.cet4prep.data.Cet4Repository
import com.zxmstar.cet4prep.data.DayTaskEntity
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val Primary = Color(0xFF4A90A4)
private val Highlight = Color(0xFFFF9A56)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = Room.databaseBuilder(applicationContext, Cet4Database::class.java, "cet4.db").build()
        setContent { CET4Theme { CET4App(Cet4Repository(db.dayTaskDao())) } }
    }
}

@Composable
private fun CET4Theme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = Primary, secondary = Highlight), content = content)
}

@Composable
private fun CET4App(repository: Cet4Repository) {
    val scope = rememberCoroutineScope()
    val tasks by repository.observeToday().collectAsState(initial = emptyList())
    var selectedTab by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { repository.seedToday() }
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            AnimatedContent(targetState = selectedTab, label = "tab") { tab ->
                when (tab) {
                    0 -> HomeScreen(tasks) { task -> scope.launch { repository.setCompleted(task.title, !task.completed) } }
                    1 -> CalendarScreen()
                    2 -> LibraryScreen()
                    else -> ProfileScreen()
                }
            }
        }
        NavigationBar(Modifier.navigationBarsPadding(), containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .94f)) {
            val nav = listOf(Icons.Default.Home to "首页", Icons.Default.CalendarMonth to "日历", Icons.Default.MenuBook to "题库", Icons.Default.Person to "我的")
            nav.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = {
                        val scale by animateFloatAsState(if (selectedTab == index) 1.08f else 1f, spring(), label = "nav")
                        Icon(item.first, item.second, Modifier.scale(scale))
                    },
                    label = { Text(item.second) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Primary, selectedTextColor = Primary, indicatorColor = Primary.copy(alpha = .12f))
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(tasks: List<DayTaskEntity>, onTaskClick: (DayTaskEntity) -> Unit) {
    val start = LocalDate.of(2026, 9, 14)
    val exam = LocalDate.of(2026, 12, 13)
    val today = LocalDate.now()
    val day = (ChronoUnit.DAYS.between(start, today).toInt() + 1).coerceIn(1, 90)
    val remaining = ChronoUnit.DAYS.between(today, exam).coerceAtLeast(0)
    val progress = day / 90f
    val completed = tasks.count { it.completed }
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("90天备考计划 · 第$day天", 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("今天也向四级靠近一点", 25.sp, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.size(44.dp).background(Primary.copy(alpha = .12f), RoundedCornerShape(16.dp)), Alignment.Center) { Text("四", color = Primary, fontWeight = FontWeight.Bold) }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Primary)) {
                Column(Modifier.padding(20.dp)) {
                    Text("距离 CET-4 考试", 13.sp, color = Color.White.copy(alpha = .82f))
                    Text("$remaining 天", 39.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("基础攻坚期 · 当前完成度 ${(progress * 100).toInt()}%", 13.sp, color = Color.White.copy(alpha = .9f))
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(7.dp).background(Color.White.copy(alpha = .22f), RoundedCornerShape(8.dp))) {
                        Box(Modifier.fillMaxWidth(progress).height(7.dp).background(Color.White, RoundedCornerShape(8.dp)))
                    }
                }
            }
        }
        item { Row(verticalAlignment = Alignment.CenterVertically) { Text("今日任务", 19.sp, fontWeight = FontWeight.Bold, Modifier.weight(1f)); Text("$completed / ${tasks.size} 项", 13.sp, color = Primary) } }
        items(tasks, key = { it.title }) { task -> TaskCard(task, onTaskClick) }
        item {
            AnimatedVisibility(completed == tasks.size && tasks.isNotEmpty(), enter = fadeIn() + scaleIn(), exit = fadeOut()) {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Highlight.copy(alpha = .12f))) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Check, null, tint = Highlight); Spacer(Modifier.width(10.dp)); Text("今日学习完成，保持这个节奏。") }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(task: DayTaskEntity, onClick: (DayTaskEntity) -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .96f else 1f, spring(), label = "press")
    val accent = if (task.title.contains("听力")) Highlight else Primary
    Card(Modifier.fillMaxWidth().scale(scale).combinedClickable(onClick = { pressed = false; onClick(task) }, onLongClick = { pressed = false }), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(43.dp).background(accent, RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) { Text(task.title, 15.sp, fontWeight = FontWeight.SemiBold); Text(task.detail, Modifier.padding(top = 5.dp), 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(if (task.completed) "✓" else task.duration, color = if (task.completed) Primary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = if (task.completed) 22.sp else 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CalendarScreen() {
    val tabs = listOf("基础攻坚期", "题型强化期", "冲刺模考期")
    var selected by remember { mutableStateOf(0) }
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("备考日历", 28.sp, fontWeight = FontWeight.Bold); Text("90天阶段规划", 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f), RoundedCornerShape(18.dp)).padding(4.dp)) {
                tabs.forEachIndexed { i, title -> Box(Modifier.weight(1f).background(if (i == selected) Primary else Color.Transparent, RoundedCornerShape(15.dp)).combinedClickable(onClick = { selected = i }, onLongClick = {}).padding(vertical = 11.dp), Alignment.Center) { Text(title, fontSize = 12.sp, color = if (i == selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
        }
        item { Card(shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("第${selected + 1}阶段", 18.sp, fontWeight = FontWeight.Bold); Text(if (selected == 0) "建立词汇、听力与阅读基础" else if (selected == 1) "围绕题型进行专项强化" else "模拟考试、错题复盘与冲刺", color = MaterialTheme.colorScheme.onSurfaceVariant); Text("点击具体日期后查看当天任务", 13.sp, color = Primary) } } }
        items((1..7).toList()) { week -> Card(shape = RoundedCornerShape(20.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text("第 $week 周", fontWeight = FontWeight.Bold, Modifier.weight(1f)); Text(if (week == 1) "当前" else "备考计划", 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
    }
}

@Composable
private fun LibraryScreen() {
    val books = listOf("大雁四级 · 词汇", "大雁四级 · 听力", "大雁四级 · 阅读", "大雁四级 · 写作翻译")
    val types = listOf("听力", "阅读", "选词", "匹配", "翻译", "写作")
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("学习题库", 28.sp, fontWeight = FontWeight.Bold); Text("资料 · 方法 · 模考 · 错题", 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Text("大雁四级资料", 18.sp, fontWeight = FontWeight.Bold) }
        items(books.chunked(2)) { pair -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { pair.forEach { book -> Card(Modifier.weight(1f), shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(16.dp)) { Text(book, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(12.dp)); Text("进度 0%", 12.sp, color = Primary) } } } } }
        item { Text("题型方法", 18.sp, fontWeight = FontWeight.Bold, Modifier.padding(top = 8.dp)) }
        items(types) { type -> Card(shape = RoundedCornerShape(20.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(type, fontWeight = FontWeight.SemiBold, Modifier.weight(1f)); Text("查看方法 →", 12.sp, color = Primary) } } }
    }
}

@Composable
private fun ProfileScreen() {
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("我的", 28.sp, fontWeight = FontWeight.Bold); Text("学习统计与设置", 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) { StatCard("学习天数", "0", Modifier.weight(1f)); StatCard("完成任务", "0", Modifier.weight(1f)); StatCard("模考次数", "0", Modifier.weight(1f)) } }
        item { SettingCard("深色模式", "跟随系统") }
        item { SettingCard("动画效果", "开启") }
        item { SettingCard("备考起始时间", "2026-09-14") }
        item { SettingCard("本地数据", "数据仅保存在设备") }
        item { SettingCard("版本", "0.1.0") }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(14.dp)) { Text(value, 23.sp, fontWeight = FontWeight.Bold, color = Primary); Text(label, 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}

@Composable
private fun SettingCard(title: String, value: String) {
    Card(shape = RoundedCornerShape(20.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(title, fontWeight = FontWeight.SemiBold, Modifier.weight(1f)); Text(value, 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}
