package cn.tabidachi.electro.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cn.tabidachi.electro.data.database.entity.Search

@Dao
interface SearchDao {
    @Query("select * from search where uid = :uid")
    fun find(uid: Long): Search?
    @Upsert
    fun upsert(search: Search)
}