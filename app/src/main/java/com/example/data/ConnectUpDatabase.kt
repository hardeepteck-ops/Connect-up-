package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ConnectUpDao
import com.example.data.model.*
import com.example.data.seed.SeedData
import com.example.util.VideoUrlUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        LikeEntity::class,
        CommentEntity::class,
        CommentLikeEntity::class,
        FollowEntity::class,
        MessageEntity::class,
        NotificationEntity::class,
        SavedPostEntity::class,
        ReportEntity::class,
        BlockedUserEntity::class,
        SearchHistoryEntity::class,
        AppSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ConnectUpDatabase : RoomDatabase() {

    abstract fun connectUpDao(): ConnectUpDao

    companion object {
        @Volatile
        private var INSTANCE: ConnectUpDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ConnectUpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConnectUpDatabase::class.java,
                    "connectup_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        SeedData.populateInitialData(database.connectUpDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        val dao = database.connectUpDao()
                        try {
                            for ((oldUrl, newUrl) in VideoUrlUtils.LEGACY_URL_MAPPINGS) {
                                dao.updateLegacyMediaUrl(oldUrl, newUrl)
                            }
                            dao.updateAllLegacyCommondatastorageUrls(VideoUrlUtils.DEFAULT_FALLBACK_VIDEO_URL)
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }
    }
}
