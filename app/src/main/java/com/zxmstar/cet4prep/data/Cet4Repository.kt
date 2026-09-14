package com.zxmstar.cet4prep.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class Cet4Repository(private val dao: DayTaskDao) {
    fun observeToday(): Flow<List<DayTaskEntity>> = dao.observe(LocalDate.now().toString())

    suspend fun seedToday() {
        val date = LocalDate.now().toString()
        dao.upsert(DayTaskEntity(date, "墨墨背单词", "完成今日词汇进度", "今日词汇", true))
        dao.upsert(DayTaskEntity(date, "大雁四级 · 听力", "基础训练 · 15 分钟", "约 15 分钟"))
        dao.upsert(DayTaskEntity(date, "大雁四级 · 阅读", "基础训练 · 15 分钟", "约 15 分钟"))
    }

    suspend fun setCompleted(title: String, completed: Boolean) {
        dao.setCompleted(
            date = LocalDate.now().toString(),
            title = title,
            completed = completed,
            completedAt = if (completed) System.currentTimeMillis() else null
        )
    }
}
