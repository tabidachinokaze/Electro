package cn.tabidachi.electro.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cn.tabidachi.electro.data.database.entity.User

@Dao
interface UserDao {
    @Query("select * from user where uid = :uid")
    fun find(uid: Long): User?
    @Upsert
    fun upsert(user: User)
}