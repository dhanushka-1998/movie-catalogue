package com.example.moviecatalogue

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SearchAllMovies : AppCompatActivity() {

    // Define ui components
    private lateinit var searchText: EditText
    private lateinit var retrieveButton: Button
    private lateinit var displayResult: TextView
    private lateinit var errorView: TextView
    private lateinit var searchPageIcon: ImageView
    private var numberOfResults = 0
    private var totalResults = 0
    private var displayText = StringBuilder()
    private var errorText = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_all_movies)

        this.supportActionBar?.hide() // Hide Action Bar when application startup

        // All components of Search All Movie Screen
        searchText = findViewById(R.id.search)
        retrieveButton = findViewById(R.id.retrieve)
        displayResult = findViewById(R.id.result)
        searchPageIcon = findViewById(R.id.searchPageIcon2)
        errorView = findViewById(R.id.errorView)

        // Set click listener of Retrieve Button
        retrieveButton.setOnClickListener {

            /**
             * Reference : Lecture 7, Lecture 8, Tutorial 7, Tutorial 8, Tutorial 9
             **/

            searchText.hideKeyboard()       // Run function of hide keyboard when retrieve button clicked
            displayResult.text = " "        // Set Text empty when every time clicking the retrieve button
            errorView.text = " "            // Set Text empty when every time clicking the retrieve button
            displayText.clear()             // Clear displayText when every time clicking the retrieve button
            errorText.clear()               // Clear displayText when every time clicking the retrieve button

            // Collecting all the JSON string
            val stb = StringBuilder()
            totalResults=0
            numberOfResults=0
            stb.clear()
            var pageNumber = 1

            var isIterate = true
            while(isIterate) {
                val urlString = "https://www.omdbapi.com/?&apikey=4facec60&s=${searchText.text}*&plot=full&type=movie&page=${pageNumber}"
                val url = URL(urlString)
                val con: HttpURLConnection = url.openConnection() as HttpURLConnection

                runBlocking {
                    launch {
                        // Checking the input is empty or not and notify by toast message
                        if (searchText.text.isEmpty() || urlString.isEmpty()) {
                            runOnUiThread {
                                Toast.makeText(this@SearchAllMovies, "Please Enter the Movie Name!", Toast.LENGTH_SHORT).show()
                                searchPageIcon.visibility = View.VISIBLE    // Make visible searchPageIcon logo when not showing movie details
                            }
                        } else {
                            // Run the code of the coroutine in a new thread
                            withContext(Dispatchers.Default) {
                                val bf = BufferedReader(InputStreamReader(con.inputStream))
                                var line: String? = bf.readLine()
                                while (line != null) {
                                    stb.append(line + "\n")
                                    line = bf.readLine()
                                }
                                parseJSON(stb)      // Pass all detail object to the parseJSON function
                            }
                        }
                    }
                }
                stb.clear()
                pageNumber++

                // Not taken below as it takes much time
                if(numberOfResults<50) {
                    if (numberOfResults == totalResults) {
                        isIterate = false
                    }
                }else{
                    if(numberOfResults == 50){
                        isIterate = false
                    }
                }
            }
            displayResult.text = displayText
        }
    }

    /**
     * This Method [parseJSON] parse all the data of JSON Object
     * Reference : Lecture 7
     **/
    private fun parseJSON(stb: java.lang.StringBuilder) {

        // This contains the full JSON returned by the Web Service
        val json = JSONObject(stb.toString())

        if((!json["Response"].equals("False"))) {
            if (totalResults == 0) {
                val totalStr = json["totalResults"] as String
                totalResults = totalStr.toInt()
            }

            // Information about all the Movies extracted by this function
            val jsonArray: JSONArray = json.getJSONArray("Search")
            var i = 0
            while (i != jsonArray.length()) {
                val movie: JSONObject = jsonArray[i] as JSONObject

                val title = movie["Title"] as String
                numberOfResults++
                displayText.append("${numberOfResults}) Movie Title: \"${title}\" \n")
                i++
            }
            searchPageIcon.visibility = View.INVISIBLE  // Make invisible searchPageIcon logo when showing movie details

        }else{

            runOnUiThread{
                Toast.makeText(this@SearchAllMovies, "No Results Found!", Toast.LENGTH_SHORT).show()
                errorText.append("No Results Found!")
                this.errorView.text = errorText
                searchPageIcon.visibility =View.VISIBLE     // Make visible searchPageIcon logo when not showing movie details
            }
        }
    }

    /**
     * This Method [onSaveInstanceState] save the necessary data on activity before the rotation
     **/
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("resultText",displayText.toString())
        outState.putString("errorText",errorText.toString())
    }

    /**
     * This [onRestoreInstanceState] method Restore necessary data to a bundle after the rotation
     **/
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val txt1 = savedInstanceState.getString("resultText")
        val txt2 = savedInstanceState.getString("errorText").toString()

        displayText.append(txt1)
        errorText.append(txt2)

        if (displayText.isEmpty()){
            runOnUiThread {
                searchPageIcon.visibility = View.VISIBLE
                errorView.text = txt2
            }
        }
        else {
            runOnUiThread {
                searchPageIcon.visibility = View.INVISIBLE
                displayResult.text = txt1
            }
        }
    }

    /**
     * This [EditText.hideKeyboard] is [hideKeyboard] function extension of [EditText]
     * Reference : https://stackoverflow.com/questions/41790357/close-hide-the-android-soft-keyboard-with-kotlin
     **/
    private fun EditText.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    }

}