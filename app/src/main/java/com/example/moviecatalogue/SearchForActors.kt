package com.example.moviecatalogue

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SearchForActors : AppCompatActivity() {

    // Define ui components
    private lateinit var searchText: EditText
    private lateinit var searchButton: Button
    private lateinit var searchPageIcon: ImageView
    private lateinit var resultView: TextView
    private lateinit var noResultView: TextView
    private var displayText = StringBuilder()
    private var errorText = StringBuilder()
    private var database = AppDatabase.getInstance(this)
    private val movieDao = database.movieDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_for_actors)

        this.supportActionBar?.hide() // Hide Action Bar when application startup

        // All components of Search for Actor Screen
        searchText = findViewById(R.id.searchText)
        searchButton = findViewById(R.id.search)
        resultView = findViewById(R.id.result)
        searchPageIcon = findViewById(R.id.searchPageIcon1)
        noResultView = findViewById(R.id.noResultView)

        // Set click listener of Search Button
        searchButton.setOnClickListener {

            /**
             * Reference : Lecture 6, Tutorial 7
             **/

            searchText.hideKeyboard()           // Run function of hide keyboard when retrieve button clicked
            resultView.text = " "               // Set Text empty when every time clicking the retrieve button
            noResultView.text = " "             // Set Text empty when every time clicking the retrieve button
            displayText.clear()                 // Clear text when every time clicking the retrieve button
            errorText.clear()                   // Clear text when every time clicking the retrieve button

            runBlocking {
                launch {
                    // Checking the input is empty or not and notify by toast message
                    if (searchText.text.isEmpty()) {
                        runOnUiThread {
                            searchPageIcon.visibility = View.VISIBLE  // Make visible searchPageIcon logo when not showing movie details
                            Toast.makeText(this@SearchForActors, "Please Enter the Actor's Name!", Toast.LENGTH_SHORT).show()
                        }
                    } else {

                        val movies: List<Movie> = movieDao.getAll()

                        // Checking the database empty or not and notify by toast message
                        if (movies.isEmpty()) {
                            searchPageIcon.visibility = View.VISIBLE  // Make visible searchPageIcon logo when not showing movie details
                            Toast.makeText(this@SearchForActors, "Please Add Movies to the Database First!", Toast.LENGTH_SHORT).show()

                        } else {

                            for (x in movies) {
                                val str1 = x.actors.toString()
                                println(str1)   // Print on CLI all the movie's actors in Database
                                val str2 = searchText.text

                                // Checking input actor's name is in database or not
                                if (str1.contains(str2, ignoreCase = true)) {
                                    displayText.append("Title: ${x.title}\n")
                                    displayText.append("Year: ${x.year}\n")
                                    displayText.append("Rated: ${x.rated}\n")
                                    displayText.append("Released: ${x.released}\n")
                                    displayText.append("Runtime: ${x.runtime}\n")
                                    displayText.append("Genre: ${x.genre}\n")
                                    displayText.append("Director: ${x.director}\n")
                                    displayText.append("Writer: ${x.writer}\n")
                                    displayText.append("Actor: ${x.actors}\n\n")
                                    displayText.append("Plot: ${x.plot}\n\n")
                                    displayText.append("_______________________________________________\n\n\n")

                                    // Append all the details of movie and show all details on resultView
                                    resultView.text = displayText
                                    searchPageIcon.visibility = View.INVISIBLE // Make Invisible searchPageIcon logo when showing movie details
                                }
                            }
                            // Checking displayText Emptiness and showing status of results
                            if (displayText.isEmpty()) {
                                searchPageIcon.visibility = View.VISIBLE  // Make visible searchPageIcon logo when showing "No Results Found!"
                                Toast.makeText(this@SearchForActors, "No Results Found!", Toast.LENGTH_SHORT).show()
                                runOnUiThread {
                                    errorText.append("No Results Found!")
                                    noResultView.text = errorText
                                }
                            }
                        }
                    }
                }
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
        val txt1 = savedInstanceState.getString("resultText").toString()
        val txt2 = savedInstanceState.getString("errorText").toString()

        displayText.append(txt1)
        errorText.append(txt2)

        if (displayText.isEmpty()){
            runOnUiThread {
                searchPageIcon.visibility = View.VISIBLE
                noResultView.text = txt2
            }
        }
        else{
            searchPageIcon.visibility = View.INVISIBLE
            resultView.text = txt1
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