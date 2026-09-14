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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.zxmstar.cet4prep.data.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val Primary = Color(0xFF4A90A4)
private val DarkPrimary = Color(0xFF6BAFC2)
private val Orange = Color(0xFFFF9A56)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = Room.databaseBuilder(applicationContext, Cet4Database::class.java, "cet4.db").build()
        setContent { AppTheme { App(Cet4Repository(db.dayTaskDao())) } }
    }
}

@Composable
private fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(colorScheme = if (dark) darkColorScheme(primary = DarkPrimary, secondary = Orange) else lightColorScheme(primary = Primary, secondary = Orange), content = content)
}

@Composable
private fun App(repo: Cet4Repository) {
    val tasks by repo.observeToday().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var tab by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { repo.seedToday() }
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            AnimatedContent(targetState = tab, label = "page") { page ->
                when (page) {
                    0 -> Home(tasks) { scope.launch { repo.setCompleted(it.title, !it.completed) } }
                    1 -> Calendar()
                    2 -> Library()
                    else -> Me()
                }
            }
        }
        NavigationBar(Modifier.navigationBarsPadding(), containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .94f)) {
            listOf(Icons.Default.Home to "首页", Icons.Default.CalendarMonth to "日历", Icons.Default.MenuBook to "题库", Icons.Default.Person to "我的").forEachIndexed { i, item ->
                NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { val s by animateFloatAsState(if (tab == i) 1.08f else 1f, spring(), label = "nav"); Icon(item.first, contentDescription = item.second, modifier = Modifier.scale(s)) }, label = { Text(text = item.second) })
            }
        }
    }
}

@Composable
private fun Home(tasks: List<DayTaskEntity>, toggle: (DayTaskEntity) -> Unit) {
    val start = LocalDate.of(2026, 9, 14)
    val exam = LocalDate.of(2026, 12, 13)
    val today = LocalDate.now()
    val day = (ChronoUnit.DAYS.between(start, today).toInt() + 1).coerceIn(1, 90)
    val left = ChronoUnit.DAYS.between(today, exam).coerceAtLeast(0)
    val done = tasks.count { it.completed }
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(text = "90天备考计划 · 第" + day + "天", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "今天也向四级靠近一点", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.size(44.dp).background(Primary.copy(alpha = .12f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Text(text = "四", color = Primary, fontWeight = FontWeight.Bold) }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Primary)) {
                Column(Modifier.padding(20.dp)) {
                    Text(text = "距离 CET-4 考试", fontSize = 13.sp, color = Color.White.copy(alpha = .82f))
                    Text(text = "$left 天", fontSize = 39.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = "基础攻坚期 · 第" + day + "天", fontSize = 13.sp, color = Color.White.copy(alpha = .9f))
                    Spacer(Modifier.height(14.dp))
                    LinearProgressIndicator(progress = { day / 90f }, modifier = Modifier.fillMaxWidth().height(7.dp), color = Color.White, trackColor = Color.White.copy(alpha = .22f))
                }
            }
        }
        item { Row(verticalAlignment = Alignment.CenterVertically) { Text(text = "今日任务", fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text(text = "$done / ${tasks.size} 项", fontSize = 13.sp, color = Primary) } }
        items(tasks, key = { it.title }) { TaskCard(it, toggle) }
        item { AnimatedVisibility(done == tasks.size && tasks.isNotEmpty(), enter = fadeIn() + scaleIn(), exit = fadeOut()) { Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Orange.copy(alpha = .12f))) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Check, contentDescription = null, tint = Orange); Spacer(Modifier.width(10.dp)); Text(text = "今日学习完成，保持这个节奏。") } } } }
    }
}

