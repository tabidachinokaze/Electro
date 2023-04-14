package cn.tabidachi.electro.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import cn.tabidachi.electro.data.database.entity.MessageSendRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageSendRequestDao {
    @Insert
    fun insertMessage(messageSendRequest: MessageSendRequest)
    @Upsert
    fun upsert(messageSendRequest: MessageSendRequest)
    @Update
    fun updateMessage(messageSendRequest: MessageSendRequest)
    @Query("select * from messagesendrequest where id = :id")
    fun queryMessageById(id: String): MessageSendRequest?
    @Query("select * from messagesendrequest where sid = :chatId")
    fun queryMessageListByChatId(chatId: String): List<MessageSendRequest>
    @Query("delete from messagesendrequest where id = :id")
    fun deleteMessageById(id: String)
    @Query("select * from messagesendrequest where sid = :sid order by createTime desc")
    fun getMessagesFlow(sid: Long): Flow<List<MessageSendRequest>>
}