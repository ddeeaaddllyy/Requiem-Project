package com.application.requiemproject.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.application.requiemproject.data.converters.RoomTypeConverters
import com.application.requiemproject.data.local.dao.UserDao
import com.application.requiemproject.data.local.entities.User

@TypeConverters(value = [RoomTypeConverters::class])
@Database(entities = [User::class], version = 1)
public abstract class AppDatabase: RoomDatabase() {

    abstract fun userDao(): UserDao

    public companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "requiem_database"
                )
                    .allowMainThreadQueries()
                    //DELETE IN FINAL VERSION
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}