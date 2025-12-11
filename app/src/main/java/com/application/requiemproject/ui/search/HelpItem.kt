package com.application.requiemproject.ui.search

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "help_items")
data class HelpItem(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "question")
    val question: String,

    @ColumnInfo(name = "solution")
    val solution: String
)
