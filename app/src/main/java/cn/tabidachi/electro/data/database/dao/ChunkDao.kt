package cn.tabidachi.electro.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cn.tabidachi.electro.data.database.entity.Chunk
import kotlinx.coroutines.flow.Flow

@Dao
interface ChunkDao {
    @Upsert
    fun upsert(chunk: Chunk)
    @Query("select * from chunk where path = :path")
    fun getChunkFlow(path: String): Flow<List<Chunk>>
    @Query("select * from chunk where path = :path order by `offset`")
    fun getChunk(path: String): List<Chunk>
    @Query("select path from chunk group by path order by id")
    fun getPaths(): Flow<List<String>>
    @Query("select * from chunk where path = :path limit 1")
    fun chunkExists(path: String): Chunk?
    @Query("select count(id) from chunk where path = :path and uploaded = 0")
    fun unloadedFlow(path: String): Flow<Int>
    @Query("select count(id) from chunk where path = :path and uploaded = 0")
    fun unloaded(path: String): Int
    @Query("delete from chunk where path = :path")
    fun delete(path: String)
}