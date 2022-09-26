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
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SearchForMovies : AppCompatActivity() {

    // Define ui components
    private lateinit var displayResult: TextView
    private lateinit var retrieveButton: Button
    private lateinit var saveButton: Button
    private lateinit var searchText: EditText
    private lateinit var searchPageIcon: ImageView
    private lateinit var notFoundView: TextView
    private var movieDetails  = arrayListOf<String>()
    private var displayText = StringBuilder()
    private var errorText = StringBuilder()
    private var database = AppDatabase.getInstance(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_for_movies)

        this.supportActionBar?.hide() // Hide Action Bar when application startup

        // All components of Search For Movies Screen
        searchText = findViewById(R.id.search)
        saveButton = findViewById(R.id.save)
        retrieveButton = findViewById(R.id.retrieve)
        displayResult = findViewById(R.id.result)
        searchPageIcon = findViewById(R.id.searchPageIcon2)
        notFoundView = findViewById(R.id.notFoundView)

        saveButton.visibility = View.INVISIBLE      // Make invisible saveButton Button when application startup

        // Set click listener of to Retrieve Button
        retrieveButton.setOnClickListener {

            /**
             * Reference : Lecture 7, Lecture 8, Tutorial 7, Tutorial 8, Tutorial 9
             **/

            searchText.hideKeyboard()           // Run function of hide keyboard when retrieve button clicked
            displayResult.text = " "            // Set Text empty when every time clicking the retrieve button
            notFoundView.text = " "             // Set Text empty when every time clicking the retrieve button
            displayText.clear()                 // Clear displayText when every time clicking the retrieve button
            errorText.clear()                   // Clear displayText when every time clicking the retrieve button

            // Collecting all the JSON string
            val stb = StringBuilder()
            val urlString = "https://www.omdbapi.com/?&apikey=4facec60&t=${searchText.text}&plot=full&type=movie"
            val url = URL(urlString)
            val con: HttpURLConnection = url.openConnection() as HttpURLConnection

            runBlocking {
                launch {
                    // Checking the input is empty or not and notify by toast message
                    if (searchText.text.isEmpty() || urlString.isEmpty()) {
                        runOnUiThread {
                            searchPageIcon.visibility = View.VISIBLE    // Make visible searchPageIcon logo when not showing movie details
                            saveButton.visibility = View.INVISIBLE      // Make invisible saveButton button when not showing movie details
                            Toast.makeText(this@SearchForMovies, "Please Enter the Movie Name!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Run the code of the coroutine in a new thread
                        withContext(Dispatchers.IO) {
                            val bf = BufferedReader(InputStreamReader(con.inputStream))
                            var line: String? = bf.readLine()
                            while (line != null) {
                                stb.append(line + "\n")
                                line = bf.readLine()
                            }
                            parseJSON(stb)  // Pass all detail object to the parseJSON function
                        }
                    }
                }
            }
        }

        // Set click listener of Add Movies to Database Button
        saveButton.setOnClickListener {

            val movieDao = database.movieDao()
            runBlocking {
                launch {
                    val id = movieDao.getAll().size+1
                    val movie = Movie(
                        id,
                        movieDetails[0],
                        movieDetails[1],
                        movieDetails[2],
                        movieDetails[3],
                        movieDetails[4],
                        movieDetails[5],
                        movieDetails[6],
                        movieDetails[7],
                        movieDetails[8],
                        movieDetails[9]
                    )
                    movieDao.insertMovies(movie)    // Save movie details to the database
                }
            }
            // Showing toast message for successful saving
            Toast.makeText(this@SearchForMovies, "Saved to Database Successfully!", Toast.LENGTH_SHORT).show()
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
            val title = json["Title"] as String
            displayText.append("Title: \"${title}\" \n")
            movieDetails.add(title)
            val year = json["Year"] as String
            displayText.append("Year: $year \n")
            movieDetails.add(year)
            val rated = json["Rated"] as String
            displayText.append("Rated: $rated \n")
            movieDetails.add(rated)
            val released = json["Released"] as String
            displayText.append("Released: $released \n")
            movieDetails.add(released)
            val runtime = json["Runtime"] as String
            displayText.append("Runtime: $runtime \n")
            movieDetails.add(runtime)
            val genre = json["Genre"] as String
            displayText.append("Genre: $genre \n")
            movieDetails.add(genre)
            val director = json["Director"] as String
            displayText.append("Director: $director \n")
            movieDetails.add(director)
            val writer = json["Writer"] as String
            displayText.append("Writer: $writer \n")
            movieDetails.add(writer)
            val actors = json["Actors"] as String
            displayText.append("Actors: $actors \n\n")
            movieDetails.add(actors)
            val plot = json["Plot"] as String
            displayText.append("Plot: \"${plot}\" \n")
            movieDetails.add(plot)

            this@SearchForMovies.runOnUiThread {
                this.displayResult.text = displayText
                saveButton.visibility = View.VISIBLE          // Make visible saveButton Button when showing movie details
                searchPageIcon.visibility = View.INVISIBLE    // Make invisible searchPageIcon logo when showing movie details
            }

        } else{

            runOnUiThread{
                Toast.makeText(this@SearchForMovies, "No Results Found!", Toast.LENGTH_SHORT).show()
                errorText.append("No Results Found!")
                this.notFoundView.text = errorText
                searchPageIcon.visibility =View.VISIBLE     // Make visible searchPageIcon logo when not showing movie details
                saveButton.visibility=View.INVISIBLE        // Make invisible saveButton Button when not showing movie details
            }
        }
    }

    /**
     * This Method [onSaveInstanceState] save the necessary data on activity before the rotation
     **/
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("loadedValues",movieDetails)
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

        movieDetails = savedInstanceState.getStringArrayList("loadedValues") as ArrayList<String>
        displayText.append(txt1)
        errorText.append(txt2)

        if (displayText.isEmpty()){
            runOnUiThread {
                searchPageIcon.visibility = View.VISIBLE
                saveButton.visibility = View.INVISIBLE
                notFoundView.text = txt2
            }
        }
        else {
            runOnUiThread {
                searchPageIcon.visibility = View.INVISIBLE
                saveButton.visibility = View.VISIBLE
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