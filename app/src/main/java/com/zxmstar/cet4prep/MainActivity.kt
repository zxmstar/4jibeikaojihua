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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val Primary = Color(0xFF4A90A4)
private val Highlight = Color(0xFFFF9A56)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CET4PrepTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CET4App()
                }
            }
        }
    }
}

@Composable
private fun CET4PrepTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Primary,
            secondary = Highlight
        ),
        content = content
    )
}

private data class Task(
    val title: String,
    val detail: String,
    val duration: String,
    val accent: Color,
    val completed: Boolean = false
)

@Composable
private fun CET4App() {
    var selectedTab by remember { mutableStateOf(0) }
    var tasks by remember {
        mutableStateOf(
            listOf(
                Task("墨墨背单词", "完成今日词汇进度", "今日词汇", Primary, true),
                Task("大雁四级 · 听力", "基础训练 · 15 分钟", "约 15 分钟", Primary),
                Task("大雁四级 · 阅读", "基础训练 · 15 分钟", "约 15 分钟", Highlight)
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(targetState = selectedTab, label = "tab") { tab ->
                when (tab) {
                    0 -> HomeScreen(tasks) { title ->
                        tasks = tasks.map { if (it.title == title) it.copy(completed = !it.completed) else it }
                    }
                    1 -> SimplePage("备考日历", "90天阶段计划")
                    2 -> SimplePage("学习题库", "资料 · 方法 · 模考 · 错题")
                    else -> SimplePage("个人中心", "学习统计与设置")
                }
            }
        }
        NavigationBar(
            modifier = Modifier.navigationBarsPadding(),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        ) {
            val items = listOf(
                Triple(Icons.Default.Home, "首页", "首页"),
                Triple(Icons.Default.CalendarMonth, "日历", "备考日历"),
                Triple(Icons.Default.MenuBook, "题库", "学习题库"),
                Triple(Icons.Default.Person, "我的", "个人中心")
            )
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = {
                        val scale by animateFloatAsState(
                            if (selectedTab == index) 1.08f else 1f,
                            animationSpec = spring(),
                            label = "navScale"
                        )
                        Icon(item.first, contentDescription = item.second, modifier = Modifier.scale(scale))
                    },
                    label = { Text(item.second) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = Primary.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(tasks: List<Task>, onTaskClick: (String) -> Unit) {
    val start = LocalDate.of(2026, 9, 14)
    val exam = LocalDate.of(2026, 12, 13)
    val today = LocalDate.now()
    val day = ChronoUnit.DAYS.between(start, today).toInt() + 1
    val remaining = ChronoUnit.DAYS.between(today, exam).coerceAtLeast(0)
    val progress = (day.coerceIn(1, 90) / 90f)
    val completed = tasks.count { it.completed }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 18.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("90天备考计划 · 第${day.coerceIn(1, 90)}天", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("今天也向四级靠近一点", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.size(44.dp).background(Primary.copy(alpha = .12f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) { Text("四", color = Primary, fontWeight = FontWeight.Bold) }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("距离 CET-4 考试", color = Color.White.copy(alpha = .82f), fontSize = 13.sp)
                    Text("$remaining 天", color = Color.White, fontSize = 39.sp, fontWeight = FontWeight.Bold)
                    Text("基础攻坚期 · 当前完成度 ${(progress * 100).toInt()}%", color = Color.White.copy(alpha = .9f), fontSize = 13.sp)
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(7.dp).background(Color.White.copy(alpha = .22f), RoundedCornerShape(8.dp))) {
                        Box(Modifier.fillMaxWidth(progress).height(7.dp).background(Color.White, RoundedCornerShape(8.dp)))
                    }
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("今日任务", fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("$completed / ${tasks.size} 项", fontSize = 13.sp, color = Primary)
            }
        }

        items(tasks, key = { it.title }) { task ->
            TaskCard(task, onClick = { onTaskClick(task.title) })
        }

        item {
            AnimatedVisibility(
                visible = completed == tasks.size,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Highlight.copy(alpha = .12f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, null, tint = Highlight)
                        Spacer(Modifier.width(10.dp))
                        Text("今日学习完成，保持这个节奏。", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(task: Task, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .96f else 1f, spring(), label = "press")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                onClick = {
                    pressed = false
                    onClick()
                },
                onLongClick = { pressed = false }
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(43.dp).background(task.accent, RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(task.detail, modifier = Modifier.padding(top = 5.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(if (task.completed) "✓" else task.duration, color = if (task.completed) Primary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = if (task.completed) 22.sp else 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SimplePage(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHome() {
    CET4PrepTheme { CET4App() }
}
