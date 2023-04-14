package cn.tabidachi.electro.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import cn.tabidachi.electro.data.database.entity.SessionType
import cn.tabidachi.electro.data.database.entity.SessionUser

@Dao
interface SessionUserDao {
    @Query("select * from SessionUser where sid = :sid")
    fun find(sid: Long): SessionUser?
    @Query("select * from SessionUser where type = :type")
    fun find(type: SessionType): List<SessionUser>
    @Insert
    fun insert(sessionUser: SessionUser)
    @Update
    fun update(sessionUser: SessionUser)
    @Upsert
    fun upsert(sessionUser: SessionUser)
}