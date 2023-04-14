package cn.tabidachi.electro.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cn.tabidachi.electro.data.database.entity.Session

@Dao
interface SessionDao {
    @Query("select * from session where sid = :sid")
    fun find(sid: Long): Session?
    @Upsert
    fun upsert(session: Session)
}