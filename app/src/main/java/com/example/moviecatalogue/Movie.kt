package com.example.moviecatalogue

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Reference : Lecture 6, Tutorial 7
 **/

@Entity
data class Movie (

    @PrimaryKey val id: Int,

    // Variables of Movie Details

    val title: String?,
    val year: String?,
    val rated: String?,
    val released: String?,
    val runtime:String?,
    val genre:String?,
    val director:String?,
    val writer:String?,
    val actors:String?,
    val plot:String?

)




