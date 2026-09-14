package com.zxmstar.cet4prep.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "day_tasks", primaryKeys = ["date", "title"])
data class DayTaskEntity(
    val date: String,
    val title: String,
    val detail: String,
    val duration: String,
    val completed: Boolean = false,
    val completedAt: Long? = null
)

@Dao
interface DayTaskDao {
    @Query("SELECT * FROM day_tasks WHERE date = :date ORDER BY title")
    fun observe(date: String): Flow<List<DayTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: DayTaskEntity)

    @Query("UPDATE day_tasks SET completed = :completed, completedAt = :completedAt WHERE date = :date AND title = :title")
    suspend fun setCompleted(date: String, title: String, completed: Boolean, completedAt: Long?)
}

@Database(entities = [DayTaskEntity::class], version = 1, exportSchema = false)
abstract class Cet4Database : RoomDatabase() {
    abstract fun dayTaskDao(): DayTaskDao
}
