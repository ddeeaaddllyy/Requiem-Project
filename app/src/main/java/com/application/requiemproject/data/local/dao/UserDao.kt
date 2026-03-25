package com.application.requiemproject.data.local.dao

import androidx.annotation.Nullable
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.application.requiemproject.data.local.entities.User

@Dao
interface UserDao {

    @Query(value = "SELECT * FROM user WHERE login = :inputLogin LIMIT 1")
    suspend fun getUserByLogin(inputLogin: String): User?

    @Query(value = "SELECT * FROM user WHERE email = :inputEmail LIMIT 1")
    suspend fun getUserByEmail(inputEmail: String): User?

    @Query(value = "SELECT * FROM user WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Delete
    suspend fun deleteUser(user: User)
}