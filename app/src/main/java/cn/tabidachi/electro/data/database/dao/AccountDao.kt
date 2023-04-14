package cn.tabidachi.electro.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cn.tabidachi.electro.data.database.entity.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("select * from account where uid = :uid")
    fun findByUser(uid: Long): Account?
    @Upsert
    fun upsert(account: Account)
    @Query("select * from account")
    fun allFlow(): Flow<List<Account>>
    @Query("delete from account where uid = :uid")
    fun delete(uid: Long)
}