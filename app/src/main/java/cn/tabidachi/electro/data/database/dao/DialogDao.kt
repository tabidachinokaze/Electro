package cn.tabidachi.electro.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import cn.tabidachi.electro.data.database.entity.Dialog
import kotlinx.coroutines.flow.Flow

@Dao
interface DialogDao {
    @Query("select * from dialog where uid = :uid order by latest desc")
    fun dialogsFlow(uid: Long): Flow<List<Dialog>>
    @Query("select * from dialog where uid like :uid")
    fun dialogs(uid: Long): List<Dialog>
    @Query("select * from dialog where sid = :sid and uid = :uid")
    fun dialog(sid: Long, uid: Long): Dialog?
    @Insert
    fun insert(dialog: Dialog)
    @Update
    fun update(dialog: Dialog)
    @Upsert
    fun upsert(dialog: Dialog)
    @Query("delete from dialog where sid = :sid")
    fun delete(sid: Long)
}