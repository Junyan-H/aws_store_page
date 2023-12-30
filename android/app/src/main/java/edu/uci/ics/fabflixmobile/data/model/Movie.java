package edu.uci.ics.fabflixmobile.data.model;

import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String title;
    private final String year;
    private final String director;
    private final String rating;
    private final String id;
    private String topStarsString;
    private String topGenresString;

    public Movie(String title, String year, String director, String rating, String id, JsonArray starNames, JsonArray movieGenres) {
        this.id = id.substring(1, id.length() - 1);
        this.title = title.substring(1, title.length() - 1);
        this.year = year.substring(1, year.length() - 1);
        this.director = director.substring(1, director.length() - 1);
        this.rating = rating.substring(1, rating.length() - 1);
        this.topStarsString = "Stars: ";
        this.topGenresString = "Genres: ";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < starNames.size(); i++) {
            JsonObject jo = starNames.get(i).getAsJsonObject();
            String starName = jo.get("star_name").toString();
            if (i < starNames.size() - 1){
                sb.append(starName.substring(1, starName.length() - 1)).append(", ");
            } else {
                sb.append(starName.substring(1, starName.length() - 1));
            }

        }
        this.topStarsString += sb.toString();
        sb.setLength(0);

        for (int i = 0; i < movieGenres.size(); i++) {
            String genreName = movieGenres.get(i).toString();
            if(i < movieGenres.size() - 1){
                sb.append(genreName.substring(1, genreName.length() - 1) + ", ");
            } else {
                sb.append(genreName.substring(1, genreName.length() - 1));
            }
        }
        this.topGenresString += sb.toString();
        sb.setLength(0);
    }

    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public String getRating() {
        return rating + " ✩";
    }

    public String getId() { return id; }

    public String getStarNames() {
        return this.topStarsString;
    }

    public String getMovieGenres() {
        return this.topGenresString;
    }
}