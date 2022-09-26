package com.example.moviecatalogue

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Reference : Lecture 6, Tutorial 7
 **/

@Dao
interface MovieDao{
    @Query("Select * from movie")
    suspend fun getAll() : List<Movie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(vararg movies:Movie)

    @Query("DELETE from movie")
    suspend fun deleteAll()
}