package moe.tabidachi.electro.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import moe.tabidachi.electro.data.database.dao.AccountDao
import moe.tabidachi.electro.data.database.dao.ChunkDao
import moe.tabidachi.electro.data.database.dao.DialogDao
import moe.tabidachi.electro.data.database.dao.DownloadDao
import moe.tabidachi.electro.data.database.dao.MessageDao
import moe.tabidachi.electro.data.database.dao.MessageSendRequestDao
import moe.tabidachi.electro.data.database.dao.PathDao
import moe.tabidachi.electro.data.database.dao.QueryUserDao
import moe.tabidachi.electro.data.database.dao.SearchDao
import moe.tabidachi.electro.data.database.dao.SessionDao
import moe.tabidachi.electro.data.database.dao.SessionSearchDao
import moe.tabidachi.electro.data.database.dao.SessionUserDao
import moe.tabidachi.electro.data.database.dao.UserDao
import moe.tabidachi.electro.data.database.entity.Account
import moe.tabidachi.electro.data.database.entity.Chunk
import moe.tabidachi.electro.data.database.entity.Dialog
import moe.tabidachi.electro.data.database.entity.Download
import moe.tabidachi.electro.data.database.entity.ListTypeConverter
import moe.tabidachi.electro.data.database.entity.Message
import moe.tabidachi.electro.data.database.entity.MessageSendRequest
import moe.tabidachi.electro.data.database.entity.Path
import moe.tabidachi.electro.data.database.entity.Search
import moe.tabidachi.electro.data.database.entity.Session
import moe.tabidachi.electro.data.database.entity.SessionSearch
import moe.tabidachi.electro.data.database.entity.SessionUser
import moe.tabidachi.electro.data.database.entity.User
import moe.tabidachi.electro.model.UserQuery

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