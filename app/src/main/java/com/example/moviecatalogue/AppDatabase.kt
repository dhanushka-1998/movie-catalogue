package com.example.moviecatalogue

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Reference : Lecture 6, Tutorial 7
 **/

@Database(entities = [Movie::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao() : MovieDao

    companion object {

        private var INSTANCE : AppDatabase? = null

        fun getInstance(context: Context) : AppDatabase {
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context,AppDatabase::class.java,"myAppDatabase").build()
            }
            return INSTANCE!!
        }

    }
}