package edu.uci.ics.fabflixmobile.ui.mainPage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.RequestQueue;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityMainPageBinding;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

import java.util.HashMap;
import java.util.Map;


public class MainPageActivity extends AppCompatActivity {
    private EditText query;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout view
        setContentView(R.layout.activity_main_page);

        this.button = findViewById(R.id.searchButton);
        this.query = findViewById(R.id.search);

        // assign button listener to start querying
        button.setOnClickListener(view -> handleMovieSearch());
    }

    @SuppressLint("SetTextI18n")
    public void handleMovieSearch() {
        Log.d("Going to movie list activity", query.getText().toString());

        // initialize the activity(page)/destination
        Intent movieListActivity = new Intent(MainPageActivity.this, MovieListActivity.class);
        movieListActivity.putExtra("query", query.getText().toString());
        startActivity(movieListActivity);

    }
}