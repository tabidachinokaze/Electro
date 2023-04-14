package cn.tabidachi.electro.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import cn.tabidachi.electro.data.database.entity.SessionSearch

@Dao
interface SessionSearchDao {
    @Query("select * from sessionsearch where sid = :sid")
    fun findById(sid: Long): SessionSearch?
    @Query("select * from sessionsearch where title like :title")
    fun query(title: String): List<SessionSearch>
    @Insert
    fun insert(sessionSearch: SessionSearch)
    @Update
    fun update(sessionSearch: SessionSearch)
    @Upsert
    fun upsert(sessionSearch: SessionSearch)
}