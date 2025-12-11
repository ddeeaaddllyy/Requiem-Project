package com.application.requiemproject.ui.search

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [HelpItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun helpDao(): HelpDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "requirem_database"
                )
                    .addCallback(HelpDatabaseCallback())
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

    private class HelpDatabaseCallback(): Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.helpDao())
                }
            }
        }

        suspend fun populateDatabase(helpDao: HelpDao) {
            val items = HelpData.getInitialList()
            helpDao.insertAll(items)
        }
    }

}
