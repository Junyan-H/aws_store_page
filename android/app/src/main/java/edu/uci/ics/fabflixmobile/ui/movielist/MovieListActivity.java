package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import edu.uci.ics.fabflixmobile.ui.mainPage.MainPageActivity;
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {
    private String userInput;
    private Button next;
    private Button prev;
    private TextView pageCnt;
    private final String host = "52.53.209.227";
    private final String port = "8443";
    private final String domain = "cs122b-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    private final String queryUrl = "&year=&director=&starName=&genre=";
    private final String pUrl = "&n=10&sortOption=truu&arrow=prev";
    private final String nUrl = "&n=10&sortOption=truu&arrow=next";

    private MovieListViewAdapter adapter;

    private String url = "";
    // -1 means prev 0 means default 1 means next
    private int buttonState = 0;
    private ArrayList<Movie> movieList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        this.userInput = getIntent().getStringExtra("query");
        Log.d("User Input", userInput);
        this.pageCnt = findViewById(R.id.pageCnt);

        if(this.buttonState == 0) {
            sendQuery();
        }

        //created an adapted view to display list of movies that are viewed
        this.adapter = new MovieListViewAdapter(this, movieList);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);

        this.prev = findViewById(R.id.prevBtn);
        this.next = findViewById(R.id.nextBtn);

        this.prev.setOnClickListener(view -> {
            Log.d("PREV BUTTON", "CLICKED");
            this.buttonState = -1;
            sendQuery();
        });
        this.next.setOnClickListener(view -> {
            Log.d("NEXT BUTTON", "CLICKED");
            this.buttonState = 1;
            sendQuery();
        });

        // when a movie is pressed
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = movieList.get(position);
            Log.d("Movie id pressed", movie.getId());
            Intent singleMovieActivity = new Intent(MovieListActivity.this, SingleMovieActivity.class);
            singleMovieActivity.putExtra("movieId", movie.getId());
            startActivity(singleMovieActivity);
        });
    }

    @SuppressLint("SetTextI18n")
    public void sendQuery(){
        this.movieList.clear();
        Log.d("SEND QUERY: ", this.userInput);
        // Utilizing same network queue
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // url for normal search
        if (this.buttonState == 0){
            url = baseURL + "/api/movie-list?title=" + this.userInput + this.queryUrl + "&n=null&sortOption=null&arrow=null";
        }
        //url for next page
        else if (this.buttonState == 1) {
            url = baseURL + "/api/movie-list?title=null&year=null&director=null&starName=null&genre=null" + this.nUrl;
        }
        //url for prev page
        else {
            url = baseURL + "/api/movie-list?title=null&year=null&director=null&starName=null&genre=null" + this.pUrl;
        }
        final StringRequest queryRequest = new StringRequest(
        // grab Get request from get method
        Request.Method.GET,
        this.url,
        response -> {
            Log.d("~~~~ QUERY SUCCESS: ~~~~", response);
            // response object with title passed as param
            // Parsing the response to json
            Gson gson = new Gson();
            JsonArray jsA = gson.fromJson(response, JsonArray.class);

            Log.d("QUERY LEN", Integer.toString(jsA.size()));
            Log.d("JSA", jsA.toString());
            int loop_cnt = jsA.size();
            if (buttonState == -1 || buttonState == 1){
                loop_cnt -= 1;
                JsonObject metadata = jsA.get(loop_cnt).getAsJsonObject();
                this.pageCnt.setText(metadata.get("pageNum").toString());
            }

            // loop through list of resultdata and grab the movie infos
            for (int i = 0; i < loop_cnt; i++){
                JsonObject jo = jsA.get(i).getAsJsonObject();
                Log.d("JSO", jo.toString());
                String id = jo.get("movie_id").toString();
                String title = jo.get("movie_title").toString();
                String year = jo.get("movie_year").toString();
                String director = jo.get("movie_director").toString();
                String rating = jo.get("movie_rating").toString();
                JsonArray starNames = jo.get("star_list").getAsJsonArray();
                JsonArray movieGenre = jo.get("movie_genres").getAsJsonArray();

                Movie m = new Movie(title, year, director, rating, id, starNames, movieGenre);
                this.movieList.add(m);
            }
            adapter.notifyDataSetChanged();
        },
        error -> {
            Log.d("~~~~ SEARCH ERROR: ~~~~", error.toString());
        });

        // Add GET request to network queue
        queue.add(queryRequest);
    }
}