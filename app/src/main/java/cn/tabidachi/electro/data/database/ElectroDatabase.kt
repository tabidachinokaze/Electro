package cn.tabidachi.electro.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cn.tabidachi.electro.data.database.dao.AccountDao
import cn.tabidachi.electro.data.database.dao.ChunkDao
import cn.tabidachi.electro.data.database.dao.DialogDao
import cn.tabidachi.electro.data.database.dao.DownloadDao
import cn.tabidachi.electro.data.database.dao.MessageDao
import cn.tabidachi.electro.data.database.dao.MessageSendRequestDao
import cn.tabidachi.electro.data.database.dao.PathDao
import cn.tabidachi.electro.data.database.dao.QueryUserDao
import cn.tabidachi.electro.data.database.dao.SearchDao
import cn.tabidachi.electro.data.database.dao.SessionDao
import cn.tabidachi.electro.data.database.dao.SessionSearchDao
import cn.tabidachi.electro.data.database.dao.SessionUserDao
import cn.tabidachi.electro.data.database.dao.UserDao
import cn.tabidachi.electro.data.database.entity.Account
import cn.tabidachi.electro.data.database.entity.Chunk
import cn.tabidachi.electro.data.database.entity.Dialog
import cn.tabidachi.electro.data.database.entity.Download
import cn.tabidachi.electro.data.database.entity.ListTypeConverter
import cn.tabidachi.electro.data.database.entity.Message
import cn.tabidachi.electro.data.database.entity.MessageSendRequest
import cn.tabidachi.electro.data.database.entity.Path
import cn.tabidachi.electro.data.database.entity.Search
import cn.tabidachi.electro.data.database.entity.Session
import cn.tabidachi.electro.data.database.entity.SessionSearch
import cn.tabidachi.electro.data.database.entity.SessionUser
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.model.UserQuery

@Database(
    entities = [
        Dialog::class,
        Message::class,
        MessageSendRequest::class,
        SessionSearch::class,
        Path::class,
        SessionUser::class,
        Account::class,
        Chunk::class,
        Download::class,
        User::class,
        Session::class,
        UserQuery::class,
        Search::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(ListTypeConverter::class)
abstract class ElectroDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun messageSendRequestDao(): MessageSendRequestDao
    abstract fun dialogDao(): DialogDao
    abstract fun sessionSearchDao(): SessionSearchDao
    abstract fun pathDao(): PathDao
    abstract fun sessionUserDao(): SessionUserDao
    abstract fun accountDao(): AccountDao
    abstract fun chunkDao(): ChunkDao
    abstract fun downloadDao(): DownloadDao
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun queryUserDao(): QueryUserDao
    abstract fun searchDao(): SearchDao
}