@Composable
private fun TaskCard(task: DayTaskEntity, toggle: (DayTaskEntity) -> Unit) {
    var down by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (down) .96f else 1f, spring(), label = "press")
    val accent = if (task.title.contains("听力")) Orange else Primary
    Card(Modifier.fillMaxWidth().scale(scale).combinedClickable(onClick = { down = false; toggle(task) }, onLongClick = { down = false }), shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(43.dp).background(accent, RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(text = task.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(text = task.detail, modifier = Modifier.padding(top = 5.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = if (task.completed) "✓" else task.duration, fontSize = if (task.completed) 22.sp else 11.sp, fontWeight = FontWeight.Bold, color = if (task.completed) Primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Calendar() {
    val phases = listOf("基础攻坚期", "题型强化期", "冲刺模考期")
    var phase by remember { mutableIntStateOf(0) }
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(text = "备考日历", fontSize = 28.sp, fontWeight = FontWeight.Bold); Text(text = "90天阶段规划", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f), RoundedCornerShape(18.dp)).padding(4.dp)) { phases.forEachIndexed { i, p -> Box(Modifier.weight(1f).combinedClickable(onClick = { phase = i }, onLongClick = {}).background(if (i == phase) Primary else Color.Transparent, RoundedCornerShape(15.dp)).padding(vertical = 11.dp), contentAlignment = Alignment.Center) { Text(text = p, fontSize = 11.sp, color = if (i == phase) Color.White else MaterialTheme.colorScheme.onSurfaceVariant) } } } }
        item { Card(shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { Text(text = phases[phase], fontSize = 19.sp, fontWeight = FontWeight.Bold); Text(text = when (phase) { 0 -> "建立词汇、听力与阅读基础"; 1 -> "围绕六大题型进行专项强化"; else -> "模拟考试、错题复盘与冲刺" }, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(text = "日期任务容器已建立，真实90天计划将在资料导入后绑定。", fontSize = 12.sp, color = Primary) } } }
        items((1..13).toList()) { week -> Card(shape = RoundedCornerShape(20.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(text = "第 $week 周", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); Text(text = if (week == 1) "当前阶段" else "待学习", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
    }
}

@Composable
private fun Library() {
    val sections = listOf("资料", "方法", "模考", "错题")
    var section by remember { mutableIntStateOf(0) }
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(text = "学习题库", fontSize = 28.sp, fontWeight = FontWeight.Bold); Text(text = "资料 · 方法 · 模考 · 错题", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f), RoundedCornerShape(18.dp)).padding(4.dp)) { sections.forEachIndexed { i, s -> Box(Modifier.weight(1f).combinedClickable(onClick = { section = i }, onLongClick = {}).background(if (i == section) Primary else Color.Transparent, RoundedCornerShape(15.dp)).padding(vertical = 11.dp), contentAlignment = Alignment.Center) { Text(text = s, fontSize = 12.sp, color = if (i == section) Color.White else MaterialTheme.colorScheme.onSurfaceVariant) } } } }
        when (section) {
            0 -> { item { Text(text = "大雁四级资料", fontSize = 18.sp, fontWeight = FontWeight.Bold) }; items(listOf("词汇", "听力", "阅读", "写作翻译")) { name -> Card(shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(16.dp)) { Text(text = "大雁四级 · $name", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(9.dp)); Text(text = "进度 0% · 等待真实资料导入", fontSize = 12.sp, color = Primary) } } }; item { Card(shape = RoundedCornerShape(20.dp)) { Text(text = "墨墨背单词 · 本地进度同步入口", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
            1 -> { item { Text(text = "六大题型方法", fontSize = 18.sp, fontWeight = FontWeight.Bold) }; items(listOf("听力", "阅读", "选词", "匹配", "翻译", "写作")) { type -> var open by remember { mutableStateOf(false) }; Card(Modifier.fillMaxWidth().combinedClickable(onClick = { open = !open }, onLongClick = {}), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(16.dp)) { Row { Text(text = type, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); Text(text = if (open) "收起" else "展开", fontSize = 12.sp, color = Primary) }; AnimatedVisibility(open) { Text(text = "方法内容必须根据你提供的大雁四级资料填充，当前不虚构教材内容。", modifier = Modifier.padding(top = 10.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } } }
            2 -> item { Card(shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(text = "离线模拟考试", fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(text = "计时、作答、交卷、评分、错题收录框架已预留。", color = MaterialTheme.colorScheme.onSurfaceVariant); Text(text = "当前：真实题库未导入", fontWeight = FontWeight.SemiBold, color = Orange) } } }
            else -> item { Card(shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(text = "错题复盘", fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(text = "暂无错题。完成真实模考后自动收录。", color = MaterialTheme.colorScheme.onSurfaceVariant); Text(text = "趋势图：等待模考记录", fontSize = 13.sp, color = Primary) } } }
        }
    }
}

@Composable
private fun Me() {
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(text = "我的", fontSize = 28.sp, fontWeight = FontWeight.Bold); Text(text = "学习统计与设置", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) { Stat("0", "学习天数", Modifier.weight(1f)); Stat("0", "完成任务", Modifier.weight(1f)); Stat("0", "模考次数", Modifier.weight(1f)) } }
        item { Setting("深色模式", "跟随系统") }
        item { Setting("动画效果", "开启") }
        item { Setting("备考起始时间", "2026-09-14") }
        item { Setting("本地数据", "Room 持久化") }
        item { Setting("版本", "0.1.0") }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier) { Card(modifier, shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(14.dp)) { Text(text = value, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Primary); Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable
private fun Setting(title: String, value: String) { Card(shape = RoundedCornerShape(20.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(text = title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); Text(text = value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
