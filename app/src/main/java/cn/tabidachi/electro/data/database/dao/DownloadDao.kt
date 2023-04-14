package cn.tabidachi.electro.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cn.tabidachi.electro.data.database.entity.Download

@Dao
interface DownloadDao {
    @Query("select * from download where id = :id")
    fun find(id: String): Download?
    @Upsert
    fun upsert(download: Download)
    @Query("delete from download where id = :id")
    fun delete(id: String)
}