package cn.tabidachi.electro.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import cn.tabidachi.electro.data.database.entity.Path

@Dao
interface PathDao {
    @Query("select * from path where id = :id")
    fun find(id: String): Path?
    @Insert
    fun insert(path: Path)
    @Update
    fun update(path: Path)
    @Upsert
    fun upsert(path: Path)
    @Query("delete from path where id = :id")
    fun delete(id: String)
}