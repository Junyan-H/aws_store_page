package edu.uci.ics.fabflixmobile.ui.singlemovie;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonObject;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SingleMovieActivity extends AppCompatActivity {
    private String movieId;
    private String url;
    private TextView titleEle, yearEle, ratingEle, starsEle, genresEle, directorEle;
    private final String host = "52.53.209.227";
    private final String port = "8443";
    private final String domain = "cs122b-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_movie);
        this.movieId = getIntent().getStringExtra("movieId");
        Log.d("Single Movie", "id:" + movieId);

        this.titleEle = findViewById(R.id.title);
        this.ratingEle = findViewById(R.id.rating);
        this.yearEle = findViewById(R.id.year);
        this.directorEle = findViewById(R.id.director);
        this.starsEle = findViewById(R.id.stars);
        this.genresEle = findViewById(R.id.genres);

        getSingleMovieInfo();
    }

    @SuppressLint("SetTextI18n")
    public void getSingleMovieInfo(){
        // Utilizing same network queue
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // url for normal search
        url = baseURL + "/api/single-movie?id=" + movieId;

        final StringRequest queryRequest = new StringRequest(
                // grab Get request from get method
                Request.Method.GET,
                this.url,
                response -> {
                    Log.d("~~~~ SINGLE_M SUCCESS: ~~~~", response);
//                    // response object with title passed as param
//                    // Parsing the response to json
                    Gson gson = new Gson();
                    JsonArray jsA = gson.fromJson(response, JsonArray.class);
                    Log.d("sm-JSA", jsA.toString());

                    JsonObject jo = jsA.get(0).getAsJsonObject();
                    String title = jo.get("title").toString();
                    String year = jo.get("year").toString();
                    String director = jo.get("director").toString();
                    String rating = jo.get("rating").toString();
                    JsonArray star_list = jo.get("star_list").getAsJsonArray();
                    JsonArray genre_list = jo.get("movie_genres").getAsJsonArray();

//
//                    String year = jo.get("movie_year").toString();
//                    String director = jo.get("movie_director").toString();
//                    String rating = jo.get("movie_rating").toString();
//                    String id = jo.get("movie_id").toString();
//                    JsonArray starNames = jo.get("star_list").getAsJsonArray();
//                    JsonArray movieGenre = jo.get("movie_genres").getAsJsonArray();
                    Movie m = new Movie(title, year, director, rating, this.movieId, star_list, genre_list);
                    Log.d("title", m.getTitle());
                    Log.d("year", m.getYear());
                    Log.d("director", m.getDirector());
                    Log.d("rating", m.getRating());
                    Log.d("starlist", m.getStarNames());
                    Log.d("genrelist", m.getMovieGenres());

                    this.titleEle.setText(m.getTitle());
                    this.ratingEle.setText(m.getRating());
                    this.yearEle.setText(m.getYear());
                    this.directorEle.setText(m.getDirector());
                    this.starsEle.setText(m.getStarNames());
                    this.genresEle.setText(m.getMovieGenres());
                },
                error -> {
                    Log.d("~~~~ SINGLE_M ERROR: ~~~~", error.toString());
                });

        // Add GET request to network queue
        queue.add(queryRequest);
    }
}