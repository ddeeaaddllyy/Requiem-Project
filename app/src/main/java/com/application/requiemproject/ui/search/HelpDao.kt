package com.application.requiemproject.ui.search

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HelpDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: HelpItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HelpItem>)

    @Query("SELECT * FROM help_items WHERE question LIKE '%' || :query || '%' ORDER BY question ASC")
    fun searchHelpItems(query: String) : Flow<List<HelpItem>>
}