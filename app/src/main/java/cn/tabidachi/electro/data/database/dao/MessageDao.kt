package cn.tabidachi.electro.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import cn.tabidachi.electro.data.database.entity.Message

@Dao
interface MessageDao {
    @Query("SELECT * FROM message WHERE sid = :sid")
    fun getMessageListBySessionId(sid: Long): List<Message>

    @Query("SELECT * FROM message WHERE sid = :sid ORDER BY createTime DESC LIMIT 0, 1")
    fun getLatestMessageInSession(sid: Long): Message?
    @Query("select * from message where createTime >= :millis and sid = :sid")
    fun getMessageAfterTime(sid: Long, millis: Long): List<Message>
    @Query("select * from message where createTime < :millis and sid = :sid ORDER BY createTime DESC LIMIT :size")
    fun getMessageBeforeTime(sid: Long, millis: Long, size: Int): List<Message>
    @Query("select * from message where mid = :mid")
    fun getMessageById(mid: Long): Message?
    @Insert
    fun insert(message: Message)
    @Update
    fun update(message: Message)
    @Upsert
    fun upsert(message: Message)
    @Query("delete from message where mid = :mid")
    fun delete(mid: Long)
    @Query("select * from message where sid = :sid ORDER BY createTime DESC LIMIT :size")
    fun messages(sid: Long, size: Int): List<Message>
}