package cn.tabidachi.electro.data.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import cn.tabidachi.electro.model.UserQuery

@Dao
interface QueryUserDao {
    @Upsert
    fun upsert(query: UserQuery)
}